package ui.mo;

import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ui.auth.LoginFrame;
import modules.application.ApplicationStatus;
import modules.job.Job;
import modules.user.User;
import modules.job.JobService;
import modules.notification.NotificationService;
import infrastructure.ui.NotificationPopup;
import modules.application.Application;
import modules.application.ApplicationService;

public class MOHomeFrame extends JFrame {
    private final User currentUser;
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final NotificationService notificationService = new NotificationService();

    public MOHomeFrame(User user) {
        this.currentUser = user;
        setTitle("MO Home");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUi();
    }

    private void initUi() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Welcome, " + currentUser.getEmail() + " (MO)"));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JButton jobBtn = new JButton("Create Demo Job");
        jobBtn.addActionListener(e -> {
            Job job = new Job();
            job.setMoUserId(currentUser.getUserId());
            job.setModuleCode("EBU6304");
            job.setTitle("Teaching Assistant");
            job.setDescription("Support labs and coursework marking.\nDeadline: 2026-12-31");
            job.setWeeklyHours(6);
            job.setStatus("DRAFT");
            // 设置截止日期
            job.setApplicationDeadline(LocalDateTime.now().plusDays(30));
            Job savedJob = jobService.createOrUpdate(job);
            JOptionPane.showMessageDialog(this, "Draft job saved.\nJob ID: " + savedJob.getJobId());
        });

        JButton publishBtn = new JButton("Publish Latest Job");
        publishBtn.addActionListener(e -> {
            List<Job> jobs = jobService.listAll();
            if (jobs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No job found to publish.");
                return;
            }
            Job latest = jobs.get(jobs.size() - 1);
            jobService.publishJob(latest.getJobId());
            JOptionPane.showMessageDialog(this, "Job published.\nJob ID: " + latest.getJobId());
        });

        JButton hireBtn = new JButton("Accept & Send Offer to First TA");
        hireBtn.addActionListener(e -> {
            // 查找 SUBMITTED 状态的申请
            List<Application> submitted = applicationService.listByStatus(ApplicationStatus.SUBMITTED);
            if (submitted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No submitted applications.");
                return;
            }

            Application target = submitted.get(0);
            
            // 发送 Offer（接受申请并发 Offer）
            try {
                applicationService.sendOffer(target.getApplicationId(), 6, 7);
                JOptionPane.showMessageDialog(this, 
                    "Application accepted and offer sent!\n" +
                    "Application ID: " + target.getApplicationId() + "\n" +
                    "Offered Hours: 6 hours/week\n" +
                    "Offer expires in 7 days");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        JButton rejectBtn = new JButton("Reject First Submitted TA");
        rejectBtn.addActionListener(e -> {
            List<Application> submitted = applicationService.listByStatus(ApplicationStatus.SUBMITTED);
            if (submitted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No submitted applications.");
                return;
            }
            Application target = submitted.get(0);
            applicationService.rejectApplication(target.getApplicationId());
            JOptionPane.showMessageDialog(this, 
                "Application rejected.\nApplication ID: " + target.getApplicationId());
        });

        JButton waitlistBtn = new JButton("Waitlist first submitted TA");
        waitlistBtn.addActionListener(e -> {
            List<Application> submitted = applicationService.listByStatus(ApplicationStatus.SUBMITTED);
            if (submitted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No submitted applications.");
                return;
            }
            applicationService.markAsWaitlisted(submitted.get(0).getApplicationId());
            JOptionPane.showMessageDialog(this, "TA waitlisted.");
        });

        JButton hireFromWaitlistBtn = new JButton("Hire from Waitlist (demo)");
        hireFromWaitlistBtn.addActionListener(e -> {
            // 查找 WAITLISTED 状态的申请
            List<Application> waitlisted = applicationService.listByStatus(ApplicationStatus.WAITLISTED);
            if (waitlisted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No waitlisted applications.");
                return;
            }
            Application target = waitlisted.get(0);
            try {
                applicationService.sendOffer(target.getApplicationId(), 6, 7);
                JOptionPane.showMessageDialog(this, 
                    "Waitlisted TA hired and offer sent!\n" +
                    "Application ID: " + target.getApplicationId());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        JButton notificationsBtn = new JButton("View Notifications");
        notificationsBtn.addActionListener(e ->
                NotificationPopup.showAllNotifications(this, currentUser, notificationService));

        panel.add(jobBtn);
        panel.add(publishBtn);
        panel.add(hireBtn);
        panel.add(rejectBtn);
        panel.add(waitlistBtn);
        panel.add(hireFromWaitlistBtn);
        panel.add(notificationsBtn);
        panel.add(logoutBtn);
        setContentPane(panel);
    }
}