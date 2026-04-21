package auth;

import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import common.domain.ApplicationStatus;
import common.entity.MOJob;
import common.entity.User;
import common.service.MOJobService;
import common.service.NotificationService;
import common.ui.NotificationPopup;
import ta.entity.TAApplication;
import ta.service.TAApplicationService;

public class MOHomeFrame extends JFrame {
    private final User currentUser;
    private final MOJobService jobService = new MOJobService();
    private final TAApplicationService applicationService = new TAApplicationService();
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
            MOJob job = new MOJob();
            job.setMoUserId(currentUser.getUserId());
            job.setModuleCode("EBU6304");
            job.setTitle("Teaching Assistant");
            job.setDescription("Support labs and coursework marking.\nDeadline: 2026-12-31");
            job.setWeeklyHours(6);
            job.setStatus("DRAFT");
            // 设置截止日期
            job.setApplicationDeadline(LocalDateTime.now().plusDays(30));
            MOJob savedJob = jobService.createOrUpdate(job);
            JOptionPane.showMessageDialog(this, "Draft job saved.\nJob ID: " + savedJob.getJobId());
        });

        JButton publishBtn = new JButton("Publish Latest Job");
        publishBtn.addActionListener(e -> {
            List<MOJob> jobs = jobService.listAll();
            if (jobs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No job found to publish.");
                return;
            }
            MOJob latest = jobs.get(jobs.size() - 1);
            jobService.publishJob(latest.getJobId());
            JOptionPane.showMessageDialog(this, "Job published.\nJob ID: " + latest.getJobId());
        });

        JButton hireBtn = new JButton("Accept & Send Offer to First TA");
        hireBtn.addActionListener(e -> {
            // 查找 SUBMITTED 状态的申请
            List<TAApplication> submitted = applicationService.listByStatus(ApplicationStatus.SUBMITTED);
            if (submitted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No submitted applications.");
                return;
            }

            TAApplication target = submitted.get(0);
            
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
            List<TAApplication> submitted = applicationService.listByStatus(ApplicationStatus.SUBMITTED);
            if (submitted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No submitted applications.");
                return;
            }
            TAApplication target = submitted.get(0);
            applicationService.rejectApplication(target.getApplicationId());
            JOptionPane.showMessageDialog(this, 
                "Application rejected.\nApplication ID: " + target.getApplicationId());
        });

        JButton waitlistBtn = new JButton("Waitlist first submitted TA");
        waitlistBtn.addActionListener(e -> {
            List<TAApplication> submitted = applicationService.listByStatus(ApplicationStatus.SUBMITTED);
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
            List<TAApplication> waitlisted = applicationService.listByStatus(ApplicationStatus.WAITLISTED);
            if (waitlisted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No waitlisted applications.");
                return;
            }
            TAApplication target = waitlisted.get(0);
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