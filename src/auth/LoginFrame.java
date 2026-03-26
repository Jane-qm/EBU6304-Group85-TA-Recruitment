package auth;

import common.entity.User;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
 */

/**
 * 登录窗口
 * 只处理 LoginFrame 的界面和登录按钮事件
 */
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeBox;
    private AuthService authService;

    private static final int CONTENT_WIDTH = 360;
    private static final int FIELD_HEIGHT = 56;

    public LoginFrame() {
        this.authService = new AuthService();
        initUI();
    }

    /**
     * 初始化界面
     */
    private void initUI() {
        setTitle("TA Recruitment System - Login");
        setSize(760, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 外层背景
        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBackground(new Color(245, 247, 251));
        rootPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // 中间白色卡片
        RoundedPanel cardPanel = new RoundedPanel(28, Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(620, 700));
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

        // 顶部角色切换栏（纯 UI，不接业务）
        JPanel rolePanel = new RoundedPanel(18, new Color(243, 246, 251));
        rolePanel.setLayout(new GridLayout(1, 3, 10, 0));
        rolePanel.setMaximumSize(new Dimension(CONTENT_WIDTH, 62));
        rolePanel.setPreferredSize(new Dimension(CONTENT_WIDTH, 62));
        rolePanel.setMinimumSize(new Dimension(CONTENT_WIDTH, 62));
        rolePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        rolePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JToggleButton applicantTab = createRoleTab("Applicant", true);
        JToggleButton staffTab = createRoleTab("Staff", false);
        JToggleButton adminTab = createRoleTab("Admin", false);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(applicantTab);
        roleGroup.add(staffTab);
        roleGroup.add(adminTab);

        rolePanel.add(applicantTab);
        rolePanel.add(staffTab);
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

        optionsPanel.add(rememberMeBox, BorderLayout.WEST);
        optionsPanel.add(forgotPwdButton, BorderLayout.EAST);

        cardPanel.add(optionsPanel);
        cardPanel.add(Box.createVerticalStrut(28));

        // 登录按钮
        JButton loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        loginButton.setForeground(new Color(37, 99, 235));

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

    /**
     * 创建角色按钮
     */
    private JToggleButton createRoleTab(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text, selected);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new RoundedBorder(14, new Color(220, 224, 230), 1));
        button.setPreferredSize(new Dimension(100, 44));

        if (selected) {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(17, 24, 39));
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

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        LoginFrame.this,
                        "Please enter both email and password",
                        "Input Required",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                User user = authService.login(email, password);

                if (user != null) {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Login Successful! Welcome " + user.getEmail(),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Invalid email or password",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        LoginFrame.this,
                        "System Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
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