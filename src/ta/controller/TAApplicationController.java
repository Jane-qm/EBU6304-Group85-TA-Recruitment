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

/**
 * TA 申请控制器（包含 Offer 功能）
 * 
 * @version 3.0 - 合并 Offer 功能
 */
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
    
    // ==================== 查询方法 ====================
    
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
                    return ApplicationStatus.isActive(status) || 
                           ApplicationStatus.isRejected(status) ||
                           ApplicationStatus.isHired(status);
                })
                .map(TAApplication::getJobId)
                .collect(Collectors.toList());
        
        return allJobs.stream()
                .filter(job -> !excludedJobIds.contains(job.getJobId()))
                .filter(job -> job.getApplicationDeadline() == null || 
                        !java.time.LocalDateTime.now().isAfter(job.getApplicationDeadline()))
                .collect(Collectors.toList());
    }
    
    // ==================== 申请提交 ====================
    
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
    
    // ==================== 取消申请 ====================
    
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
                "Only submitted or waitlisted applications can be cancelled.",
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
    
    // ==================== Offer 响应方法 ====================
    
    /**
     * TA 接受 Offer
     */
    public boolean acceptOfferWithFeedback(Long applicationId, JFrame parent) {
        TAApplication app = applicationService.findById(applicationId);
        if (app == null) {
            JOptionPane.showMessageDialog(parent, "Application not found.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!ApplicationStatus.OFFER_SENT.equals(app.getStatus())) {
            JOptionPane.showMessageDialog(parent, 
                "Cannot accept offer. Application status is: " + getDisplayStatus(app),
                "Cannot Accept", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (app.isOfferExpired()) {
            JOptionPane.showMessageDialog(parent, 
                "This offer has expired. You cannot accept it.",
                "Offer Expired", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        int confirm = JOptionPane.showConfirmDialog(parent,
            "Do you want to ACCEPT this offer?\n\n" +
            "Course: " + getCourseName(app.getJobId()) + "\n" +
            "Offered Hours: " + app.getOfferedHours() + " hours/week\n\n" +
            "This action cannot be undone.",
            "Confirm Accept Offer",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }
        
        try {
            applicationService.acceptOffer(applicationId);
            JOptionPane.showMessageDialog(parent, 
                "You have accepted the offer! Congratulations!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "System error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * TA 拒绝 Offer
     */
    public boolean rejectOfferWithFeedback(Long applicationId, JFrame parent) {
        TAApplication app = applicationService.findById(applicationId);
        if (app == null) {
            JOptionPane.showMessageDialog(parent, "Application not found.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!ApplicationStatus.OFFER_SENT.equals(app.getStatus())) {
            JOptionPane.showMessageDialog(parent, 
                "Cannot reject offer. Application status is: " + getDisplayStatus(app),
                "Cannot Reject", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (app.isOfferExpired()) {
            JOptionPane.showMessageDialog(parent, 
                "This offer has expired.",
                "Offer Expired", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        int confirm = JOptionPane.showConfirmDialog(parent,
            "Are you sure you want to REJECT this offer?\n\n" +
            "Course: " + getCourseName(app.getJobId()) + "\n" +
            "This action cannot be undone.",
            "Confirm Reject Offer",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }
        
        try {
            applicationService.rejectOffer(applicationId);
            JOptionPane.showMessageDialog(parent, 
                "You have rejected the offer.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "System error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // ==================== 统计方法 ====================
    
    public ApplicationStats getApplicationStats(Long taUserId) {
        List<TAApplication> applications = applicationService.listByTaUserId(taUserId);
        
        long hired = applications.stream()
                .filter(a -> ApplicationStatus.isHired(a.getStatus()))
                .count();
        long pending = applications.stream()
                .filter(a -> ApplicationStatus.SUBMITTED.equals(a.getStatus()) || 
                             ApplicationStatus.WAITLISTED.equals(a.getStatus()))
                .count();
        long offerSent = applications.stream()
                .filter(a -> ApplicationStatus.OFFER_SENT.equals(a.getStatus()))
                .count();
        long rejected = applications.stream()
                .filter(a -> ApplicationStatus.isRejected(a.getStatus()))
                .count();
        
        return new ApplicationStats(hired, pending, offerSent, rejected);
    }
    
    // ==================== 辅助方法 ====================
    
    public String getDisplayStatus(TAApplication application) {
        return ApplicationStatus.getDisplayText(application.getStatus());
    }
    
    public String getShortDisplayStatus(TAApplication application) {
        return ApplicationStatus.getShortDisplayText(application.getStatus());
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
    
    // ==================== 内部类 ====================
    
    public static class ApplicationStats {
        public final long hired;
        public final long pending;
        public final long offerSent;
        public final long rejected;
        
        public ApplicationStats(long hired, long pending, long offerSent, long rejected) {
            this.hired = hired;
            this.pending = pending;
            this.offerSent = offerSent;
            this.rejected = rejected;
        }
        
        public long getTotal() {
            return hired + pending + offerSent + rejected;
        }
    }
}