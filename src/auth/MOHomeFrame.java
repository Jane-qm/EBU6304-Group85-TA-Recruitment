package auth;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;              // 改为 ta.entity
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;      // 改为 ta.service

import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.User;
import common.service.MOJobService;
import common.service.MOOfferService;
import common.service.NotificationService;
import common.ui.NotificationPopup;
import ta.entity.TAApplication;
import ta.service.TAApplicationService;

public class MOHomeFrame extends JFrame {
    private final User currentUser;
    private final MOJobService jobService = new MOJobService();
    private final MOOfferService offerService = new MOOfferService();
    private final TAApplicationService applicationService = new TAApplicationService();
    private final NotificationService notificationService = new NotificationService();

    public MOHomeFrame(User user) {
        this.currentUser = user;
        setTitle("MO Home");
        setSize(640, 420);
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
            job.setDescription("Support labs and coursework marking.");
            job.setWeeklyHours(6);
            job.setStatus("DRAFT");
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

        JButton offerBtn = new JButton("Create Demo Offer");
        offerBtn.addActionListener(e -> {
            List<MOJob> jobs = jobService.listAll();
            if (jobs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please create and publish a job first.");
                return;
            }
            MOOffer offer = new MOOffer();
            offer.setMoUserId(currentUser.getUserId());
            offer.setTaUserId(100001L);
            offer.setModuleCode(jobs.get(0).getModuleCode());
            offer.setOfferedHours(6);
            offerService.sendOffer(offer);
            JOptionPane.showMessageDialog(this, "MO offer saved and TA notified.");
        });

        JButton hireBtn = new JButton("Hire First Submitted TA");
        hireBtn.addActionListener(e -> {
            List<TAApplication> submitted = applicationService.listApplicationsAwaitingReview();
            if (submitted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No applications awaiting review.");
                return;
            }

            TAApplication target = submitted.get(0);
            applicationService.markAsHired(target.getApplicationId());

            MOOffer offer = new MOOffer();
            offer.setMoUserId(currentUser.getUserId());
            offer.setTaUserId(target.getTaUserId());
            offer.setApplicationId(target.getApplicationId());
            offer.setModuleCode("EBU6304");
            offer.setOfferedHours(6);
            offerService.sendOffer(offer);
            JOptionPane.showMessageDialog(this, "TA hired and offer sent.\nApplication ID: " + target.getApplicationId());
        });

        JButton rejectBtn = new JButton("Reject First Submitted TA");
        rejectBtn.addActionListener(e -> {
            List<TAApplication> submitted = applicationService.listApplicationsAwaitingReview();
            if (submitted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No applications awaiting review.");
                return;
            }
            TAApplication target = submitted.get(0);
            applicationService.rejectApplication(target.getApplicationId());
            JOptionPane.showMessageDialog(this, "Application rejected and TA notified.\nApplication ID: " + target.getApplicationId());
        });

        JButton waitlistBtn = new JButton("Waitlist first awaiting (demo)");
        waitlistBtn.addActionListener(e -> {
            List<TAApplication> submitted = applicationService.listApplicationsAwaitingReview();
            if (submitted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No applications awaiting review.");
                return;
            }
            applicationService.markAsWaitlisted(submitted.get(0).getApplicationId());
            JOptionPane.showMessageDialog(this, "TA waitlisted and notified.");
        });

        JButton notificationsBtn = new JButton("View Notifications");
        notificationsBtn.addActionListener(e ->
                NotificationPopup.showAllNotifications(this, currentUser, notificationService));

        panel.add(jobBtn);
        panel.add(publishBtn);
        panel.add(offerBtn);
        panel.add(hireBtn);
        panel.add(rejectBtn);
        panel.add(waitlistBtn);
        panel.add(notificationsBtn);
        panel.add(logoutBtn);
        setContentPane(panel);
    }
}
