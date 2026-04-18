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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MODashboardFrame extends BaseFrame {
    private final User currentUser;
    private final NotificationService notificationService = new NotificationService();

    private JPanel mainCardPanel;
    private CardLayout cardLayout;

    // Align visual style with TA portal (TAMainFrame)
    private static final Color APP_BG = new Color(248, 250, 252);
    private static final Color SIDEBAR_BG = new Color(30, 35, 45);
    private static final Color NAV_ACTIVE_BG = new Color(37, 99, 235);
    private static final Color NAV_HOVER_BG = new Color(55, 65, 81);
    private static final Color NAV_ACTIVE_FG = Color.WHITE;
    private static final Color NAV_INACTIVE_FG = new Color(156, 163, 175);

    private JButton currentActiveBtn = null;
    private JButton dashboardBtn;
    private JButton jobsBtn;
    private JButton reviewBtn;
    private JButton hiredBtn;

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
        getContentPane().setBackground(APP_BG);

        add(createSidebar(), BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(APP_BG);
        mainContent.add(createTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(APP_BG);
        mainCardPanel.add(createWelcomePanel(), "HOME");
        mainCardPanel.add(new MOJobManagementPanel(currentUser), "JOBS");
        mainCardPanel.add(new MOApplicantReviewPanel(currentUser), "REVIEW");
        mainCardPanel.add(new MOHiredTAsPanel(currentUser), "HIRED");

        mainContent.add(mainCardPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // Default selection
        setActiveButton(dashboardBtn);
        cardLayout.show(mainCardPanel, "HOME");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(new EmptyBorder(28, 20, 28, 20));

        // Logo (aligned with TA)
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoLabel = new JLabel("TA Recruit");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoSubLabel = new JLabel("Module Organiser Portal");
        logoSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logoSubLabel.setForeground(new Color(107, 114, 128));
        logoSubLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(logoLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(logoSubLabel);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(26));

        // User info (left aligned like TA)
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBackground(SIDEBAR_BG);
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel(currentUser.getEmail());
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel metaLabel = new JLabel("MO #" + currentUser.getUserId());
        metaLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        metaLabel.setForeground(new Color(107, 114, 128));
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userPanel.add(userLabel);
        userPanel.add(Box.createVerticalStrut(4));
        userPanel.add(metaLabel);

        sidebar.add(userPanel);
        sidebar.add(Box.createVerticalStrut(26));

        // Navigation buttons (TA style)
        dashboardBtn = createNavButton("Dashboard", "HOME");
        jobsBtn = createNavButton("Manage Jobs", "JOBS");
        reviewBtn = createNavButton("Review Applicants", "REVIEW");
        hiredBtn = createNavButton("Hired TAs", "HIRED");

        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(jobsBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(reviewBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(hiredBtn);

        sidebar.add(Box.createVerticalGlue());

        // Logout (TA style)
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(239, 68, 68));
        logoutBtn.setOpaque(true);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(240, 46));
        logoutBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
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
        btn.setForeground(NAV_INACTIVE_FG);
        btn.setBackground(SIDEBAR_BG);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(240, 46));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (cardName != null) {
                cardLayout.show(mainCardPanel, cardName);
                setActiveButton(btn);
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != currentActiveBtn) {
                    btn.setBackground(NAV_HOVER_BG);
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != currentActiveBtn) {
                    btn.setBackground(SIDEBAR_BG);
                    btn.setForeground(NAV_INACTIVE_FG);
                }
            }
        });

        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (currentActiveBtn != null && currentActiveBtn != btn) {
            currentActiveBtn.setBackground(SIDEBAR_BG);
            currentActiveBtn.setForeground(NAV_INACTIVE_FG);
        }
        currentActiveBtn = btn;
        if (currentActiveBtn != null) {
            currentActiveBtn.setBackground(NAV_ACTIVE_BG);
            currentActiveBtn.setForeground(NAV_ACTIVE_FG);
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel welcomeLabel = new JLabel("MO Dashboard");
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
        panel.setBackground(APP_BG);
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel welcomeLabel = new JLabel("Welcome to the MO Portal");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(welcomeLabel, BorderLayout.NORTH);

        return panel;
    }
}
