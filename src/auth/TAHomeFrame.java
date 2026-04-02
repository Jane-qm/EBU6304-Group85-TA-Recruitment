package auth;

import common.entity.CVInfo;
import common.entity.MOOffer;
import common.entity.TAApplication;
import common.entity.TAProfile;
import common.entity.User;
import common.service.CVInfoService;
import common.service.MOOfferService;
import common.service.TAApplicationService;
import common.service.TAProfileService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TAHomeFrame extends JFrame {
    private final User currentUser;
    private final TAProfileService profileService = new TAProfileService();
    private final CVInfoService cvInfoService = new CVInfoService();
    private final TAApplicationService applicationService = new TAApplicationService();
    private final MOOfferService offerService = new MOOfferService();

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
            TAApplication application = new TAApplication();
            application.setTaUserId(currentUser.getUserId());
            application.setJobId(2001L);
            application.setStatement("I am interested in this module.");
            application.setStatus("SUBMITTED");
            applicationService.createOrUpdate(application);
            JOptionPane.showMessageDialog(this, "Application saved.");
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

        panel.add(profileBtn);
        panel.add(cvBtn);
        panel.add(applyBtn);
        panel.add(offersBtn);
        panel.add(logoutBtn);
        setContentPane(panel);
    }
}
