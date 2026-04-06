package ta.service;
//package common.service;

import ta.dao.TAApplicationDAO;

import common.domain.ApplicationStatus;
import common.domain.NotificationKind;
import common.entity.MOJob;

import ta.entity.TAApplication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import common.service.MOJobService;
import common.service.NotificationService;
 

public class TAApplicationService {

    private final TAApplicationDAO dao = new TAApplicationDAO();


    private final MOJobService jobService = new MOJobService();
    private final TAProfileService taProfileService = new TAProfileService();
    private final CVService cvService = new CVService();
    private final common.service.NotificationService notificationService = new NotificationService();


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

    /** MO inbox: {@link ApplicationStatus#PENDING_REVIEW} and legacy {@link ApplicationStatus#SUBMITTED}. */
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
     * Iteration 2 hook: MO sets waitlist → TA sees {@link NotificationKind#WAITLISTED}.
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

    public TAApplication markAsHired(Long applicationId) {
        List<TAApplication> all = dao.findAll();
        for (TAApplication application : all) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {

                //application.setStatus("HIRED");
                //return dao.save(application);

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

    public TAApplication rejectApplication(Long applicationId) {
        List<TAApplication> all = dao.findAll();
        for (TAApplication application : all) {
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

    public TAApplication submitApplication(Long taUserId, Long jobId, String statement) {
        validateApplicationAccess(taUserId, jobId);

        TAApplication application = new TAApplication();
        application.setTaUserId(taUserId);
        application.setJobId(jobId);
        application.setStatement(statement);
        application.setStatus(ApplicationStatus.SUBMITTED);
        return createOrUpdate(application);
    }

    public void validateApplicationAccess(Long taUserId, Long jobId) {
        if (!taProfileService.isProfileComplete(taUserId) || !cvService.hasCV(taUserId)) {
            throw new IllegalStateException("Please complete your TA profile and upload a CV before applying.");
        }

        MOJob job = jobService.getPublishedJob(jobId);
        if (job == null) {
            throw new IllegalStateException("This position has not been published yet and cannot be viewed or applied for.");
        }

        for (TAApplication existing : dao.findAll()) {
            if (taUserId != null && taUserId.equals(existing.getTaUserId())
                    && jobId != null && jobId.equals(existing.getJobId())) {
                throw new IllegalStateException("You have already applied for this position.");
            }
        }
    }

    public String buildApplicationSummary(TAApplication application) {
        if (application == null) {
            return "No application selected.";
        }
        return "Application ID: " + application.getApplicationId()
                + "\nJob ID: " + application.getJobId()
                + "\nStatus: " + application.getStatus()
                + "\nStatement: " + application.getStatement();
    }

}

