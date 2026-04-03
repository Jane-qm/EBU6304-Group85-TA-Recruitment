package ta.ui;

import auth.LoginFrame;
import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.TA;
import common.entity.TAApplication;
import common.entity.User;
import common.service.MOJobService;
import common.service.MOOfferService;
import common.service.NotificationService;
import common.service.TAApplicationService;
import common.ui.BaseFrame;
import common.ui.NotificationPopup;
import ta.service.CVService;
import ta.service.TAProfileService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public class TAMainFrame extends BaseFrame {
    private final TA ta;
    private final TAProfileService profileService;
    private final CVService cvService;
    private final MOJobService jobService;
    private final TAApplicationService applicationService;
    private final MOOfferService offerService;
    private final NotificationService notificationService;

    /***
     * Users can browse published positions, submit applications, view their own applications/offers, 
     * and see if they currently meet the application requirements.
     */

    public TAMainFrame(User user) {
        super("TA Recruitment System - Dashboard", 1100, 700);
        this.ta = (TA) user;
        this.profileService = new TAProfileService();
        this.cvService = new CVService();
        this.jobService = new MOJobService();
        this.applicationService = new TAApplicationService();
        this.offerService = new MOOfferService();
        this.notificationService = new NotificationService();
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 251));
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createContentPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(37, 99, 235));
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(24, 18, 24, 18));

        JLabel title = new JLabel("TA Recruitment");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel userLabel = new JLabel(ta.getEmail());
        userLabel.setForeground(new Color(219, 234, 254));
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userLabel.setBorder(new EmptyBorder(8, 0, 24, 0));

        JButton profileBtn = createSidebarButton("My Profile");
        profileBtn.addActionListener(e -> {
            new TAProfileFrame(ta).setVisible(true);
            dispose();
        });

        JButton notificationsBtn = createSidebarButton("System Notifications");
        notificationsBtn.addActionListener(e ->
                NotificationPopup.showUnreadNotifications(this, ta, notificationService));

        JButton logoutBtn = createSidebarButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        sidebar.add(title);
        sidebar.add(userLabel);
        sidebar.add(profileBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(notificationsBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);
        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(220, 42));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        button.setForeground(new Color(17, 24, 39));
        button.setBackground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(245, 247, 251));
        content.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel welcome = new JLabel("Welcome, " + (ta.getName() == null ? ta.getEmail() : ta.getName()));
        welcome.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcome.setForeground(new Color(17, 24, 39));

        JLabel status = new JLabel(buildEligibilitySummary());
        status.setFont(new Font("SansSerif", Font.PLAIN, 14));
        status.setForeground(new Color(75, 85, 99));
        status.setBorder(new EmptyBorder(8, 0, 20, 0));

        content.add(welcome);
        content.add(status);
        content.add(createActionPanel());
        content.add(Box.createVerticalStrut(20));
        content.add(createSectionCard("My Applications", createApplicationsTable()));
        content.add(Box.createVerticalStrut(20));
        content.add(createSectionCard("Published Positions", createJobsTable()));
        content.add(Box.createVerticalStrut(20));
        content.add(createSectionCard("My Offers", createOffersTable()));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(245, 247, 251));
        wrapper.add(new JScrollPane(content), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);

        JButton applyBtn = new JButton("Apply for First Published Job");
        applyBtn.addActionListener(e -> applyForFirstPublishedJob());

        JButton rejectOfferBtn = new JButton("Reject Latest Offer");
        rejectOfferBtn.addActionListener(e -> rejectLatestOffer());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            new TAMainFrame(ta).setVisible(true);
            dispose();
        });

        panel.add(applyBtn);
        panel.add(rejectOfferBtn);
        panel.add(refreshBtn);
        return panel;
    }

    private JPanel createSectionCard(String title, JScrollPane tablePane) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(tablePane, BorderLayout.CENTER);
        return card;
    }

    private JScrollPane createApplicationsTable() {
        String[] columns = {"Application ID", "Job ID", "Status", "Statement"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (TAApplication application : applicationService.listByTaUserId(ta.getUserId())) {
            model.addRow(new Object[]{
                    application.getApplicationId(),
                    application.getJobId(),
                    application.getStatus(),
                    application.getStatement()
            });
        }
        return buildTable(model);
    }

    private JScrollPane createJobsTable() {
        String[] columns = {"Job ID", "Module", "Title", "Hours", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (MOJob job : jobService.listPublishedJobs()) {
            model.addRow(new Object[]{
                    job.getJobId(),
                    job.getModuleCode(),
                    job.getTitle(),
                    job.getWeeklyHours(),
                    job.getStatus()
            });
        }
        return buildTable(model);
    }

    private JScrollPane createOffersTable() {
        String[] columns = {"Offer ID", "Module", "Hours", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (MOOffer offer : offerService.listByTaUserId(ta.getUserId())) {
            model.addRow(new Object[]{
                    offer.getOfferId(),
                    offer.getModuleCode(),
                    offer.getOfferedHours(),
                    offer.getStatus()
            });
        }
        return buildTable(model);
    }

    private JScrollPane buildTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(720, 170));
        return pane;
    }

    private String buildEligibilitySummary() {
        boolean profileComplete = profileService.isProfileComplete(ta.getUserId());
        boolean hasCv = cvService.hasCV(ta.getUserId());
        return "Application access: "
                + (profileComplete ? "profile complete" : "profile incomplete")
                + ", "
                + (hasCv ? "CV uploaded" : "CV missing")
                + ". Unpublished jobs are hidden from TA users.";
    }

    private void applyForFirstPublishedJob() {
        try {
            List<MOJob> jobs = jobService.listPublishedJobs();
            if (jobs.isEmpty()) {
                showWarning("No published jobs are available.");
                return;
            }
            TAApplication application = applicationService.submitApplication(
                    ta.getUserId(),
                    jobs.get(0).getJobId(),
                    "Submitted from TA dashboard."
            );
            showInfo("Application submitted successfully.\n\n" + applicationService.buildApplicationSummary(application));
            new TAMainFrame(ta).setVisible(true);
            dispose();
        } catch (Exception ex) {
            showWarning(ex.getMessage());
        }
    }

    private void rejectLatestOffer() {
        try {
            List<MOOffer> offers = offerService.listByTaUserId(ta.getUserId());
            if (offers.isEmpty()) {
                showWarning("No offers available.");
                return;
            }
            MOOffer latest = offers.get(offers.size() - 1);
            offerService.rejectOffer(latest.getOfferId());
            showInfo("Offer rejected. The MO has been notified.");
            new TAMainFrame(ta).setVisible(true);
            dispose();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
}
