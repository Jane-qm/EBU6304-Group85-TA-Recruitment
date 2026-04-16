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

    public int getActiveApplicationCount(Long taUserId) {
        return (int) listByTaUserId(taUserId).stream()
                .filter(a -> ApplicationStatus.isAwaitingReview(a.getStatus()) || 
                             ApplicationStatus.WAITLISTED.equals(a.getStatus()) ||
                             ApplicationStatus.HIRED.equals(a.getStatus()))
                .count();
    }

    // ==================== 查询方法 ====================

    /**
     * Returns all WAITLISTED applications for the given job, sorted by appliedAt
     * ascending so the candidate who waited longest is shown first.
     */
    public List<TAApplication> listWaitlistedByJobId(Long jobId) {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication app : dao.findAll()) {
            if (jobId != null && jobId.equals(app.getJobId())
                    && ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
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

    public List<TAApplication> listApplicationsAwaitingReview() {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication application : dao.findAll()) {
            if (ApplicationStatus.isAwaitingReview(application.getStatus())) {
                result.add(application);
            }
        }
        return result;
    }

    // ==================== 申请提交 ====================
    

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
            throw new IllegalStateException("This position has not been published yet and cannot be viewed or applied for.");
        }
    }
    
   /* *private void refreshProfileCompletion(Long taUserId) {
        TAProfile profile = taProfileService.getProfileByTaId(taUserId);
        if (profile != null) {
            profile.saveProfile();
            taProfileService.saveProfile(profile);
        }
    }*/

private void refreshProfileCompletion(Long taUserId) {
    // 强制从文件重新加载，确保获取最新数据
    taProfileService.refreshProfile(taUserId);
    
    TAProfile profile = taProfileService.getProfileByTaId(taUserId);
    if (profile != null) {
        System.out.println("=== refreshProfileCompletion ===");
        System.out.println("profile.getStudentId(): " + profile.getStudentId());
        System.out.println("profile.getSurname(): " + profile.getSurname());
        System.out.println("profile.getForename(): " + profile.getForename());
        System.out.println("profile.getPhone(): " + profile.getPhone());
        System.out.println("profile.getEmail(): " + profile.getEmail());
        System.out.println("profile.isProfileCompleted(): " + profile.isProfileCompleted());
        
        profile.saveProfile();
        taProfileService.saveProfile(profile);
    } else {
        System.out.println("警告: refreshProfileCompletion 中 profile 为 null");
    }
}
  /* private void refreshProfileCompletion(Long taUserId) {
    TAProfile profile = taProfileService.getProfileByTaId(taUserId);
    if (profile != null) {
        System.out.println("=== refreshProfileCompletion ===");
        System.out.println("profile.getStudentId(): " + profile.getStudentId());
        System.out.println("profile.getSurname(): " + profile.getSurname());
        System.out.println("profile.getForename(): " + profile.getForename());
        System.out.println("profile.getPhone(): " + profile.getPhone());
        System.out.println("profile.getEmail(): " + profile.getEmail());
        
        profile.saveProfile();
        taProfileService.saveProfile(profile);
    }
}*/  
    public TAApplication submitApplication(Long taUserId, Long jobId, String statement, Long cvId) {
        // ADM-003: enforce the global application cycle configured by the Admin
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
            throw new IllegalStateException("This position has not been published yet.");
        }

        List<TAApplication> userApplications = listByTaUserId(taUserId);
        
        boolean hasActiveApplication = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        (ApplicationStatus.isAwaitingReview(a.getStatus()) || 
                         ApplicationStatus.WAITLISTED.equals(a.getStatus())));
        if (hasActiveApplication) {
            throw new IllegalStateException("You already have an active application for this position.");
        }
        
        boolean wasRejected = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        ApplicationStatus.isRejected(a.getStatus()));
        if (wasRejected) {
            throw new IllegalStateException("Your previous application for this position was rejected. You cannot reapply.");
        }

        boolean hasAcceptedOrHired = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        (ApplicationStatus.ACCEPTED.equals(a.getStatus()) || 
                         ApplicationStatus.HIRED.equals(a.getStatus())));
        if (hasAcceptedOrHired) {
            throw new IllegalStateException("You have already been accepted/hired for this position.");
        }

        boolean hasPendingOffer = userApplications.stream()
                .anyMatch(a -> jobId.equals(a.getJobId()) && 
                        ApplicationStatus.ACCEPTED.equals(a.getStatus()));
        if (hasPendingOffer) {
            throw new IllegalStateException("You already have a pending offer for this position. Please accept or reject it first.");
        }

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

    public TAApplication markAsAccepted(Long applicationId) {
        TAApplication application = findById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        
        application.setStatus(ApplicationStatus.ACCEPTED);
        return dao.save(application);
    }

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
}