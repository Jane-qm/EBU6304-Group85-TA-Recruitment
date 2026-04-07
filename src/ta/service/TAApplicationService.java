// ta/service/TAApplicationService.java
package ta.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.domain.ApplicationStatus;
import common.domain.NotificationKind;
import common.entity.MOJob;
import common.entity.UserRole;
import common.service.MOJobService;
import common.service.NotificationService;
import ta.dao.TAApplicationDAO;
import ta.entity.CVInfo;
import ta.entity.TAApplication;


public class TAApplicationService {

    private final TAApplicationDAO dao = new TAApplicationDAO();
    private final MOJobService jobService = new MOJobService();
    private final TAProfileService taProfileService = new TAProfileService();
    private final CVService cvService = new CVService();
    private final NotificationService notificationService = new NotificationService();

    // ==================== 基础 CRUD 方法 ====================

    public TAApplication createOrUpdate(TAApplication application) {
        if (application.getAppliedAt() == null) {
            application.setAppliedAt(LocalDateTime.now());
        }
        return dao.save(application);
    }

    public List<TAApplication> listAll() {
        return dao.findAll();
    }

    public List<TAApplication> listByStatus(String status) {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication application : dao.findAll()) {
            if (application.getStatus() != null && application.getStatus().equalsIgnoreCase(status)) {
                result.add(application);
            }
        }
        return result;
    }

    public List<TAApplication> listByTaUserId(Long taUserId) {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication application : dao.findAll()) {
            if (taUserId != null && taUserId.equals(application.getTaUserId())) {
                result.add(application);
            }
        }
        return result;
    }

    /**
     * 根据申请ID查找
     */
    public TAApplication findById(Long applicationId) {
        for (TAApplication application : dao.findAll()) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {
                return application;
            }
        }
        return null;
    }

    /**
     * 获取TA的所有申请（按时间倒序）
     */
    public List<TAApplication> listByTaUserIdSorted(Long taUserId) {
        List<TAApplication> result = listByTaUserId(taUserId);
        result.sort((a, b) -> {
            if (a.getAppliedAt() == null) return 1;
            if (b.getAppliedAt() == null) return -1;
            return b.getAppliedAt().compareTo(a.getAppliedAt());
        });
        return result;
    }

    // ==================== 查询方法 ====================

    /**
     * MO inbox: 待审核状态
     */
    public List<TAApplication> listApplicationsAwaitingReview() {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication application : dao.findAll()) {
            if (ApplicationStatus.isAwaitingReview(application.getStatus())) {
                result.add(application);
            }
        }
        return result;
    }

    /**
     * 获取TA的活跃申请数量（待审核状态 + 候补状态）
     * 活跃申请 = 等待MO决策的申请
     */
    public int getActiveApplicationCount(Long taUserId) {
        return (int) listByTaUserId(taUserId).stream()
                .filter(a -> ApplicationStatus.isAwaitingReview(a.getStatus()) || 
                             ApplicationStatus.WAITLISTED.equals(a.getStatus()))
                .count();
    }

    // ==================== 申请提交 ====================

    /**
     * 提交申请
     * 规则：
     * 1. 不能有活跃申请（SUBMITTED/PENDING_REVIEW/WAITLISTED）
     * 2. 被拒绝过的申请不能重新提交
     * 3. 自己取消的申请（CANCELLED）可以重新提交
     */
    public TAApplication submitApplication(Long taUserId, Long jobId, String statement) {
        validateApplicationAccess(taUserId, jobId);

        List<TAApplication> userApplications = listByTaUserId(taUserId);
        
        // 检查是否已有活跃申请（SUBMITTED/PENDING_REVIEW/WAITLISTED）
        boolean hasActiveApplication = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        (ApplicationStatus.isAwaitingReview(a.getStatus()) || 
                         ApplicationStatus.WAITLISTED.equals(a.getStatus())));
        if (hasActiveApplication) {
            throw new IllegalStateException("You already have an active application for this position.");
        }
        
        // 检查是否曾被拒绝（拒绝后不可再申请同一岗位）
        boolean wasRejected = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        ApplicationStatus.isRejected(a.getStatus()));
        if (wasRejected) {
            throw new IllegalStateException("Your previous application for this position was rejected. You cannot reapply.");
        }
        
        // 取消后的申请（CANCELLED）允许重新申请，不做检查

        // 检查活跃申请数量限制（最多3个）
        if (getActiveApplicationCount(taUserId) >= 3) {
            throw new IllegalStateException("You can only have 3 active applications at once.");
        }

        TAApplication application = new TAApplication();
        application.setTaUserId(taUserId);
        application.setJobId(jobId);
        application.setStatement(statement);
        application.setStatus(ApplicationStatus.SUBMITTED);
        return createOrUpdate(application);
    }

    /**
     * 验证申请资格（个人资料和CV）
     */
    public void validateApplicationAccess(Long taUserId, Long jobId) {
        if (!taProfileService.isProfileComplete(taUserId)) {
            throw new IllegalStateException("Please complete your TA profile before applying.");
        }
        if (!cvService.hasCV(taUserId)) {
            throw new IllegalStateException("Please upload a CV before applying.");
        }

        MOJob job = jobService.getPublishedJob(jobId);
        if (job == null) {
            throw new IllegalStateException("This position has not been published yet and cannot be viewed or applied for.");
        }
    }

    // ==================== 取消申请 ====================

    /**
     * 取消申请
     * 只有 PENDING_REVIEW、SUBMITTED 或 WAITLISTED 状态的申请才能取消
     */
    public TAApplication cancelApplication(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found with ID: " + applicationId);
        }

        if (!ApplicationStatus.isCancellable(application.getStatus())) {
            throw new IllegalStateException(
                "Cannot cancel application in status: " + application.getStatus() + 
                ". Only pending or waitlisted applications can be cancelled."
            );
        }

        application.setStatus(ApplicationStatus.CANCELLED);
        TAApplication saved = dao.save(application);

        // 发送通知给TA
        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "Application Cancelled",
            "Your application #" + saved.getApplicationId() + " has been cancelled successfully.",
            "APPLICATION_CANCELLED"
        );

        return saved;
    }

    // ==================== MO 审核方法 ====================

    /**
     * MO将申请设为候补
     */
    public TAApplication markAsWaitlisted(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        
        application.setStatus(ApplicationStatus.WAITLISTED);
        TAApplication saved = dao.save(application);
        
        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "Waitlisted",
            "Your application #" + saved.getApplicationId() + " is now waitlisted.",
            NotificationKind.WAITLISTED
        );
        return saved;
    }

    /**
     * MO设为已录用（发Offer前）
     */
    public TAApplication markAsAccepted(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        
        application.setStatus(ApplicationStatus.ACCEPTED);
        return dao.save(application);
    }

    /**
     * TA接受Offer后设为已录用
     */
    public TAApplication markAsHired(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        
        application.setStatus(ApplicationStatus.HIRED);
        TAApplication saved = dao.save(application);
        
        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "Application Result",
            "Your application #" + saved.getApplicationId() + " has been approved.",
            NotificationKind.RESULT
        );
        return saved;
    }

    /**
     * MO拒绝申请
     */
    public TAApplication rejectApplication(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        
        application.setStatus(ApplicationStatus.REJECTED);
        TAApplication saved = dao.save(application);
        
        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "Application Result",
            "Your application #" + saved.getApplicationId() + " has been rejected.",
            NotificationKind.RESULT
        );
        return saved;
    }

    // ==================== 辅助方法 ====================

    public String buildApplicationSummary(TAApplication application) {
        if (application == null) {
            return "No application selected.";
        }
        return "Application ID: " + application.getApplicationId()
                + "\nJob ID: " + application.getJobId()
                + "\nStatus: " + ApplicationStatus.getDisplayText(application.getStatus())
                + "\nStatement: " + application.getStatement();
    }
    /**
     * 提交申请（带 CV 选择）
     * @param taUserId TA用户ID
     * @param jobId 职位ID
     * @param statement 申请陈述
     * @param cvId 选中的CV ID
     */
    public TAApplication submitApplication(Long taUserId, Long jobId, String statement, Long cvId) {
        // 1. 验证个人资料是否完整
        if (!taProfileService.isProfileComplete(taUserId)) {
            throw new IllegalStateException("Please complete your TA profile before applying.");
        }
        
        // 2. 验证 CV 是否存在且属于该 TA
        CVInfo selectedCV = cvService.getCVById(taUserId, cvId);
        if (selectedCV == null) {
            throw new IllegalArgumentException("Selected CV not found. Please upload a CV first.");
        }

        // 3. 验证职位是否存在且已发布
        MOJob job = jobService.getPublishedJob(jobId);
        if (job == null) {
            throw new IllegalStateException("This position has not been published yet.");
        }

        List<TAApplication> userApplications = listByTaUserId(taUserId);
        
        // 4. 检查是否已有活跃申请
        boolean hasActiveApplication = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        (ApplicationStatus.isAwaitingReview(a.getStatus()) || 
                         ApplicationStatus.WAITLISTED.equals(a.getStatus())));
        if (hasActiveApplication) {
            throw new IllegalStateException("You already have an active application for this position.");
        }
        
        // 5. 检查是否曾被拒绝
        boolean wasRejected = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        ApplicationStatus.isRejected(a.getStatus()));
        if (wasRejected) {
            throw new IllegalStateException("Your previous application for this position was rejected. You cannot reapply.");
        }

        // 6. 检查活跃申请数量限制（最多3个）
        if (getActiveApplicationCount(taUserId) >= 3) {
            throw new IllegalStateException("You can only have 3 active applications at once.");
        }

        // 7. 创建申请
        TAApplication application = new TAApplication();
        application.setTaUserId(taUserId);
        application.setJobId(jobId);
        application.setStatement(statement);
        application.setStatus(ApplicationStatus.SUBMITTED);
        
        return createOrUpdate(application);
    }
}