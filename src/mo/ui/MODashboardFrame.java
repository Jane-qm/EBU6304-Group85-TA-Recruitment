package mo.ui;

import auth.LoginFrame;
import common.entity.User;
import common.entity.UserRole;
import common.service.MOJobService;
import common.service.NotificationService;
import common.service.PermissionService;
import common.ui.BaseFrame;
import common.ui.NotificationButtonFactory;
import common.ui.NotificationPopup;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.CardLayout;

public class MODashboardFrame extends BaseFrame {
    private static final Color SIDEBAR_PRIMARY_TEXT = Color.WHITE;
    private static final Color SIDEBAR_SECONDARY_TEXT = new Color(224, 231, 255);
    private final User currentUser;
    private final NotificationService notificationService = new NotificationService();

    private JPanel mainCardPanel;
    private CardLayout cardLayout;

    private static final Color BRAND_BLUE = new Color(59, 130, 246);
    private static final Color BRAND_BG = new Color(248, 250, 252);

    public MODashboardFrame(User currentUser) {
        super("MO Dashboard - TA Recruitment System", 1100, 700);

        // SYS-001: defend against direct instantiation with wrong-role user
        if (!PermissionService.hasAccess(currentUser == null ? null : currentUser.getRole(), UserRole.MO)) {
            this.currentUser = null;
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Access denied. An MO account is required to open this portal.",
                    "Permission Denied", javax.swing.JOptionPane.ERROR_MESSAGE);
            javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            dispose();
            return;
        }

        this.currentUser = currentUser;
        initUI();
    }

    @Override
    protected void initUI() {
        // MO-009.2: auto-close any jobs whose deadline passed since the last run
        new MOJobService().autoCloseExpiredJobs();

        setLayout(new BorderLayout());
        getContentPane().setBackground(BRAND_BG);

        add(createSidebar(), BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BRAND_BG);
        mainContent.add(createTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(BRAND_BG);
        mainCardPanel.add(createWelcomePanel(), "HOME");
        mainCardPanel.add(new MOJobManagementPanel(currentUser), "JOBS");
        mainCardPanel.add(new MOApplicantReviewPanel(currentUser), "REVIEW");
        mainCardPanel.add(new MOHiredTAsPanel(currentUser), "HIRED");

        mainContent.add(mainCardPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BRAND_BLUE);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel titleLabel = new JLabel("TA Recruitment");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(SIDEBAR_PRIMARY_TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        JLabel userIcon = new JLabel("MO", SwingConstants.CENTER);
        userIcon.setFont(new Font("SansSerif", Font.BOLD, 28));
        userIcon.setForeground(SIDEBAR_PRIMARY_TEXT);
        userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel idLabel = new JLabel(String.valueOf(currentUser.getUserId()));
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        idLabel.setForeground(SIDEBAR_PRIMARY_TEXT);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLabel = new JLabel(currentUser.getEmail());
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emailLabel.setForeground(SIDEBAR_SECONDARY_TEXT);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Module Organiser");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLabel.setForeground(SIDEBAR_SECONDARY_TEXT);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(userIcon);
        sidebar.add(idLabel);
        sidebar.add(emailLabel);
        sidebar.add(roleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 50)));

        sidebar.add(createNavButton("Dashboard", "HOME"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("Manage Jobs", "JOBS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("Review Applicants", "REVIEW"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("Hired TAs", "HIRED"));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = createNavButton("Logout", null);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(new Color(29, 78, 216));
        btn.setBackground(new Color(255, 255, 255, 245));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 210), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setMaximumSize(new Dimension(200, 46));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (cardName != null) {
                cardLayout.show(mainCardPanel, cardName);
            }
        });
        return btn;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel welcomeLabel = new JLabel("Welcome back, MO!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(30, 35, 45));

        int unreadCount = notificationService.getUnreadCount(currentUser.getUserId());
        JButton notifyBtn = NotificationButtonFactory.createButton(unreadCount);
        notifyBtn.addActionListener(e ->
                NotificationPopup.showUnreadNotifications(this, currentUser, notificationService));

        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(notifyBtn, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BRAND_BG);
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel welcomeLabel = new JLabel("Welcome back, MO!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(welcomeLabel, BorderLayout.NORTH);

        return panel;
    }
}
