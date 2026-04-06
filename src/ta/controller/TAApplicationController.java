// ta/controller/TAApplicationController.java
package ta.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import common.entity.MOJob;
import common.service.MOJobService;
import common.service.NotificationService;
import common.domain.ApplicationStatus;
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
    
    /**
     * 获取 TA 的所有申请记录
     */
    public List<TAApplication> getMyApplications(Long taUserId) {
        return applicationService.listByTaUserIdSorted(taUserId);
    }
    
    /**
     * 获取活跃申请数量（待审核状态）
     */
    public int getActiveApplicationCount(Long taUserId) {
        return applicationService.getActiveApplicationCount(taUserId);
    }
    
    /**
     * 检查是否可以申请更多职位
     */
    public boolean canSubmitMoreApplications(Long taUserId) {
        return getActiveApplicationCount(taUserId) < MAX_ACTIVE_APPLICATIONS;
    }
    
    /**
     * 获取剩余可申请数量
     */
    public int getRemainingApplicationSlots(Long taUserId) {
        return MAX_ACTIVE_APPLICATIONS - getActiveApplicationCount(taUserId);
    }
    
    /**
     * 获取最大申请数量限制
     */
    public int getMaxActiveApplications() {
        return MAX_ACTIVE_APPLICATIONS;
    }
    
    /**
     * 获取已发布的职位列表
     */
    public List<MOJob> getPublishedJobs() {
        return jobService.listPublishedJobs();
    }
    
    /**
     * 获取可申请的职位（TA 未申请过的）
     */
    public List<MOJob> getAvailableJobs(Long taUserId) {
        List<MOJob> allJobs = jobService.listPublishedJobs();
        List<Long> appliedJobIds = applicationService.listByTaUserId(taUserId).stream()
                .map(TAApplication::getJobId)
                .collect(Collectors.toList());
        
        return allJobs.stream()
                .filter(job -> !appliedJobIds.contains(job.getJobId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 提交申请（带用户反馈和限制检查）
     */
    public boolean submitApplicationWithFeedback(Long taUserId, Long jobId, String statement, JFrame parent) {
        // 检查申请数量限制
        if (!canSubmitMoreApplications(taUserId)) {
            JOptionPane.showMessageDialog(parent, 
                "You can only have " + MAX_ACTIVE_APPLICATIONS + " active applications at once.\n" +
                "Please wait for a decision on your existing applications before applying for more.",
                "Application Limit Reached", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            TAApplication application = applicationService.submitApplication(taUserId, jobId, statement);
            JOptionPane.showMessageDialog(parent, 
                "Application submitted successfully!\n\n" + buildApplicationSummary(application),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Cannot Apply", JOptionPane.WARNING_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "System error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * 取消申请（带用户反馈）
     */
    public boolean cancelApplicationWithFeedback(Long applicationId, JFrame parent) {
        TAApplication app = applicationService.findById(applicationId);
        if (app == null) {
            JOptionPane.showMessageDialog(parent, "Application not found.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 检查状态是否可取消
        if (!ApplicationStatus.isCancellable(app.getStatus())) {
            String displayStatus = getDisplayStatus(app);
            JOptionPane.showMessageDialog(parent, 
                "Cannot cancel application in status: " + displayStatus + "\n" +
                "Only pending or waitlisted applications can be cancelled.",
                "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 确认取消
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
    
    /**
     * 获取申请统计
     */
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
    
    /**
     * 获取申请显示状态（完善版）
     */
    public String getDisplayStatus(TAApplication application) {
        return ApplicationStatus.getDisplayText(application.getStatus());
    }
    
    /**
     * 获取反馈信息（完善版）
     */
    public String getFeedbackMessage(TAApplication application) {
        return ApplicationStatus.getFeedbackMessage(application.getStatus());
    }
    
    /**
     * 获取课程名称
     */
    private String getCourseName(Long jobId) {
        List<MOJob> jobs = jobService.listPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return "Course #" + jobId;
    }
    
    /**
     * 构建申请摘要
     */
    public String buildApplicationSummary(TAApplication application) {
        return applicationService.buildApplicationSummary(application);
    }
    
    /**
     * 获取未读通知数量
     */
    public int getUnreadNotificationCount(Long userId) {
        return notificationService.listUnreadByUser(userId).size();
    }
    
    /**
     * 申请统计内部类
     */
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