package auth;

import common.entity.CVInfo;
import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.TAApplication;
import common.entity.TAProfile;
import common.entity.User;
import common.service.CVInfoService;
import common.service.MOJobService;
import common.service.MOOfferService;
import common.service.NotificationService;
import common.service.TAApplicationService;
import common.service.TAProfileService;
import common.ui.NotificationPopup;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TAHomeFrame extends JFrame {
    private final User currentUser;
    private final TAProfileService profileService = new TAProfileService();
    private final CVInfoService cvInfoService = new CVInfoService();
    private final TAApplicationService applicationService = new TAApplicationService();
    private final MOOfferService offerService = new MOOfferService();
    private final MOJobService jobService = new MOJobService();
    private final NotificationService notificationService = new NotificationService();

    public TAHomeFrame(User user) {
        this.currentUser = user;
        setTitle("TA Home");
        setSize(640, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUi();
    }

    private void initUi() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        panel.add(new JLabel("Welcome, " + currentUser.getEmail() + " (TA)"));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JButton profileBtn = new JButton("Create Demo TA Profile");
        profileBtn.addActionListener(e -> {
            TAProfile profile = new TAProfile();
            profile.setUserId(currentUser.getUserId());
            profile.setName("Demo TA");
            profile.setMajor("Software Engineering");
            profile.setGrade("Year 3");
            profile.setAvailableWorkingHours(8);
            profile.setPublished(true);
            profileService.createOrUpdate(profile);
            JOptionPane.showMessageDialog(this, "TA profile saved.");
        });

        JButton cvBtn = new JButton("Create Demo CV");
        cvBtn.addActionListener(e -> {
            CVInfo cvInfo = new CVInfo();
            cvInfo.setUserId(currentUser.getUserId());
            cvInfo.setEducationSummary("BUPT Undergraduate");
            cvInfo.setFilePath("cv-demo.pdf");
            cvInfoService.createOrUpdate(cvInfo);
            JOptionPane.showMessageDialog(this, "CV info saved.");
        });

        JButton applyBtn = new JButton("Create Demo Application");
        applyBtn.addActionListener(e -> {
            try {
                List<MOJob> publishedJobs = jobService.listPublishedJobs();
                if (publishedJobs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No published jobs are available to view or apply.");
                    return;
                }
                TAApplication application = applicationService.submitApplication(
                        currentUser.getUserId(),
                        publishedJobs.get(0).getJobId(),
                        "I am interested in this module."
                );
                JOptionPane.showMessageDialog(this, "Application saved.\nApplication ID: " + application.getApplicationId());
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Permission Blocked", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton offersBtn = new JButton("View My Offers");
        offersBtn.addActionListener(e -> {
            List<MOOffer> offers = offerService.listByTaUserId(currentUser.getUserId());
            if (offers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No offers yet.");
                return;
            }
            MOOffer latest = offers.get(offers.size() - 1);
            JOptionPane.showMessageDialog(
                    this,
                    "Total offers: " + offers.size()
                            + "\nLatest module: " + latest.getModuleCode()
                            + "\nHours: " + latest.getOfferedHours()
                            + "\nStatus: " + latest.getStatus(),
                    "My Offers",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        JButton rejectOfferBtn = new JButton("Reject Latest Offer");
        rejectOfferBtn.addActionListener(e -> {
            List<MOOffer> offers = offerService.listByTaUserId(currentUser.getUserId());
            if (offers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No offers yet.");
                return;
            }
            MOOffer latest = offers.get(offers.size() - 1);
            offerService.rejectOffer(latest.getOfferId());
            JOptionPane.showMessageDialog(this, "Offer rejected and MO notified.");
        });

        JButton notificationsBtn = new JButton("View Notifications");
        notificationsBtn.addActionListener(e ->
                NotificationPopup.showAllNotifications(this, currentUser, notificationService));

        panel.add(profileBtn);
        panel.add(cvBtn);
        panel.add(applyBtn);
        panel.add(offersBtn);
        panel.add(rejectOfferBtn);
        panel.add(notificationsBtn);
        panel.add(logoutBtn);
        setContentPane(panel);
    }
}
