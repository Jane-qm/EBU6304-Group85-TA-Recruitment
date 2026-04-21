package ta.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.domain.ApplicationStatus;
import common.domain.NotificationKind;
import common.entity.MOJob;
import common.entity.SystemConfig;
import common.entity.UserRole;
import common.service.MOJobService;
import common.service.NotificationService;
import common.service.SystemConfigService;
import ta.dao.TAApplicationDAO;
import ta.entity.CVInfo;
import ta.entity.TAApplication;
import ta.entity.TAProfile;

/**
 * TA 申请业务服务（包含 Offer 功能）
 * 
 * @version 3.0 - 合并 Offer 功能
 */
public class TAApplicationService {

    private final TAApplicationDAO dao = new TAApplicationDAO();
    private final MOJobService jobService = new MOJobService();
    private final TAProfileService taProfileService = new TAProfileService();
    private final CVService cvService = new CVService();
    private final NotificationService notificationService = new NotificationService();
    private final SystemConfigService systemConfigService = new SystemConfigService();

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

    public List<TAApplication> listByTaUserId(Long taUserId) {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication application : dao.findAll()) {
            if (taUserId != null && taUserId.equals(application.getTaUserId())) {
                result.add(application);
            }
        }
        return result;
    }

    public List<TAApplication> listByJobId(Long jobId) {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication application : dao.findAll()) {
            if (jobId != null && jobId.equals(application.getJobId())) {
                result.add(application);
            }
        }
        return result;
    }

    public TAApplication findById(Long applicationId) {
        for (TAApplication application : dao.findAll()) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {
                return application;
            }
        }
        return null;
    }

    public List<TAApplication> listByTaUserIdSorted(Long taUserId) {
        List<TAApplication> result = listByTaUserId(taUserId);
        result.sort((a, b) -> {
            if (a.getAppliedAt() == null) return 1;
            if (b.getAppliedAt() == null) return -1;
            return b.getAppliedAt().compareTo(a.getAppliedAt());
        });
        return result;
    }

    /**
     * 获取活跃申请数量（计入 3 个上限）
     */
    public int getActiveApplicationCount(Long taUserId) {
        return (int) listByTaUserId(taUserId).stream()
                .filter(a -> ApplicationStatus.isActive(a.getStatus()))
                .count();
    }

    // ==================== 申请提交 ====================

    private void refreshProfileCompletion(Long taUserId) {
        taProfileService.refreshProfile(taUserId);
        TAProfile profile = taProfileService.getProfileByTaId(taUserId);
        if (profile != null) {
            profile.saveProfile();
            taProfileService.saveProfile(profile);
        }
    }

    /**
     * 检查申请是否可提交
     */
    public void validateApplicationAccess(Long taUserId, Long jobId) {
        refreshProfileCompletion(taUserId);

        if (!taProfileService.isProfileComplete(taUserId)) {
            throw new IllegalStateException("Please complete your TA profile before applying.");
        }
        if (!cvService.hasCV(taUserId)) {
            throw new IllegalStateException("Please upload a CV before applying.");
        }

        MOJob job = jobService.getPublishedJob(jobId);
        if (job == null) {
            throw new IllegalStateException("This position is not available for application.");
        }

        // 检查职位截止日期
        if (job.getApplicationDeadline() != null && 
            LocalDateTime.now().isAfter(job.getApplicationDeadline())) {
            throw new IllegalStateException("The application deadline for this position has passed.");
        }
    }

    /**
     * 提交申请
     */
    public TAApplication submitApplication(Long taUserId, Long jobId, String statement, Long cvId) {
        // 检查全局申请周期
        if (!systemConfigService.isWithinApplicationCycle(LocalDateTime.now())) {
            SystemConfig cfg = systemConfigService.getConfig();
            String window = cfg.isConfigured()
                    ? cfg.getApplicationStart().toLocalDate() + " to " + cfg.getApplicationEnd().toLocalDate()
                    : "not yet configured";
            throw new IllegalStateException(
                    "Applications are currently closed. Recruitment window: " + window + ".");
        }

        refreshProfileCompletion(taUserId);

        if (!taProfileService.isProfileComplete(taUserId)) {
            List<String> missing = taProfileService.getMissingFields(taUserId);
            String missingMsg = missing.isEmpty() ? "" : " Missing: " + String.join(", ", missing);
            throw new IllegalStateException("Please complete your TA profile before applying." + missingMsg);
        }

        CVInfo selectedCV = cvService.getCVById(taUserId, cvId);
        if (selectedCV == null) {
            throw new IllegalArgumentException("Selected CV not found. Please upload a CV first.");
        }

        MOJob job = jobService.getPublishedJob(jobId);
        if (job == null) {
            throw new IllegalStateException("This position is not available for application.");
        }

        // 检查职位截止日期
        if (job.getApplicationDeadline() != null && 
            LocalDateTime.now().isAfter(job.getApplicationDeadline())) {
            throw new IllegalStateException("The application deadline for this position has passed.");
        }

        List<TAApplication> userApplications = listByTaUserId(taUserId);

        // 检查是否已有活跃申请
        boolean hasActiveApplication = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        ApplicationStatus.isActive(a.getStatus()));
        if (hasActiveApplication) {
            throw new IllegalStateException("You already have an active application for this position.");
        }

        // 检查是否曾被拒绝
        boolean wasRejected = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        ApplicationStatus.isRejected(a.getStatus()));
        if (wasRejected) {
            throw new IllegalStateException("Your previous application for this position was rejected. You cannot reapply.");
        }

        // 检查是否已录用
        boolean wasHired = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        ApplicationStatus.isHired(a.getStatus()));
        if (wasHired) {
            throw new IllegalStateException("You have already been hired for this position.");
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
        application.setCvId(cvId);

        return createOrUpdate(application);
    }

    // ==================== 取消申请 ====================

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
                ". Only submitted or waitlisted applications can be cancelled."
            );
        }

        // 检查职位截止日期
        MOJob job = jobService.getJobById(application.getJobId());
        if (job != null && job.getApplicationDeadline() != null && 
            LocalDateTime.now().isAfter(job.getApplicationDeadline())) {
            throw new IllegalStateException("Cannot cancel after the application deadline.");
        }

        application.setStatus(ApplicationStatus.CANCELLED);
        TAApplication saved = dao.save(application);

        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "Application Cancelled",
            "Your application has been cancelled successfully.",
            "APPLICATION_CANCELLED"
        );

        return saved;
    }

    // ==================== Offer 相关方法 ====================

    /**
     * MO 发送 Offer（接受申请并发 Offer）
     * 
     * @param applicationId 申请 ID
     * @param offeredHours 提供的工时
     * @param expiryDays 有效期天数（从发送时间开始计算）
     * @return 更新后的申请
     */
    public TAApplication sendOffer(Long applicationId, int offeredHours, int expiryDays) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }

        if (!ApplicationStatus.SUBMITTED.equals(application.getStatus()) && 
            !ApplicationStatus.WAITLISTED.equals(application.getStatus())) {
            throw new IllegalStateException(
                "Cannot send offer. Application must be in SUBMITTED or WAITLISTED status. Current: " + 
                application.getStatus()
            );
        }

        if (offeredHours <= 0) {
            throw new IllegalArgumentException("Offered hours must be positive.");
        }
        if (expiryDays <= 0) {
            throw new IllegalArgumentException("Expiry days must be positive.");
        }

        LocalDateTime now = LocalDateTime.now();
        application.setStatus(ApplicationStatus.OFFER_SENT);
        application.setOfferedHours(offeredHours);
        application.setOfferSentAt(now);
        application.setOfferExpiryAt(now.plusDays(expiryDays));
        application.setRespondedAt(null);

        TAApplication saved = dao.save(application);

        // 发送通知给 TA
        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "New Offer Received",
            "You have received an offer for " + getJobModuleCode(saved.getJobId()) + 
            " with " + offeredHours + " hours/week. Please respond before " + 
            saved.getOfferExpiryAt().toLocalDate() + ".",
            NotificationKind.OFFER_RESPONSE
        );

        return saved;
    }

    /**
     * TA 接受 Offer
     */
    public TAApplication acceptOffer(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }

        if (!ApplicationStatus.OFFER_SENT.equals(application.getStatus())) {
            throw new IllegalStateException(
                "Cannot accept offer. Application must be in OFFER_SENT status. Current: " + 
                application.getStatus()
            );
        }

        if (application.isOfferExpired()) {
            application.setStatus(ApplicationStatus.EXPIRED);
            dao.save(application);
            throw new IllegalStateException("This offer has expired. You cannot accept it.");
        }

        application.setStatus(ApplicationStatus.HIRED);
        application.setRespondedAt(LocalDateTime.now());
        TAApplication saved = dao.save(application);

        // 通知 MO
        MOJob job = jobService.getJobById(application.getJobId());
        if (job != null) {
            notificationService.notifyUser(
                job.getMoUserId(),
                UserRole.MO,
                "Offer Accepted",
                "TA has accepted the offer for " + job.getModuleCode() + " - " + job.getTitle(),
                NotificationKind.OFFER_RESPONSE
            );
        }

        // 通知 TA
        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "Offer Accepted",
            "Congratulations! You have accepted the offer and been hired.",
            NotificationKind.OFFER_RESPONSE
        );

        return saved;
    }

    /**
     * TA 拒绝 Offer
     */
    public TAApplication rejectOffer(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }

        if (!ApplicationStatus.OFFER_SENT.equals(application.getStatus())) {
            throw new IllegalStateException(
                "Cannot reject offer. Application must be in OFFER_SENT status. Current: " + 
                application.getStatus()
            );
        }

        if (application.isOfferExpired()) {
            application.setStatus(ApplicationStatus.EXPIRED);
            dao.save(application);
            throw new IllegalStateException("This offer has expired. You cannot reject it.");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRespondedAt(LocalDateTime.now());
        TAApplication saved = dao.save(application);

        // 通知 MO
        MOJob job = jobService.getJobById(application.getJobId());
        if (job != null) {
            notificationService.notifyUser(
                job.getMoUserId(),
                UserRole.MO,
                "Offer Declined",
                "TA has declined the offer for " + job.getModuleCode() + " - " + job.getTitle() +
                ". You may select another candidate from the waitlist.",
                NotificationKind.OFFER_RESPONSE
            );
        }

        return saved;
    }

    /**
     * 自动过期 Offer（定时任务调用）
     */
    public int autoExpireOffers() {
        int expiredCount = 0;
        for (TAApplication application : dao.findAll()) {
            if (ApplicationStatus.OFFER_SENT.equals(application.getStatus()) && 
                application.isOfferExpired()) {
                application.setStatus(ApplicationStatus.EXPIRED);
                dao.save(application);
                expiredCount++;
                
                // 通知 TA
                notificationService.notifyUser(
                    application.getTaUserId(),
                    UserRole.TA,
                    "Offer Expired",
                    "The offer for your application has expired.",
                    "OFFER_EXPIRED"
                );
            }
        }
        return expiredCount;
    }

    /**
     * 处理职位关闭后的申请
     */
    public int processExpiredApplicationsForJob(Long jobId) {
        int processedCount = 0;
        for (TAApplication application : listByJobId(jobId)) {
            String status = application.getStatus();
            if (ApplicationStatus.SUBMITTED.equals(status) || 
                ApplicationStatus.WAITLISTED.equals(status)) {
                application.setStatus(ApplicationStatus.EXPIRED);
                dao.save(application);
                processedCount++;
            }
        }
        return processedCount;
    }

    // ==================== MO 审核方法 ====================

    /**
     * MO 将申请标记为候选
     */
    public TAApplication markAsWaitlisted(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        
        if (!ApplicationStatus.SUBMITTED.equals(application.getStatus())) {
            throw new IllegalStateException(
                "Cannot mark as waitlisted. Application must be in SUBMITTED status."
            );
        }
        
        application.setStatus(ApplicationStatus.WAITLISTED);
        TAApplication saved = dao.save(application);
        
        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "Waitlisted",
            "Your application has been placed on the waitlist.",
            NotificationKind.WAITLISTED
        );
        return saved;
    }

    /**
     * MO 拒绝申请
     */
    public TAApplication rejectApplication(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        
        if (!ApplicationStatus.SUBMITTED.equals(application.getStatus()) && 
            !ApplicationStatus.WAITLISTED.equals(application.getStatus())) {
            throw new IllegalStateException(
                "Cannot reject application. Application must be in SUBMITTED or WAITLISTED status."
            );
        }
        
        application.setStatus(ApplicationStatus.REJECTED);
        TAApplication saved = dao.save(application);
        
        notificationService.notifyUser(
            saved.getTaUserId(),
            UserRole.TA,
            "Application Result",
            "Your application has been rejected.",
            NotificationKind.RESULT
        );
        return saved;
    }

    // 添加到 TAApplicationService 类中

/**
 * 根据状态获取申请列表
 */
public List<TAApplication> listByStatus(String status) {
    List<TAApplication> result = new ArrayList<>();
    for (TAApplication application : dao.findAll()) {
        if (application.getStatus() != null && application.getStatus().equals(status)) {
            result.add(application);
        }
    }
    return result;
}

/**
 * 根据职位 ID 获取候选名单中的申请
 */
public List<TAApplication> listWaitlistedByJobId(Long jobId) {
    List<TAApplication> result = new ArrayList<>();
    for (TAApplication app : listByJobId(jobId)) {
        if (ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
            result.add(app);
        }
    }
    result.sort((a, b) -> {
        if (a.getAppliedAt() == null) return 1;
        if (b.getAppliedAt() == null) return -1;
        return a.getAppliedAt().compareTo(b.getAppliedAt());
    });
    return result;
}

/**
 * 获取待审核的申请（SUBMITTED 状态）
 */
public List<TAApplication> listApplicationsAwaitingReview() {
    return listByStatus(ApplicationStatus.SUBMITTED);
}

    // ==================== 辅助方法 ====================

    private String getJobModuleCode(Long jobId) {
        MOJob job = jobService.getJobById(jobId);
        return job != null ? job.getModuleCode() : "Unknown";
    }

    public String buildApplicationSummary(TAApplication application) {
        if (application == null) {
            return "No application selected.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Application ID: ").append(application.getApplicationId());
        sb.append("\nStatus: ").append(ApplicationStatus.getDisplayText(application.getStatus()));
        
        if (application.getOfferedHours() != null) {
            sb.append("\nOffered Hours: ").append(application.getOfferedHours()).append(" hours/week");
        }
        if (application.getOfferExpiryAt() != null) {
            sb.append("\nOffer Expires: ").append(application.getOfferExpiryAt().toLocalDate());
        }
        sb.append("\nStatement: ").append(application.getStatement());
        
        return sb.toString();
    }
}