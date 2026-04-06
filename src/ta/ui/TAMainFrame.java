package ta.ui;

<<<<<<< HEAD
import common.entity.TA;
import common.entity.User;
import common.ui.BaseFrame;
import ta.service.TAProfileService;
import ta.service.CVService;

import javax.swing.*;
=======
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
>>>>>>> aa2732b48ca9c1f4a7107f3d8b004fc7c57fa014
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
<<<<<<< HEAD
import java.awt.*;
import java.time.LocalDate;

/**
 * TA 主界面 - Dashboard
 * 显示欢迎信息、近期申请记录和快捷入口
 * 
 * @author System
 * @version 1.0
=======
import javax.swing.table.JTableHeader;

import auth.LoginFrame;
import common.entity.MOJob;
import common.entity.TA;
import common.entity.User;
import common.service.NotificationService;
import common.ui.BaseFrame;
import common.ui.NotificationPopup;
import ta.controller.TAApplicationController;
import ta.controller.TAOfferController;
import ta.controller.TAProfileController;
import ta.controller.TAAuthController;
import ta.entity.TAApplication;

/**
 * TA 主界面 - Dashboard
 * 继承 BaseFrame，使用统一窗口大小
 * 
 * @author Can Chen
 * @version 3.0
>>>>>>> aa2732b48ca9c1f4a7107f3d8b004fc7c57fa014
 */
public class TAMainFrame extends BaseFrame {
    
    private final TA ta;
<<<<<<< HEAD
    private final TAProfileService profileService;
    private final CVService cvService;
    
=======
    
    // Controllers
    private final TAProfileController profileController;
    private final TAApplicationController applicationController;
    private final TAOfferController offerController;
    private final TAAuthController authController;
    private final NotificationService notificationService;

    // 颜色常量
    private static final Color SIDEBAR_BG = new Color(30, 35, 45);
    private static final Color ACCEPTED_COLOR = new Color(34, 197, 94);
    private static final Color PENDING_COLOR = new Color(234, 179, 8);
    private static final Color REJECTED_COLOR = new Color(239, 68, 68);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);

>>>>>>> aa2732b48ca9c1f4a7107f3d8b004fc7c57fa014
    public TAMainFrame(User user) {
        super("TA Recruitment System - Dashboard", 1200, 750);
        this.ta = (TA) user;
<<<<<<< HEAD
        this.profileService = new TAProfileService();
        this.cvService = new CVService();
=======
        
        this.profileController = new TAProfileController();
        this.applicationController = new TAApplicationController();
        this.offerController = new TAOfferController();
        this.authController = new TAAuthController();
        this.notificationService = new NotificationService();
        
>>>>>>> aa2732b48ca9c1f4a7107f3d8b004fc7c57fa014
        initUI();
    }
    
    @Override
    protected void initUI() {
<<<<<<< HEAD
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 251));
        
        // 左侧导航栏
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);
        
        // 右侧内容区域
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
=======
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));

        mainPanel.add(createSidebar(), BorderLayout.WEST);
        mainPanel.add(createMainContent(), BorderLayout.CENTER);

        setContentPane(mainPanel);
>>>>>>> aa2732b48ca9c1f4a7107f3d8b004fc7c57fa014
    }
    
    /**
     * 创建左侧导航栏
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
<<<<<<< HEAD
        sidebar.setBackground(new Color(37, 99, 235));
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        
        // Logo 区域
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel logoLabel = new JLabel("TA Recruitment");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);
        
        // 用户信息区域
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setOpaque(false);
        userPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        userPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 头像占位
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String displayName = ta.getName() != null ? ta.getName() : ta.getEmail().split("@")[0];
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emailLabel = new JLabel(ta.getEmail());
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emailLabel.setForeground(new Color(200, 210, 255));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel roleLabel = new JLabel("Teaching Assistant");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(200, 210, 255));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        userPanel.add(avatarLabel);
        userPanel.add(Box.createVerticalStrut(10));
        userPanel.add(nameLabel);
        userPanel.add(Box.createVerticalStrut(5));
        userPanel.add(emailLabel);
        userPanel.add(Box.createVerticalStrut(5));
        userPanel.add(roleLabel);
        
        // 导航菜单
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(30, 15, 20, 15));
        
        JButton dashboardBtn = createNavButton("📊  Dashboard", true);
        JButton coursesBtn = createNavButton("📚  Course Catalog", false);
        JButton applicationsBtn = createNavButton("📋  My Applications", false);
        JButton profileBtn = createNavButton("👤  My Profile", false);
        
        dashboardBtn.addActionListener(e -> refreshContent(createDashboardContent()));
        coursesBtn.addActionListener(e -> {
            // TODO: 跳转浏览课程界面
            showInfo("Browse Courses - Coming soon");
        });
        applicationsBtn.addActionListener(e -> {
            // TODO: 跳转申请记录界面
            showInfo("My Applications - Coming soon");
        });
        profileBtn.addActionListener(e -> {
            // 跳转个人信息界面
            new TAProfileFrame(ta).setVisible(true);
            dispose();
        });
        
        menuPanel.add(dashboardBtn);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(coursesBtn);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(applicationsBtn);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(profileBtn);
        
        // 登出按钮
        JPanel logoutPanel = new JPanel();
        logoutPanel.setOpaque(false);
        logoutPanel.setBorder(new EmptyBorder(20, 15, 30, 15));
        logoutPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton logoutBtn = new JButton("🚪  Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        logoutBtn.setForeground(new Color(255, 200, 200));
        logoutBtn.setBackground(new Color(37, 99, 235));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
=======
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel logoLabel = new JLabel("TA Recruit");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(logoLabel);
        sidebar.add(Box.createVerticalStrut(32));

        sidebar.add(createNavButton("Dashboard", true));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Course Catalog", false));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("My Applications", false));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("My Profile", false));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Workload Tracking", false));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(16));
        
        String displayName = authController.getDisplayName(ta);
        JLabel userLabel = new JLabel(displayName);
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLabel.setForeground(new Color(156, 163, 175));
        userLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(userLabel);
        
        JLabel roleLabel = new JLabel(authController.getRoleDisplayText());
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(107, 114, 128));
        roleLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(roleLabel);
        
        sidebar.add(Box.createVerticalStrut(16));
        
        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        logoutBtn.setForeground(new Color(239, 68, 68));
        logoutBtn.setBackground(SIDEBAR_BG);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(LEFT_ALIGNMENT);
>>>>>>> aa2732b48ca9c1f4a7107f3d8b004fc7c57fa014
        logoutBtn.addActionListener(e -> {
            new auth.LoginFrame().setVisible(true);
            dispose();
        });
<<<<<<< HEAD
        logoutPanel.add(logoutBtn);
        
        sidebar.add(logoPanel);
        sidebar.add(userPanel);
        sidebar.add(menuPanel);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutPanel);
        
        return sidebar;
    }
    
    /**
     * 创建导航按钮
     */
    private JButton createNavButton(String text, boolean active) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setForeground(active ? new Color(37, 99, 235) : Color.WHITE);
        button.setBackground(active ? Color.WHITE : new Color(37, 99, 235));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(250, 45));
        button.setPreferredSize(new Dimension(250, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (!active) {
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(59, 121, 255));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(37, 99, 235));
                }
            });
        }
        
        return button;
    }
    
    /**
     * 创建右侧内容区域
     */
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 247, 251));
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // 初始显示 Dashboard 内容
        contentPanel.add(createDashboardContent(), BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    /**
     * 刷新内容区域
     */
    private void refreshContent(JPanel newContent) {
        JPanel contentPanel = (JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
        contentPanel.removeAll();
        contentPanel.add(newContent, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * 创建 Dashboard 内容
     */
    private JPanel createDashboardContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 247, 251));
        
        // 欢迎标题
        JLabel welcomeLabel = new JLabel("Welcome back, " + (ta.getName() != null ? ta.getName().split(" ")[0] : "TA") + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(17, 24, 39));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 近期申请记录标题
        JLabel recentLabel = new JLabel("Recent Applications");
        recentLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        recentLabel.setForeground(new Color(17, 24, 39));
        recentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        recentLabel.setBorder(new EmptyBorder(30, 0, 15, 0));
        
        // 申请记录表格
        JScrollPane tableScrollPane = createApplicationsTable();
        
        // 探索新机会区域
        JPanel explorePanel = createExplorePanel();
        
        panel.add(welcomeLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Here's what's happening with your applications"));
        panel.add(Box.createVerticalStrut(20));
        panel.add(recentLabel);
        panel.add(tableScrollPane);
        panel.add(Box.createVerticalStrut(30));
        panel.add(explorePanel);
        
        return panel;
    }
    
    /**
     * 创建申请记录表格
     */
    private JScrollPane createApplicationsTable() {
        String[] columns = {"Course", "Status", "Applied", "Feedback"};
        Object[][] data = {
            {"Introduction to Computer Science", "Accepted", "2026-03-01", "Excellent candidate with strong programming skills"},
            {"Data Structures & Algorithms", "Pending", "2026-03-05", "—"},
            {"Machine Learning", "Rejected", "2026-03-08", "Position filled by another candidate"}
        };
        
        DefaultTableModel model = new DefaultTableModel(data, columns) {
=======
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNavButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isActive) {
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(59, 130, 246, 50));
        } else {
            button.setForeground(new Color(156, 163, 175));
            button.setBackground(SIDEBAR_BG);
        }
        
        button.addActionListener(e -> {
            switch (text) {
                case "My Profile":
                    new TAProfileFrame(ta).setVisible(true);
                    dispose();
                    break;
                case "My Applications":
                    new TAApplicationsFrame(ta).setVisible(true);
                    dispose();
                    break;
                case "Course Catalog":
                    new TACourseCatalogFrame(ta).setVisible(true);
                    dispose();
                    break;
                case "Workload Tracking":
                    new TAWorkloadFrame(ta).setVisible(true);
                    dispose();
                    break;
                default:
                    break;
            }
        });
        
        return button;
    }

    private JScrollPane createMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(248, 250, 252));
        content.setBorder(new EmptyBorder(30, 35, 30, 35));

        content.add(createWelcomeSection());
        content.add(Box.createVerticalStrut(25));
        content.add(createStatsCards());
        content.add(Box.createVerticalStrut(25));
        content.add(createApplicationsSection());
        content.add(Box.createVerticalStrut(20));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }

    private JPanel createWelcomeSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String displayName = authController.getDisplayName(ta);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + displayName);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(30, 35, 45));

        JLabel roleLabel = new JLabel(authController.getRoleDisplayText());
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(107, 114, 128));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel);
        textPanel.add(roleLabel);

        int unreadCount = applicationController.getUnreadNotificationCount(ta.getUserId());
        JButton notifyBtn = new JButton("🔔" + (unreadCount > 0 ? " " + unreadCount : ""));
        notifyBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        notifyBtn.setBackground(new Color(248, 250, 252));
        notifyBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        notifyBtn.setFocusPainted(false);
        notifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifyBtn.addActionListener(e -> 
                NotificationPopup.showUnreadNotifications(this, ta, notificationService));

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(notifyBtn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatsCards() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 15);

        TAApplicationController.ApplicationStats stats = 
                applicationController.getApplicationStats(ta.getUserId());

        gbc.gridx = 0;
        panel.add(createStatCard("✅", "Accepted", String.valueOf(stats.accepted), ACCEPTED_COLOR), gbc);
        
        gbc.gridx = 1;
        panel.add(createStatCard("⏳", "Pending", String.valueOf(stats.pending), PENDING_COLOR), gbc);
        
        gbc.gridx = 2;
        panel.add(createStatCard("❌", "Rejected", String.valueOf(stats.rejected), REJECTED_COLOR), gbc);
        
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(createStatCard("📋", "Total", String.valueOf(stats.getTotal()), PRIMARY_BLUE), gbc);

        return panel;
    }

    private JPanel createStatCard(String emoji, String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
        emojiLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(107, 114, 128));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(emojiLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(titleLabel);

        return card;
    }

    private JPanel createApplicationsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Recent Applications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        JButton viewAllBtn = new JButton("View All →");
        viewAllBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        viewAllBtn.setForeground(PRIMARY_BLUE);
        viewAllBtn.setBackground(Color.WHITE);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> {
            new TAApplicationsFrame(ta).setVisible(true);
            dispose();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllBtn, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        int remainingSlots = applicationController.getRemainingApplicationSlots(ta.getUserId());
        int maxApps = applicationController.getMaxActiveApplications();
        JLabel limitLabel = new JLabel("You can only have " + maxApps + " active applications at once. " +
                (remainingSlots > 0 ? remainingSlots + " slots remaining." : "No slots remaining."));
        limitLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        limitLabel.setForeground(remainingSlots > 0 ? new Color(107, 114, 128) : new Color(239, 68, 68));
        limitLabel.setBorder(new EmptyBorder(8, 0, 16, 0));
        panel.add(limitLabel, BorderLayout.CENTER);
        
        panel.add(createApplicationsTable(), BorderLayout.SOUTH);

        return panel;
    }

    private JScrollPane createApplicationsTable() {
        String[] columns = {"Course", "Status", "Applied", "Feedback"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
>>>>>>> aa2732b48ca9c1f4a7107f3d8b004fc7c57fa014
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
<<<<<<< HEAD
        
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(243, 246, 251));
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        
        return scrollPane;
    }
    
    /**
     * 创建探索新机会面板
     */
    private JPanel createExplorePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(25, 30, 25, 30)
        ));
        
        JLabel titleLabel = new JLabel("✨ Explore New Opportunities");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(17, 24, 39));
        
        JLabel descLabel = new JLabel("Browse open TA positions and apply today");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLabel.setForeground(new Color(107, 114, 128));
        descLabel.setBorder(new EmptyBorder(5, 0, 15, 0));
        
        JButton browseBtn = new JButton("Browse Courses →");
        browseBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        browseBtn.setForeground(Color.WHITE);
        browseBtn.setBackground(new Color(37, 99, 235));
        browseBtn.setBorderPainted(false);
        browseBtn.setFocusPainted(false);
        browseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        browseBtn.setPreferredSize(new Dimension(160, 40));
        browseBtn.addActionListener(e -> {
            // TODO: 跳转浏览课程界面
            showInfo("Browse Courses - Coming soon");
        });
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        textPanel.add(browseBtn);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    @Override
    protected void onWindowMaximized() {
        // 窗口最大化时的回调
    }
    
    @Override
    protected void onWindowRestored() {
        // 窗口还原时的回调
    }
=======

        List<TAApplication> applications = applicationController.getMyApplications(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 只显示最近的 5 条
        int displayCount = Math.min(applications.size(), 5);
        for (int i = 0; i < displayCount; i++) {
            TAApplication app = applications.get(i);
            String status = applicationController.getDisplayStatus(app);
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String feedback = applicationController.getFeedbackMessage(app);
            String courseName = getCourseName(app.getJobId());
            
            model.addRow(new Object[]{courseName, status, appliedAt, feedback});
        }

        if (applications.isEmpty()) {
            model.addRow(new Object[]{"—", "—", "—", "No applications yet"});
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        table.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(720, 220));

        return scrollPane;
    }

    private String getCourseName(Long jobId) {
        List<MOJob> jobs = applicationController.getPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return "Course #" + jobId;
    }

    /**
     * 状态单元格渲染器
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = (String) value;
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            if ("accepted".equalsIgnoreCase(status)) {
                setBackground(new Color(220, 252, 231));
                setForeground(new Color(22, 101, 52));
            } else if ("pending".equalsIgnoreCase(status)) {
                setBackground(new Color(254, 249, 195));
                setForeground(new Color(161, 98, 7));
            } else if ("rejected".equalsIgnoreCase(status)) {
                setBackground(new Color(254, 226, 226));
                setForeground(new Color(153, 27, 27));
            } else {
                setBackground(Color.WHITE);
                setForeground(new Color(107, 114, 128));
            }
            
            return this;
        }
    }
>>>>>>> aa2732b48ca9c1f4a7107f3d8b004fc7c57fa014
}