package auth;

import common.entity.User;
import common.entity.UserRole;
import common.service.PermissionService;
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

import common.entity.User;
import common.service.PermissionService;
import common.ui.BaseFrame;
import ta.ui.TAMainFrame;

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
 */

/**
 * 登录窗口
 * 只处理 LoginFrame 的界面和登录按钮事件
 */
public class LoginFrame extends BaseFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeBox;
    private AuthService authService;
    private common.entity.UserRole selectedRole = common.entity.UserRole.TA;

    private static final int CONTENT_WIDTH = 360;
    private static final int FIELD_HEIGHT = 56;

    public LoginFrame() {
        super("TA Recruitment System - Login", 760, 820);
        this.authService = new AuthService();
        initUI();
    }

    @Override
    protected void initUI() {
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

        JToggleButton taTab = createRoleTab("TA", true);
        JToggleButton moTab = createRoleTab("MO", false);
        JToggleButton adminTab = createRoleTab("ADMIN", false);

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

        // 邮箱
        JLabel emailLabel = new JLabel("University Email");
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        emailLabel.setForeground(new Color(17, 24, 39));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField = new JTextField();
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        emailField.setBorder(new EmptyBorder(0, 0, 0, 0));

        cardPanel.add(emailLabel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(wrapField(emailField));
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
    }

    //【新】
    /**
     * 核心逻辑：处理登录 (对应 TA-001, TA-003, TA-006, TA-007)
     */
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            // 1. 调用服务层验证 (TA-001)
            User user = authService.login(email, password);
            
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. 账号状态拦截逻辑 (TA-003)
            // 如果是 PENDING 状态（如 MO 刚注册未审核），拦截进入
            if (!authService.isAccountValid(user)) {
                String msg = authService.getAccountStatusMessage(user);
                JOptionPane.showMessageDialog(this, msg, "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. 记住我逻辑 (TA-007)
            if (rememberMeBox.isSelected()) {
                // 实际开发中此处应写入本地配置文件或 Preferences API
                System.out.println("Persistent Login enabled for: " + email);
            }

            // 4. 权限校验与页面跳转 (TA-006)
            JOptionPane.showMessageDialog(this, "Login Successful! Role: " + user.getRole());
            
            // 根据角色进入不同工作台
            if (user.getRole() == UserRole.ADMIN) {
                // new AdminDashboard(user).setVisible(true);
            } else if (user.getRole() == UserRole.TA) {
                // new TADashboard(user).setVisible(true);
            } else if (user.getRole() == UserRole.MO) {
                // new MODashboard(user).setVisible(true);
            }
            
            this.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //【新】
    /**
     * 密码找回逻辑 (对应 TA-008, TA-009)
     */
    private void handleForgotPassword() {
        String email = JOptionPane.showInputDialog(this, "Please enter your email:");
        if (email == null || email.isBlank()) return;

        if (authService.checkEmailExists(email)) {
            // TA-008: 模拟发送验证码
            String code = JOptionPane.showInputDialog(this, "Verification code sent! Enter the 6-digit code:");
            
            // 模拟验证码校验（通常由 AuthService 生成并校验）
            if ("123456".equals(code)) {
                // TA-009: 重置密码
                String newPwd = JOptionPane.showInputDialog(this, "Enter your new password:");
                if (newPwd != null && !newPwd.isBlank()) {
                    authService.resetPassword(email, newPwd);
                    JOptionPane.showMessageDialog(this, "Password reset successful! Please login.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Wrong verification code.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Email not found.");
        }
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
     * 普通输入框外层
     */
    private JPanel wrapField(JTextField field) {
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
     * 登录按钮事件
     */
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            // 1. 基础非空校验
            if (email.isEmpty() || password.isEmpty()) {
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
                  // 5. Pending accounts continue directly in demo flow.
                    if (user.getStatus() == common.entity.AccountStatus.PENDING) {
                  // No popup; continue to role-based routing directly.
                    }

                        return;
                    }

                    // 6. 正常 ACTIVE 状态账户：结合 PermissionService 校验并跳转对应首页
                   
                    if (PermissionService.hasAccess(user.getRole(), UserRole.ADMIN)) {
                
                        // Admin 权限最高，跳转 Admin 首页
                        // new AdminHomeFrame(user).setVisible(true);
                        showInfo("Admin dashboard - Coming soon");
                        dispose();
                    } else if (PermissionService.hasAccess(user.getRole(), common.entity.UserRole.MO)) {
                        // 仅 MO 跳转 MO 首页
                        // new MOHomeFrame(user).setVisible(true);
                        showInfo("MO dashboard - Coming soon");
                        dispose();
                    } else if (PermissionService.hasAccess(user.getRole(), common.entity.UserRole.TA)) {
                        // 仅 TA 跳转 TA 首页
                        new TAMainFrame(user).setVisible(true);
                        dispose(); // 关闭登录框
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
     * Password recovery with backend update.
     */
    private void handlePasswordRecovery() {
        String email = showInput("Enter your registered email:", "Password Recovery");
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        try {
            if (!authService.checkEmailExists(email.trim())) {
                showError("Email is not registered.");
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

            authService.resetPassword(email.trim(), new String(newPasswordField.getPassword()));
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
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(10, 10, 10, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 10;
            insets.right = 10;
            insets.top = 10;
            insets.bottom = 10;
            return insets;
        }
    }
}