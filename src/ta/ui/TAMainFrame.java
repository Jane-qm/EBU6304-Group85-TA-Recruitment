package ta.ui;

import common.entity.TA;
import common.entity.User;
import common.ui.BaseFrame;
import ta.service.TAProfileService;
import ta.service.CVService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

/**
 * TA 主界面 - Dashboard
 * 显示欢迎信息、近期申请记录和快捷入口
 * 
 * @author System
 * @version 1.0
 */
public class TAMainFrame extends BaseFrame {
    
    private final TA ta;
    private final TAProfileService profileService;
    private final CVService cvService;
    
    public TAMainFrame(User user) {
        super("TA Recruitment System - Dashboard", 1100, 700);
        this.ta = (TA) user;
        this.profileService = new TAProfileService();
        this.cvService = new CVService();
        initUI();
    }
    
    @Override
    protected void initUI() {
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
    }
    
    /**
     * 创建左侧导航栏
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
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
        logoutBtn.addActionListener(e -> {
            new auth.LoginFrame().setVisible(true);
            dispose();
        });
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
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
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
}