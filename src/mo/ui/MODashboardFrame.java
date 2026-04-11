package mo.ui;

import auth.LoginFrame;
import common.entity.User;
import common.service.NotificationService;
import common.ui.BaseFrame;
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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

public class MODashboardFrame extends BaseFrame {
    private final User currentUser;
    private final NotificationService notificationService;
    private JPanel mainCardPanel;
    private CardLayout cardLayout;

    private final Color BRAND_BLUE = new Color(59, 130, 246);
    private final Color BRAND_BG = new Color(248, 250, 252);

    public MODashboardFrame(User currentUser) {
        super("MO Dashboard - TA Recruitment System", 1100, 700);
        this.currentUser = currentUser;
        this.notificationService = new NotificationService();
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BRAND_BG);

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BRAND_BG);
        centerPanel.add(createTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(BRAND_BG);

        mainCardPanel.add(createWelcomePanel(), "HOME");
        mainCardPanel.add(new MOJobManagementPanel(currentUser), "JOBS");
        mainCardPanel.add(new MOApplicantReviewPanel(currentUser), "REVIEW");

        centerPanel.add(mainCardPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BRAND_BLUE);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel titleLabel = new JLabel("TA Recruitment");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        JLabel userIcon = new JLabel("👤", SwingConstants.CENTER);
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        userIcon.setForeground(Color.WHITE);
        userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel idLabel = new JLabel(String.valueOf(currentUser.getUserId()));
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        idLabel.setForeground(Color.WHITE);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLabel = new JLabel(currentUser.getEmail());
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emailLabel.setForeground(new Color(224, 231, 255));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Module Organiser");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(224, 231, 255));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(userIcon);
        sidebar.add(idLabel);
        sidebar.add(emailLabel);
        sidebar.add(roleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 50)));

        sidebar.add(createNavButton("📊 Dashboard", "HOME"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("📝 Manage Jobs", "JOBS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("👥 Review Applicants", "REVIEW"));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = createNavButton("🚪 Logout", null);
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.BLUE);
        btn.setBackground(BRAND_BLUE);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 45));
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
        JButton notifyBtn = new JButton("🔔" + (unreadCount > 0 ? " " + unreadCount : ""));
        notifyBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        notifyBtn.setBackground(Color.WHITE);
        notifyBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        notifyBtn.setFocusPainted(false);
        notifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifyBtn.addActionListener(e -> NotificationPopup.showUnreadNotifications(this, currentUser, notificationService));

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
