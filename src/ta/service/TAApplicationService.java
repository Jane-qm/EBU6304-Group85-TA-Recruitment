// ta/service/TAApplicationService.java
package ta.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.domain.ApplicationStatus;
import common.domain.NotificationKind;
import common.entity.MOJob;
import common.service.MOJobService;
import common.service.NotificationService;
import ta.dao.TAApplicationDAO;
import ta.entity.TAApplication;


public class TAApplicationService {

    private final TAApplicationDAO dao = new TAApplicationDAO();
    private final MOJobService jobService = new MOJobService();
    private final TAProfileService taProfileService = new TAProfileService();
    private final CVService cvService = new CVService();
    private final NotificationService notificationService = new NotificationService();

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

    /** MO inbox: 待审核状态 */
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
     * 获取TA的活跃申请数量（待审核状态）
     */
    public int getActiveApplicationCount(Long taUserId) {
        return (int) listByTaUserId(taUserId).stream()
                .filter(a -> ApplicationStatus.isAwaitingReview(a.getStatus()))
                .count();
    }

    /**
     * 提交申请
     */
    public TAApplication submitApplication(Long taUserId, Long jobId, String statement) {
        validateApplicationAccess(taUserId, jobId);

        // 检查是否已申请过
        boolean alreadyApplied = listByTaUserId(taUserId).stream()
                .anyMatch(a -> jobId.equals(a.getJobId()));
        if (alreadyApplied) {
            throw new IllegalStateException("You have already applied for this position.");
        }

        // 检查活跃申请数量限制
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
     * 验证申请资格
     */
    public void validateApplicationAccess(Long taUserId, Long jobId) {
        if (!taProfileService.isProfileComplete(taUserId) || !cvService.hasCV(taUserId)) {
            throw new IllegalStateException("Please complete your TA profile and upload a CV before applying.");
        }

        MOJob job = jobService.getPublishedJob(jobId);
        if (job == null) {
            throw new IllegalStateException("This position has not been published yet and cannot be viewed or applied for.");
        }
    }

    /**
     * 取消申请
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
            common.entity.UserRole.TA,
            "Application Cancelled",
            "Your application #" + saved.getApplicationId() + " has been cancelled successfully.",
            "APPLICATION_CANCELLED"
        );

        return saved;
    }

    /**
     * MO将申请设为候补
     */
    public TAApplication markAsWaitlisted(Long applicationId) {
        for (TAApplication application : dao.findAll()) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {
                application.setStatus(ApplicationStatus.WAITLISTED);
                TAApplication saved = dao.save(application);
                notificationService.notifyUser(
                        saved.getTaUserId(),
                        common.entity.UserRole.TA,
                        "Waitlisted",
                        "Your application #" + saved.getApplicationId() + " is now waitlisted.",
                        NotificationKind.WAITLISTED
                );
                return saved;
            }
        }
        throw new IllegalArgumentException("Application not found.");
    }

    /**
     * MO设为已录用（发Offer前）
     */
    public TAApplication markAsAccepted(Long applicationId) {
        for (TAApplication application : dao.findAll()) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {
                application.setStatus(ApplicationStatus.ACCEPTED);
                TAApplication saved = dao.save(application);
                return saved;
            }
        }
        throw new IllegalArgumentException("Application not found.");
    }

    /**
     * TA接受Offer后设为已录用
     */
    public TAApplication markAsHired(Long applicationId) {
        for (TAApplication application : dao.findAll()) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {
                application.setStatus(ApplicationStatus.HIRED);
                TAApplication saved = dao.save(application);
                notificationService.notifyUser(
                        saved.getTaUserId(),
                        common.entity.UserRole.TA,
                        "Application Result",
                        "Your application #" + saved.getApplicationId() + " has been approved.",
                        NotificationKind.RESULT
                );
                return saved;
            }
        }
        throw new IllegalArgumentException("Application not found.");
    }

    /**
     * MO拒绝申请
     */
    public TAApplication rejectApplication(Long applicationId) {
        for (TAApplication application : dao.findAll()) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {
                application.setStatus(ApplicationStatus.REJECTED);
                TAApplication saved = dao.save(application);
                notificationService.notifyUser(
                        saved.getTaUserId(),
                        common.entity.UserRole.TA,
                        "Application Result",
                        "Your application #" + saved.getApplicationId() + " has been rejected.",
                        NotificationKind.RESULT
                );
                return saved;
            }
        }
        throw new IllegalArgumentException("Application not found.");
    }

    public String buildApplicationSummary(TAApplication application) {
        if (application == null) {
            return "No application selected.";
        }
        return "Application ID: " + application.getApplicationId()
                + "\nJob ID: " + application.getJobId()
                + "\nStatus: " + ApplicationStatus.getDisplayText(application.getStatus())
                + "\nStatement: " + application.getStatement();
    }
}