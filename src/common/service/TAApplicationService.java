package common.service;

import common.dao.TAApplicationDAO;
import common.entity.MOJob;
import common.entity.TAApplication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TAApplicationService {
    private final TAApplicationDAO dao = new TAApplicationDAO();
    private final MOJobService jobService = new MOJobService();
    private final ta.service.TAProfileService taProfileService = new ta.service.TAProfileService();
    private final ta.service.CVService cvService = new ta.service.CVService();
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

    public TAApplication markAsHired(Long applicationId) {
        List<TAApplication> all = dao.findAll();
        for (TAApplication application : all) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {
                application.setStatus("HIRED");
                TAApplication saved = dao.save(application);
                notificationService.notifyUser(
                        saved.getTaUserId(),
                        common.entity.UserRole.TA,
                        "Application Result",
                        "Your application #" + saved.getApplicationId() + " has been approved.",
                        "APPLICATION_APPROVED"
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
                application.setStatus("REJECTED");
                TAApplication saved = dao.save(application);
                notificationService.notifyUser(
                        saved.getTaUserId(),
                        common.entity.UserRole.TA,
                        "Application Result",
                        "Your application #" + saved.getApplicationId() + " has been rejected.",
                        "APPLICATION_REJECTED"
                );
                return saved;
            }
        }
        throw new IllegalArgumentException("Application not found.");
    }

    /***
     * 
     * The "submitApplication" function and validation logic have been added.
     *  If the TA's information is incomplete or a CV has not been uploaded, the application will be directly blocked. 
     * At the same time, positions that have not been published will also be blocked from viewing or applying.
     */

    public TAApplication submitApplication(Long taUserId, Long jobId, String statement) {
        validateApplicationAccess(taUserId, jobId);

        TAApplication application = new TAApplication();
        application.setTaUserId(taUserId);
        application.setJobId(jobId);
        application.setStatement(statement);
        application.setStatus("SUBMITTED");
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
