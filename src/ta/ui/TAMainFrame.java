package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import auth.LoginFrame;
import common.entity.TA;
import common.entity.User;
import common.entity.UserRole;
import common.service.NotificationService;
import common.service.PermissionService;
import common.ui.BaseFrame;
import common.ui.NotificationPopup;
import ta.controller.TAApplicationController;
import ta.controller.TAAuthController;


public class TAMainFrame extends BaseFrame {
    
    private final TA ta;
    
    private final TAApplicationController applicationController;
    private final TAAuthController authController;
    private final NotificationService notificationService;
    
    // 面板组件
    private JPanel contentPanel;
    private java.awt.CardLayout cardLayout;
    
    // 各个功能面板
    private TADashboardPanel dashboardPanel;
    private TACourseCatalogPanel courseCatalogPanel;
    private TAApplicationsPanel applicationsPanel;
    private TAProfilePanel profilePanel;
    private TAWorkloadPanel workloadPanel;
    
    // 导航按钮
    private JButton dashboardBtn;
    private JButton courseCatalogBtn;
    private JButton applicationsBtn;
    private JButton profileBtn;
    private JButton workloadBtn;
    
    // 颜色常量
    private static final Color SIDEBAR_BG = new Color(30, 35, 45);
    private static final Color NAV_ACTIVE_BG = new Color(37, 99, 235);
    private static final Color NAV_HOVER_BG = new Color(55, 65, 81);
    private static final Color NAV_ACTIVE_FG = Color.WHITE;
    private static final Color NAV_INACTIVE_FG = new Color(156, 163, 175);
    
    // 当前选中的按钮
    private JButton currentActiveBtn = null;

    public TAMainFrame(User user) {
        super("TA Recruitment System - Dashboard", 1200, 750);

        // SYS-001: defend against direct instantiation with wrong-role user
        if (!PermissionService.hasAccess(user == null ? null : user.getRole(), UserRole.TA)) {
            this.ta = null;
            this.applicationController = null;
            this.authController = null;
            this.notificationService = null;
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Access denied. A TA account is required to open this portal.",
                    "Permission Denied", javax.swing.JOptionPane.ERROR_MESSAGE);
            javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            dispose();
            return;
        }

        this.ta = (TA) user;   // safe: role verified above; UserFileDAO returns TA instance for TA role
        this.applicationController = new TAApplicationController();
        this.authController = new TAAuthController();
        this.notificationService = new NotificationService();

        initUI();
        
        // 登录后检查是否有待处理的 Offer
    //    checkPendingOffers();
    }

    @Override
    protected void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));

        mainPanel.add(createSidebar(), BorderLayout.WEST);
        mainPanel.add(createMainContent(), BorderLayout.CENTER);

        setContentPane(mainPanel);
    }
    
    /**
     * 检查是否有待处理的 Offer
     */
    //private void checkPendingOffers() {
    //    if (offerController.hasPendingOffers(ta.getUserId())) {
         // 延迟显示，确保 UI 已完全加载
    //        javax.swing.SwingUtilities.invokeLater(() -> {
    //            offerController.handlePendingOffers(ta.getUserId(), this, () -> {
    //                // Offer 处理完成后刷新面板
    //                refreshAllPanels();
    //            });
     //       });
    //    }
    //}
    
    /**
     * 刷新所有面板
     */
    public void refreshAllPanels() {
        if (dashboardPanel != null) dashboardPanel.refresh();
        if (courseCatalogPanel != null) courseCatalogPanel.refresh();
        if (applicationsPanel != null) applicationsPanel.refresh();
        if (profilePanel != null) profilePanel.refresh();
        if (workloadPanel != null) workloadPanel.refresh();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(new EmptyBorder(28, 20, 28, 20));

        // Logo 区域
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel logoLabel = new JLabel("TA Recruit");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel logoSubLabel = new JLabel("Teaching Assistant System");
        logoSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logoSubLabel.setForeground(new Color(107, 114, 128));
        logoSubLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        logoPanel.add(logoLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(logoSubLabel);
        
        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(36));

        // 导航按钮
        dashboardBtn = createNavButton("Dashboard", "🏠");
        courseCatalogBtn = createNavButton("Course Catalog", "📚");
        applicationsBtn = createNavButton("My Applications", "📝");
        profileBtn = createNavButton("My Profile", "👤");
        workloadBtn = createNavButton("Workload Tracking", "⏱️");
        
        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(courseCatalogBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(applicationsBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(profileBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(workloadBtn);

        sidebar.add(Box.createVerticalGlue());

        // 分隔线
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(55, 65, 81));
        separator.setMaximumSize(new Dimension(240, 1));
        separator.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(20));

        // 底部用户信息
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBackground(SIDEBAR_BG);
        userPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        String displayName = authController.getDisplayName(ta);
        JLabel userLabel = new JLabel(displayName);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel roleLabel = new JLabel(authController.getRoleDisplayText());
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(107, 114, 128));
        roleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        userPanel.add(userLabel);
        userPanel.add(Box.createVerticalStrut(4));
        userPanel.add(roleLabel);
        
        sidebar.add(userPanel);
        sidebar.add(Box.createVerticalStrut(16));
        
        // 退出按钮
        JButton logoutBtn = createLogoutButton();
        sidebar.add(logoutBtn);
        
        // 设置默认选中 Dashboard
        setActiveButton(dashboardBtn);

        return sidebar;
    }
    
    /**
     * 创建导航按钮
     */
    private JButton createNavButton(String text, String icon) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(SIDEBAR_BG);
        button.setForeground(NAV_INACTIVE_FG);
        button.setMaximumSize(new Dimension(240, 48));
        button.setPreferredSize(new Dimension(240, 48));
        button.setMinimumSize(new Dimension(240, 48));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != currentActiveBtn) {
                    button.setBackground(NAV_HOVER_BG);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != currentActiveBtn) {
                    button.setBackground(SIDEBAR_BG);
                }
            }
        });
        
        button.addActionListener(e -> {
            switchPanel(text);
            setActiveButton(button);
        });
        
        return button;
    }
    
    /**
     * 创建退出按钮
     */
    private JButton createLogoutButton() {
        JButton button = new JButton("🚪  Sign Out");
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(SIDEBAR_BG);
        button.setForeground(new Color(239, 68, 68));
        button.setMaximumSize(new Dimension(240, 44));
        button.setPreferredSize(new Dimension(240, 44));
        button.setMinimumSize(new Dimension(240, 44));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(NAV_HOVER_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SIDEBAR_BG);
            }
        });
        
        button.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        
        return button;
    }
    
    /**
     * 设置激活按钮样式
     */
    private void setActiveButton(JButton button) {
        // 重置所有按钮样式
        JButton[] buttons = {dashboardBtn, courseCatalogBtn, applicationsBtn, profileBtn, workloadBtn};
        for (JButton btn : buttons) {
            if (btn != null) {
                btn.setBackground(SIDEBAR_BG);
                btn.setForeground(NAV_INACTIVE_FG);
                btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            }
        }
        
        // 设置当前按钮为激活状态
        currentActiveBtn = button;
        if (button != null) {
            button.setBackground(NAV_ACTIVE_BG);
            button.setForeground(NAV_ACTIVE_FG);
            button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        }
    }
    
    /**
     * 切换到指定面板
     */
    public void switchPanel(String panelName) {
        String cardName = "";
        
        switch (panelName) {
            case "Dashboard":
                cardName = "DASHBOARD";
                break;
            case "Course Catalog":
                cardName = "COURSE_CATALOG";
                break;
            case "My Applications":
                cardName = "APPLICATIONS";
                break;
            case "My Profile":
                cardName = "PROFILE";
                break;
            case "Workload Tracking":
                cardName = "WORKLOAD";
                break;
            default:
                return;
        }
        
        // 切换卡片
        if (cardLayout != null && contentPanel != null) {
            cardLayout.show(contentPanel, cardName);
        }
        
        // 刷新对应面板数据
        refreshPanel(cardName);
    }
    
    /**
     * 切换到 Profile 面板
     */
    public void switchToProfile() {
        switchPanel("My Profile");
        setActiveButton(profileBtn);
    }
    
    /**
     * 切换到 Applications 面板
     */
    public void switchToApplications() {
        switchPanel("My Applications");
        setActiveButton(applicationsBtn);
    }
    
    /**
     * 切换到 Course Catalog 面板
     */
    public void switchToCourseCatalog() {
        switchPanel("Course Catalog");
        setActiveButton(courseCatalogBtn);
    }
    
    /**
     * 切换到 Dashboard 面板
     */
    public void switchToDashboard() {
        switchPanel("Dashboard");
        setActiveButton(dashboardBtn);
    }
    
    /**
     * 切换到 Workload 面板
     */
    public void switchToWorkload() {
        switchPanel("Workload Tracking");
        setActiveButton(workloadBtn);
    }
    
    private void refreshPanel(String panelName) {
        switch (panelName) {
            case "DASHBOARD":
                if (dashboardPanel != null) dashboardPanel.refresh();
                break;
            case "COURSE_CATALOG":
                if (courseCatalogPanel != null) courseCatalogPanel.refresh();
                break;
            case "APPLICATIONS":
                if (applicationsPanel != null) applicationsPanel.refresh();
                break;
            case "PROFILE":
                if (profilePanel != null) profilePanel.refresh();
                break;
            case "WORKLOAD":
                if (workloadPanel != null) workloadPanel.refresh();
                break;
            default:
                break;
        }
    }
    
    private JPanel createMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(248, 250, 252));
        
        // 顶部栏
        mainContent.add(createTopBar(), BorderLayout.NORTH);
        
        // 卡片内容面板
        cardLayout = new java.awt.CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(248, 250, 252));
        
        // 初始化各个面板
        dashboardPanel = new TADashboardPanel(ta);
        courseCatalogPanel = new TACourseCatalogPanel(ta);
        applicationsPanel = new TAApplicationsPanel(ta);
        profilePanel = new TAProfilePanel(ta);
        workloadPanel = new TAWorkloadPanel(ta);
        
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(courseCatalogPanel, "COURSE_CATALOG");
        contentPanel.add(applicationsPanel, "APPLICATIONS");
        contentPanel.add(profilePanel, "PROFILE");
        contentPanel.add(workloadPanel, "WORKLOAD");
        
        // 默认显示Dashboard
        cardLayout.show(contentPanel, "DASHBOARD");
        
        mainContent.add(contentPanel, BorderLayout.CENTER);
        
        return mainContent;
    }
    
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        // 欢迎语
        String displayName = authController.getDisplayName(ta);
        JLabel welcomeLabel = new JLabel("Welcome, " + displayName);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(30, 35, 45));
        
        // 通知按钮
        int unreadCount = applicationController.getUnreadNotificationCount(ta.getUserId());
        JButton notifyBtn = new JButton("🔔" + (unreadCount > 0 ? " " + unreadCount : ""));
        notifyBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        notifyBtn.setBackground(Color.WHITE);
        notifyBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        notifyBtn.setFocusPainted(false);
        notifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifyBtn.addActionListener(e -> 
                NotificationPopup.showUnreadNotifications(this, ta, notificationService));
        
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(notifyBtn, BorderLayout.EAST);
        
        return topBar;
    }
}