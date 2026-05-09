package ui.mo;

import modules.auth.AuthService;
import modules.user.User;
import modules.user.UserRole;
import modules.job.JobService;
import modules.notification.NotificationService;
import infrastructure.security.PermissionService;
import infrastructure.ui.BaseFrame;
import infrastructure.ui.NotificationButtonFactory;
import infrastructure.ui.NotificationPopup;
import ui.auth.LoginFrame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.CardLayout;
import java.awt.Cursor;
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

    private JPasswordField moOldPasswordField;
    private JPasswordField moNewPasswordField;
    private JPasswordField moConfirmPasswordField;

    private JLabel topBarTitleLabel;

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
        new JobService().autoCloseExpiredJobs();

        setLayout(new BorderLayout());
        getContentPane().setBackground(APP_BG);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(APP_BG);
        mainCardPanel.add(createWelcomePanel(), "HOME");
        mainCardPanel.add(new MOJobManagementPanel(currentUser), "JOBS");
        mainCardPanel.add(new MOApplicantReviewPanel(currentUser), "REVIEW");

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(APP_BG);
        mainContent.add(createTopBar(), BorderLayout.NORTH);
        mainContent.add(mainCardPanel, BorderLayout.CENTER);

        add(createSidebar(), BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);

        // Default selection
        setActiveButton(dashboardBtn);
        cardLayout.show(mainCardPanel, "HOME");
        updateTopBarTitle("HOME");
    }

    private void updateTopBarTitle(String cardName) {
        if (topBarTitleLabel == null) {
            return;
        }
        String t = switch (cardName) {
            case "HOME" -> "Dashboard";
            case "JOBS" -> "Manage Jobs";
            case "REVIEW" -> "Review Applicants";
            default -> "MO Portal";
        };
        topBarTitleLabel.setText(t);
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

        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(jobsBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(reviewBtn);

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = createSidebarLogoutButton();
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(NAV_INACTIVE_FG);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(240, 48));
        btn.setPreferredSize(new Dimension(240, 48));
        btn.setMinimumSize(new Dimension(240, 48));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        btn.addActionListener(e -> {
            if (cardName != null) {
                cardLayout.show(mainCardPanel, cardName);
                setActiveButton(btn);
                updateTopBarTitle(cardName);
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != currentActiveBtn) {
                    btn.setBackground(NAV_HOVER_BG);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != currentActiveBtn) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });

        return btn;
    }

    private JButton createSidebarLogoutButton() {
        JButton button = new JButton("Logout");
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(SIDEBAR_BG);
        button.setForeground(new Color(239, 68, 68));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(240, 44));
        button.setPreferredSize(new Dimension(240, 44));
        button.setMinimumSize(new Dimension(240, 44));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(NAV_HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SIDEBAR_BG);
            }
        });
        button.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        return button;
    }

    private void setActiveButton(JButton btn) {
        JButton[] buttons = {dashboardBtn, jobsBtn, reviewBtn};
        for (JButton b : buttons) {
            if (b != null) {
                b.setBackground(SIDEBAR_BG);
                b.setForeground(NAV_INACTIVE_FG);
                b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            }
        }
        currentActiveBtn = btn;
        if (currentActiveBtn != null) {
            currentActiveBtn.setBackground(NAV_ACTIVE_BG);
            currentActiveBtn.setForeground(NAV_ACTIVE_FG);
            currentActiveBtn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        topBarTitleLabel = new JLabel("Dashboard");
        topBarTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        topBarTitleLabel.setForeground(new Color(30, 35, 45));

        int unreadCount = notificationService.getUnreadCount(currentUser.getUserId());
        JButton notifyBtn = NotificationButtonFactory.createButton(unreadCount);
        notifyBtn.addActionListener(e ->
                NotificationPopup.showUnreadNotifications(this, currentUser, notificationService));

        topBar.add(topBarTitleLabel, BorderLayout.WEST);
        topBar.add(notifyBtn, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createWelcomePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(APP_BG);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(APP_BG);
        center.setBorder(new EmptyBorder(24, 40, 24, 40));

        JLabel welcomeLabel = new JLabel("Welcome to the MO Portal");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(30, 35, 45));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(welcomeLabel);
        center.add(Box.createVerticalStrut(24));

        JPanel pwdCard = createChangePasswordPanel();
        pwdCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        pwdCard.setMaximumSize(new Dimension(720, 420));
        center.add(pwdCard);

        JScrollPane scroll = new JScrollPane(center,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(APP_BG);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(20, 28, 20, 28)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel title = new JLabel("Change Password");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(30, 35, 45));
        panel.add(title, gbc);

        gbc.gridy = 1;
        JLabel adminLine = new JLabel("MO: " + currentUser.getEmail());
        adminLine.setFont(new Font("SansSerif", Font.PLAIN, 12));
        adminLine.setForeground(new Color(107, 114, 128));
        panel.add(adminLine, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        moOldPasswordField = new JPasswordField(22);
        panel.add(moOldPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        moNewPasswordField = new JPasswordField(22);
        panel.add(moNewPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        moConfirmPasswordField = new JPasswordField(22);
        panel.add(moConfirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton changeBtn = new JButton("Update Password");
        changeBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        MoUiStyles.applyTextButton(changeBtn);
        changeBtn.addActionListener(e -> changeMoPassword());
        panel.add(changeBtn, gbc);

        return panel;
    }

    private void changeMoPassword() {
        String oldPassword = new String(moOldPasswordField.getPassword());
        String newPassword = new String(moNewPasswordField.getPassword());
        String confirmPassword = new String(moConfirmPasswordField.getPassword());

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showWarning("All fields are required.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showWarning("New passwords do not match.");
            return;
        }
        if (newPassword.length() < 6) {
            showWarning("Password must be at least 6 characters.");
            return;
        }

        AuthService authService = new AuthService();
        User user = authService.login(currentUser.getEmail(), oldPassword);
        if (user == null) {
            showWarning("Current password is incorrect.");
            return;
        }

        try {
            authService.resetPassword(currentUser.getEmail(), newPassword);
            moOldPasswordField.setText("");
            moNewPasswordField.setText("");
            moConfirmPasswordField.setText("");
            showInfo("Password changed successfully! Please sign in again.");
            dispose();
            new LoginFrame().setVisible(true);
        } catch (Exception e) {
            showError("Failed to change password: " + e.getMessage());
        }
    }

}
