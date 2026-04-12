package ta.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import common.domain.ApplicationStatus;
import common.entity.MOJob;
import common.service.MOJobService;
import common.service.NotificationService;
import ta.entity.TAApplication;
import ta.service.TAApplicationService;

public class TAApplicationController {
    
    private static final int MAX_ACTIVE_APPLICATIONS = 3;
    
    private final TAApplicationService applicationService;
    private final MOJobService jobService;
    private final NotificationService notificationService;
    
    public TAApplicationController() {
        this.applicationService = new TAApplicationService();
        this.jobService = new MOJobService();
        this.notificationService = new NotificationService();
    }
    
    public List<TAApplication> getMyApplications(Long taUserId) {
        return applicationService.listByTaUserIdSorted(taUserId);
    }
    
    public int getActiveApplicationCount(Long taUserId) {
        return applicationService.getActiveApplicationCount(taUserId);
    }
    
    public boolean canSubmitMoreApplications(Long taUserId) {
        return getActiveApplicationCount(taUserId) < MAX_ACTIVE_APPLICATIONS;
    }
    
    public int getRemainingApplicationSlots(Long taUserId) {
        return MAX_ACTIVE_APPLICATIONS - getActiveApplicationCount(taUserId);
    }
    
    public int getMaxActiveApplications() {
        return MAX_ACTIVE_APPLICATIONS;
    }
    
    public List<MOJob> getPublishedJobs() {
        return jobService.listPublishedJobs();
    }
    
    public List<MOJob> getAvailableJobs(Long taUserId) {
        List<MOJob> allJobs = jobService.listPublishedJobs();
        
        List<TAApplication> applications = applicationService.listByTaUserId(taUserId);
        
        List<Long> excludedJobIds = applications.stream()
                .filter(a -> {
                    String status = a.getStatus();
                    if (ApplicationStatus.isAwaitingReview(status) || 
                        ApplicationStatus.WAITLISTED.equals(status)) {
                        return true;
                    }
                    if (ApplicationStatus.isRejected(status)) {
                        return true;
                    }
                    if (ApplicationStatus.ACCEPTED.equals(status) || 
                        ApplicationStatus.HIRED.equals(status)) {
                        return true;
                    }
                    return false;
                })
                .map(TAApplication::getJobId)
                .collect(Collectors.toList());
        
        return allJobs.stream()
                .filter(job -> !excludedJobIds.contains(job.getJobId()))
                .collect(Collectors.toList());
    }
    
    public boolean submitApplicationWithFeedback(Long taUserId, Long jobId, String statement, Long cvId, JFrame parent) {
        if (!canSubmitMoreApplications(taUserId)) {
            JOptionPane.showMessageDialog(parent, 
                "You can only have " + MAX_ACTIVE_APPLICATIONS + " active applications at once.\n" +
                "Please wait for a decision on your existing applications before applying for more.",
                "Application Limit Reached", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            TAApplication application = applicationService.submitApplication(taUserId, jobId, statement, cvId);
            JOptionPane.showMessageDialog(parent, 
                "Application submitted successfully!\n\n" + buildApplicationSummary(application),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Cannot Apply", JOptionPane.WARNING_MESSAGE);
            return false;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "System error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean cancelApplicationWithFeedback(Long applicationId, JFrame parent) {
        TAApplication app = applicationService.findById(applicationId);
        if (app == null) {
            JOptionPane.showMessageDialog(parent, "Application not found.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!ApplicationStatus.isCancellable(app.getStatus())) {
            String displayStatus = getDisplayStatus(app);
            JOptionPane.showMessageDialog(parent, 
                "Cannot cancel application in status: " + displayStatus + "\n" +
                "Only pending or waitlisted applications can be cancelled.",
                "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int confirm = JOptionPane.showConfirmDialog(parent,
            "Are you sure you want to cancel this application?\n\n" +
            "Course: " + getCourseName(app.getJobId()) + "\n" +
            "Status: " + getDisplayStatus(app),
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        try {
            applicationService.cancelApplication(applicationId);
            JOptionPane.showMessageDialog(parent, 
                "Application cancelled successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), 
                "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "System error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public ApplicationStats getApplicationStats(Long taUserId) {
        List<TAApplication> applications = applicationService.listByTaUserId(taUserId);
        
        long accepted = applications.stream()
                .filter(a -> ApplicationStatus.isHired(a.getStatus()) || ApplicationStatus.isAccepted(a.getStatus()))
                .count();
        long pending = applications.stream()
                .filter(a -> ApplicationStatus.isAwaitingReview(a.getStatus()))
                .count();
        long rejected = applications.stream()
                .filter(a -> ApplicationStatus.isRejected(a.getStatus()))
                .count();
        
        return new ApplicationStats(accepted, pending, rejected);
    }
    
    public String getDisplayStatus(TAApplication application) {
        return ApplicationStatus.getDisplayText(application.getStatus());
    }
    
    public String getFeedbackMessage(TAApplication application) {
        return ApplicationStatus.getFeedbackMessage(application.getStatus());
    }
    
    public String getCourseName(Long jobId) {
        List<MOJob> jobs = jobService.listPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return "Course #" + jobId;
    }
    
    public String getCourseNameByModuleCode(String moduleCode) {
        List<MOJob> jobs = jobService.listPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getModuleCode().equals(moduleCode)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return moduleCode;
    }
    
    public MOJob getJobById(Long jobId) {
        List<MOJob> jobs = jobService.listPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }
    
    public String buildApplicationSummary(TAApplication application) {
        return applicationService.buildApplicationSummary(application);
    }
    
    public int getUnreadNotificationCount(Long userId) {
        return notificationService.listUnreadByUser(userId).size();
    }
    
    public static class ApplicationStats {
        public final long accepted;
        public final long pending;
        public final long rejected;
        
        public ApplicationStats(long accepted, long pending, long rejected) {
            this.accepted = accepted;
            this.pending = pending;
            this.rejected = rejected;
        }
        
        public long getTotal() {
            return accepted + pending + rejected;
        }
    }
}