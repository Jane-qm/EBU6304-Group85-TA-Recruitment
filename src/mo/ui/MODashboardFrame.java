package mo.ui;

import common.entity.User;
import common.entity.UserRole;
import common.entity.AccountStatus;
import common.ui.BaseFrame;
import auth.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MODashboardFrame extends BaseFrame {
    private final User currentUser;//存储当前登录的模块负责人用户对象，用于权限控制和个人化展示
    private JPanel mainCardPanel;
    private CardLayout cardLayout;//卡片布局管理器，用于在多个子面板间切换

    // 匹配设计图的品牌色
    private final Color BRAND_BLUE = new Color(59, 130, 246);
    private final Color BRAND_BG = new Color(248, 250, 252);

    //构造函数接收当前用户对象，调用 initUI()方法初始化用户界面
    public MODashboardFrame(User currentUser) {
        super("MO Dashboard - TA Recruitment System", 1100, 700);
        this.currentUser = currentUser;
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BRAND_BG);

        // 1. 左侧导航栏 (Sidebar)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. 右侧主内容区 (CardLayout 用于切换不同页面)
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(BRAND_BG);

        // 初始化子页面
        mainCardPanel.add(createWelcomePanel(), "HOME");
        mainCardPanel.add(new MOJobManagementPanel(currentUser), "JOBS");
        mainCardPanel.add(new MOApplicantReviewPanel(currentUser), "REVIEW");

        add(mainCardPanel, BorderLayout.CENTER);
    }

    //这个方法构建左侧的垂直导航栏
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BRAND_BLUE);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        // 顶部标题
        JLabel titleLabel = new JLabel("TA Recruitment");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // 用户信息区
        JLabel userIcon = new JLabel("👤", SwingConstants.CENTER); // 可以替换为真实的图片Icon
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

        // 导航按钮
        sidebar.add(createNavButton("📊 Dashboard", "HOME"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("📝 Manage Jobs", "JOBS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("👥 Review Applicants", "REVIEW"));

        sidebar.add(Box.createVerticalGlue());

        // 退出按钮
        JButton logoutBtn = createNavButton("🚪 Logout", null);
        logoutBtn.addActionListener(e -> {
            this.dispose();
            // 此处应调出 LoginFrame
            new LoginFrame().setVisible(true);
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

        // 简易 Hover 效果与页面切换
        btn.addActionListener(e -> {
            if (cardName != null) {
                cardLayout.show(mainCardPanel, cardName);
            }
        });
        return btn;
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
