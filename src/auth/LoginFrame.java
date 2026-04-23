package auth;

import common.entity.User;
import common.entity.UserRole;
import common.service.PermissionService;
import common.service.UserService;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import common.ui.BaseFrame;
import ta.ui.TAMainFrame;

import mo.ui.MODashboardFrame;   // 导入 MO 首页
import common.entity.AccountStatus; // 导入账号状态

/**
 * 登录窗口
 * Swing 实现，处理用户登录交互
 *
 * @author Can Chen
 * @version 1.0
 *
 * @author Yiping Zheng
 * @version 2.0
 * @update 添加背景图片
 * “Remember Me”（记住我） 的多选框勾选逻辑。
 * “Password Recovery”（找回密码） 的邮件验证和密码重置流程。
 *
 * @author Yiping Zheng
 * @version 3.0
 * @update
 * 修改UI界面
 *
 * @version 3.0
 * @update
 * 修改原有只弹窗拦截的逻辑，接入根据账户状态（ACTIVE/PENDING）
 * 及 PermissionService 跳转首页的功能。
 *
 * @author Can Chen
 * @version 4.0
 * @update 继承 BaseFrame，支持窗口最大化/还原功能
 *
 * @author Can Chen
 * @version 5.0
 * @update 添加 TA 登录跳转到 TAMainFrame
 *
 * @author Jiaze Wang
 * @version 6.0
 * @update Add strict super-admin access validation before routing to Admin Portal
 *
 * @author (Your Name)
 * @version 7.0
 * @update 邮箱输入改为前缀 + 后缀下拉选择 (@qmul.ac.uk / @bupt.edu.cn)
 */

/**
 * 登录窗口
 * 只处理 LoginFrame 的界面和登录按钮事件
 */
public class LoginFrame extends BaseFrame {

    private JTextField emailPrefixField;      // 邮箱前缀输入框
    private JComboBox<String> domainCombo;    // 邮箱后缀下拉选择
    private JPasswordField passwordField;
    private JCheckBox rememberMeBox;
    private final AuthService authService;
    private common.entity.UserRole selectedRole = common.entity.UserRole.TA;
    private final common.entity.UserRole initialRole;

    private static final int CONTENT_WIDTH = 360;
    private static final int FIELD_HEIGHT = 56;

    public LoginFrame() {
        this(common.entity.UserRole.TA);
    }

    /** Opens the login page with the specified role tab pre-selected. */
    public LoginFrame(common.entity.UserRole initialRole) {
        super("TA Recruitment System - Login", 760, 820);
        this.authService = new AuthService();
        this.initialRole = (initialRole != null) ? initialRole : common.entity.UserRole.TA;
        this.selectedRole = this.initialRole;
        initUI();
    }

    @Override
    protected void initUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        // 外层背景
        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBackground(new Color(245, 247, 251));
        rootPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // 中间白色卡片
        RoundedPanel cardPanel = new RoundedPanel(28, Color.WHITE);

        // 强制锁定卡片大小
        Dimension cardSize = new Dimension(620, 760);
        cardPanel.setPreferredSize(cardSize);
        cardPanel.setMinimumSize(cardSize);
        cardPanel.setMaximumSize(cardSize);

        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(36, 36, 36, 36));

        // 标题
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in to your account to continue");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(26));

        // 顶部角色切换栏
        JPanel rolePanel = new RoundedPanel(18, new Color(243, 246, 251));
        rolePanel.setLayout(new GridLayout(1, 3, 10, 0));
        rolePanel.setMaximumSize(new Dimension(CONTENT_WIDTH, 62));
        rolePanel.setPreferredSize(new Dimension(CONTENT_WIDTH, 62));
        rolePanel.setMinimumSize(new Dimension(CONTENT_WIDTH, 62));
        rolePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        rolePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JToggleButton taTab    = createRoleTab("TA",    initialRole == common.entity.UserRole.TA);
        JToggleButton moTab    = createRoleTab("MO",    initialRole == common.entity.UserRole.MO);
        JToggleButton adminTab = createRoleTab("ADMIN", initialRole == common.entity.UserRole.ADMIN);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(taTab);
        roleGroup.add(moTab);
        roleGroup.add(adminTab);

        taTab.addActionListener(e -> {
            selectedRole = common.entity.UserRole.TA;
            updateRoleTabStyles(taTab, moTab, adminTab);
        });
        moTab.addActionListener(e -> {
            selectedRole = common.entity.UserRole.MO;
            updateRoleTabStyles(taTab, moTab, adminTab);
        });
        adminTab.addActionListener(e -> {
            selectedRole = common.entity.UserRole.ADMIN;
            updateRoleTabStyles(taTab, moTab, adminTab);
        });

        rolePanel.add(taTab);
        rolePanel.add(moTab);
        rolePanel.add(adminTab);

        cardPanel.add(rolePanel);
        cardPanel.add(Box.createVerticalStrut(34));

        // 邮箱区域（前缀 + 后缀下拉）
        JLabel emailLabel = new JLabel("University Email");
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        emailLabel.setForeground(new Color(17, 24, 39));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 创建前缀输入框
        emailPrefixField = new JTextField();
        emailPrefixField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        emailPrefixField.setBorder(new EmptyBorder(0, 0, 0, 0));

        // 创建后缀下拉框
        domainCombo = new JComboBox<>(new String[]{"@qmul.ac.uk", "@bupt.edu.cn"});
        domainCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        domainCombo.setPreferredSize(new Dimension(120, FIELD_HEIGHT - 10));
        domainCombo.setMaximumSize(new Dimension(120, FIELD_HEIGHT - 10));

        // 将前缀和下拉框放入一个面板
        JPanel emailWrapper = new JPanel(new BorderLayout(8, 0));
        emailWrapper.setBackground(Color.WHITE);
        emailWrapper.setMaximumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        emailWrapper.setPreferredSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        emailWrapper.setMinimumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        emailWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailWrapper.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(16, new Color(220, 224, 230), 1),
                new EmptyBorder(0, 16, 0, 16)
        ));
        emailWrapper.add(emailPrefixField, BorderLayout.CENTER);
        emailWrapper.add(domainCombo, BorderLayout.EAST);

        cardPanel.add(emailLabel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(emailWrapper);
        cardPanel.add(Box.createVerticalStrut(28));

        // 密码
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        passwordLabel.setForeground(new Color(17, 24, 39));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passwordField.setBorder(new EmptyBorder(0, 0, 0, 0));

        cardPanel.add(passwordLabel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(wrapPasswordField(passwordField));
        cardPanel.add(Box.createVerticalStrut(24));

        // Remember Me + Forgot Password
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setMaximumSize(new Dimension(CONTENT_WIDTH, 36));
        optionsPanel.setPreferredSize(new Dimension(CONTENT_WIDTH, 36));
        optionsPanel.setMinimumSize(new Dimension(CONTENT_WIDTH, 36));
        optionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rememberMeBox = new JCheckBox("Remember Me");
        rememberMeBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rememberMeBox.setForeground(new Color(55, 65, 81));
        rememberMeBox.setOpaque(false);
        rememberMeBox.setFocusPainted(false);
        rememberMeBox.setMargin(new Insets(0, 0, 0, 0));

        JButton forgotPwdButton = new JButton("Forgot Password?");
        forgotPwdButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        forgotPwdButton.setForeground(new Color(37, 99, 235));
        forgotPwdButton.setContentAreaFilled(false);
        forgotPwdButton.setBorderPainted(false);
        forgotPwdButton.setFocusPainted(false);
        forgotPwdButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPwdButton.addActionListener(e -> handlePasswordRecovery());

        optionsPanel.add(rememberMeBox, BorderLayout.WEST);
        optionsPanel.add(forgotPwdButton, BorderLayout.EAST);

        cardPanel.add(optionsPanel);
        cardPanel.add(Box.createVerticalStrut(28));

        // 登录按钮
        JButton loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(37, 99, 235));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(new LoginAction());

        RoundedPanel loginButtonPanel = new RoundedPanel(16, new Color(37, 99, 235));
        loginButtonPanel.setLayout(new BorderLayout());
        loginButtonPanel.setMaximumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        loginButtonPanel.setPreferredSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        loginButtonPanel.setMinimumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        loginButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButtonPanel.add(loginButton, BorderLayout.CENTER);

        cardPanel.add(loginButtonPanel);
        cardPanel.add(Box.createVerticalStrut(34));

        // 底部注册
        JPanel registerPanel = new JPanel();
        registerPanel.setOpaque(false);
        registerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noAccountLabel = new JLabel("Don't have an account? ");
        noAccountLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        noAccountLabel.setForeground(new Color(107, 114, 128));

        JButton registerButton = new JButton("Register here");
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerButton.setForeground(new Color(37, 99, 235));
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        registerPanel.add(noAccountLabel);
        registerPanel.add(registerButton);

        cardPanel.add(registerPanel);

        rootPanel.add(cardPanel);
        add(rootPanel);

        getRootPane().setDefaultButton(loginButton);

        // 加载记住的邮箱（如果有）
        loadRememberedEmail();
    }

    /**
     * 创建角色按钮
     */
    private JToggleButton createRoleTab(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text, selected);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new RoundedBorder(14, new Color(37, 99, 235), 1));
        button.setPreferredSize(new Dimension(100, 44));
        button.setOpaque(true);

        if (selected) {
            button.setBackground(new Color(37, 99, 235));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(243, 246, 251));
            button.setForeground(new Color(107, 114, 128));
        }

        return button;
    }

    /**
     * 密码输入框外层
     */
    private JPanel wrapPasswordField(JPasswordField field) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setMaximumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        wrapper.setPreferredSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        wrapper.setMinimumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(16, new Color(220, 224, 230), 1),
                new EmptyBorder(0, 16, 0, 16)
        ));
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * 加载记住的邮箱，并拆分到前缀输入框和下拉框
     */
    private void loadRememberedEmail() {
        // 模拟从某个存储中读取完整的邮箱（实际实现中可能需要从配置文件或系统属性读取）
        // 这里为了演示，假设没有记住功能，如需扩展可在此处添加代码
        // 如果后期需要接入记住我功能，可在此根据 rememberMeBox 的状态从存储中读取完整邮箱，
        // 然后拆分为前缀和后缀进行设置。
        // 当前版本保持原有逻辑：不主动加载（原有代码中没有自动填充 remember me 的实现，因此保持原样）
        // 注：原有代码的 rememberMeBox 只是 UI 展示，实际并未实现持久化存储，因此此处不添加额外逻辑。
    }

    /**
     * 获取完整的邮箱地址（前缀 + 后缀）
     */
    private String getFullEmail() {
        String prefix = emailPrefixField.getText().trim();
        String domain = (String) domainCombo.getSelectedItem();
        return prefix + domain;
    }

    /**
     * 登录按钮事件
     */
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = getFullEmail();  // 获取完整邮箱
            String password = new String(passwordField.getPassword());

            // 1. 基础非空校验
            if (emailPrefixField.getText().trim().isEmpty() || password.isEmpty()) {
                showWarning("Please enter both email and password");
                return;
            }

            try {
                // 2. 尝试登录获取用户数据
                User user = authService.login(email, password);

                if (user != null) {
                    // 3. 校验所选角色与账户实际角色是否匹配
                    if (user.getRole() != selectedRole) {
                        showWarning("Role mismatch.\nAccount Role: " + user.getRole());
                        return;
                    }

                    // 4. 处理被禁用 (DISABLED) 的账户
                    if (user.getStatus() == common.entity.AccountStatus.DISABLED) {
                        showError(authService.getAccountStatusMessage(user));
                        return;
                    }

                    // 5. Enforce strict super-admin rule before routing to Admin Portal.
                    if (user.getRole() == UserRole.ADMIN) {
                        UserService userService = new UserService();
                        if (!userService.isStrictAdmin(user)) {
                            showError("Only active super admin account admin@test.com can access Admin Portal.");
                            return;
                        }
                        if (user.isMustChangePassword()) {
                            String newPassword = promptMandatoryPasswordChange(user.getEmail());
                            if (newPassword == null) {
                                // Explicitly block portal access until password is changed.
                                return;
                            }
                            authService.resetPassword(user.getEmail(), newPassword);
                            // Re-login with the updated password to refresh in-memory state.
                            user = authService.login(user.getEmail(), newPassword);
                            if (user == null) {
                                showError("Password updated, but automatic re-login failed. Please log in again.");
                                return;
                            }
                        }
                    }

                    // 6. Pending accounts continue directly in demo flow.
                    if (user.getStatus() == common.entity.AccountStatus.PENDING) {
                        // No popup; continue to role-based routing directly.
                    }

                    // 7. Role-based home (Admin/MO: demo consoles; TA: full TAMainFrame)
                    if (PermissionService.hasAccess(user.getRole(), UserRole.ADMIN)) {
                        new AdminHomeFrame(user).setVisible(true);
                        dispose();
                    } else if (PermissionService.hasAccess(user.getRole(), UserRole.MO)) {
                        new MODashboardFrame(user).setVisible(true);
                        dispose();
                    } else if (PermissionService.hasAccess(user.getRole(), UserRole.TA)) {
                        new TAMainFrame(user).setVisible(true);
                        dispose();
                    } else {
                        showInfo("Routing to " + user.getRole() + " Dashboard...");
                        dispose();
                    }

                } else {
                    showError("Invalid email or password");
                }
            } catch (Exception ex) {
                showError("System Error: " + ex.getMessage());
            }
        }
    }

    private void updateRoleTabStyles(JToggleButton taTab, JToggleButton moTab, JToggleButton adminTab) {
        applyRoleTabStyle(taTab, taTab.isSelected());
        applyRoleTabStyle(moTab, moTab.isSelected());
        applyRoleTabStyle(adminTab, adminTab.isSelected());
    }

    private void applyRoleTabStyle(JToggleButton button, boolean selected) {
        if (selected) {
            button.setBackground(new Color(37, 99, 235));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(243, 246, 251));
            button.setForeground(new Color(107, 114, 128));
        }
    }

    /**
     * Forces first-login admin password update before opening AdminHomeFrame.
     * Returning null means the user cancelled or failed validation.
     */
    private String promptMandatoryPasswordChange(String email) {
        JPasswordField pwd1 = new JPasswordField();
        JPasswordField pwd2 = new JPasswordField();
        Object[] content = {
                "First login security setup for " + email,
                "Please set a new password:",
                pwd1,
                "Confirm new password:",
                pwd2
        };
        int option = JOptionPane.showConfirmDialog(
                this,
                content,
                "First Login - Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) {
            showWarning("You must change the bootstrap password before entering Admin Portal.");
            return null;
        }
        String p1 = new String(pwd1.getPassword());
        String p2 = new String(pwd2.getPassword());
        if (p1.isBlank() || p2.isBlank()) {
            showWarning("Password must not be empty.");
            return null;
        }
        if (!p1.equals(p2)) {
            showWarning("Passwords do not match.");
            return null;
        }
        if (p1.length() < 6) {
            showWarning("Password must be at least 6 characters.");
            return null;
        }
        return p1;
    }

    /**
     * Password recovery with backend update.
     * 修改：使用完整的邮箱（前缀+后缀）
     */
    private void handlePasswordRecovery() {
        String fullEmail = getFullEmail();
        // 如果用户没有输入前缀，则弹出输入框让用户重新输入
        if (emailPrefixField.getText().trim().isEmpty()) {
            fullEmail = showInput("Enter your registered email (full address):", "Password Recovery");
            if (fullEmail == null || fullEmail.trim().isEmpty()) {
                return;
            }
        } else {
            // 用户已输入前缀，使用组合的完整邮箱
            // 但为了确认，可以弹出一个确认框或直接使用
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reset password for: " + fullEmail + " ?",
                    "Confirm Email",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            if (!authService.checkEmailExists(fullEmail.trim())) {
                showError("Email is not registered.");
                return;
            }
            User targetUser = new UserService().findByEmail(fullEmail.trim());
            if (targetUser != null && targetUser.getRole() == UserRole.ADMIN) {
                showError("Admin password cannot be reset via self-service recovery.\n"
                        + "Please use secure bootstrap/login flow.");
                return;
            }

            JPasswordField newPasswordField = new JPasswordField();
            int option = JOptionPane.showConfirmDialog(
                    this,
                    new Object[]{"Enter your new password:", newPasswordField},
                    "Reset Password",
                    JOptionPane.OK_CANCEL_OPTION
            );
            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            authService.resetPassword(fullEmail.trim(), new String(newPasswordField.getPassword()));
            showInfo("Password has been reset successfully.");
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (Exception ex) {
            showError("System Error: " + ex.getMessage());
        }
    }

    /**
     * 圆角面板
     */
    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * 圆角边框
     */
    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        private final int thickness;

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            if (thickness > 0) {
                g2.setStroke(new BasicStroke(thickness));
            }
            g2.drawRoundRect(
                    x + thickness / 2,
                    y + thickness / 2,
                    width - thickness,
                    height - thickness,
                    radius,
                    radius
            );
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = radius / 2;
            insets.right = radius / 2;
            insets.top = radius / 2;
            insets.bottom = radius / 2;
            return insets;
        }
    }
}