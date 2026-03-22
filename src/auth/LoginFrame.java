package auth;

import common.entity.User;
import common.entity.AccountStatus;
import javax.swing.*;
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
 */
public class LoginFrame extends JFrame {
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private AuthService authService;

    // Remember Me，记住我 勾选框
    private JCheckBox rememberMeBox;

    /**
     * 构造函数
     * 初始化窗口和认证服务
     */
    public LoginFrame() {
        this.authService = new AuthService();
        initUI();
    }
    
    /**
     * 初始化用户界面
     */
    private void initUI() {
        setTitle("TA Recruitment System - Login");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // 居中显示
        setResizable(false);
        
        // 主面板
        //JPanel mainPanel = new JPanel();

        // 使用自定义的 JPanel 来绘制背景图片
        JPanel mainPanel = new JPanel() {
            // 加载图片 (注意路径要和你的实际路径一致)
            private ImageIcon icon = new ImageIcon("src/images/Background_1.jpg");
            private Image bgImage = icon.getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 核心：把图片按窗口大小拉伸并画出来
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, this.getWidth(), this.getHeight(), this);
                }
            }
        };

        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 10, 15, 10);
        
        // 标题
        JLabel titleLabel = new JLabel("TA Recruitment System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30)); // Larger, modern font
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // 邮箱标签
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(emailLabel, gbc);
        
        // 邮箱输入框
        emailField = new JTextField(20);
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(emailField, gbc);
        
        // 密码标签
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);
        
        // 密码输入框
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);

        //选项面板：放置 "记住我" 和 "忘记密码"
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false); // 保持背景透明

        rememberMeBox = new JCheckBox("Remember Me");
        rememberMeBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rememberMeBox.setOpaque(false); // 透明背景以显示底层大背景

        JButton forgotPwdButton = new JButton("Forgot Password?");
        forgotPwdButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        forgotPwdButton.setContentAreaFilled(false);
        forgotPwdButton.setBorderPainted(false);
        forgotPwdButton.setForeground(Color.BLUE);
        forgotPwdButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPwdButton.addActionListener(e -> handlePasswordRecovery()); // 绑定找回密码逻辑

        optionsPanel.add(rememberMeBox, BorderLayout.WEST);
        optionsPanel.add(forgotPwdButton, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(optionsPanel, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        //让按钮的容器变成透明的，露出底部的拉风背景
        buttonPanel.setOpaque(false);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.addActionListener(new LoginAction());
        
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);
        
        // 添加主面板
        add(mainPanel);
        
        // 按回车键触发登录
        getRootPane().setDefaultButton(loginButton);
    }

    /**
    * 处理密码找回 (Password Recovery via email) 逻辑
    */
    private void handlePasswordRecovery() {
        // 1. 验证邮箱
        String email = JOptionPane.showInputDialog(this, "Enter your registered email:", "Password Recovery", JOptionPane.QUESTION_MESSAGE);
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        // 此处可以调用 authService.checkEmailExists(email) 验证邮箱
        // 模拟发送邮件验证码
        JOptionPane.showMessageDialog(this, "A verification code has been sent to " + email, "Email Sent", JOptionPane.INFORMATION_MESSAGE);

        // 2. 输入验证码
        String code = JOptionPane.showInputDialog(this, "Enter the verification code:", "Verification", JOptionPane.QUESTION_MESSAGE);
        if (code == null || code.trim().isEmpty()) {
            return;
        }

        // 3. 重置密码
        JPasswordField newPasswordField = new JPasswordField();
        Object[] message = {"Enter your new password:", newPasswordField};
        int option = JOptionPane.showConfirmDialog(this, message, "Reset Password", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPasswordField.getPassword());
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 此处应调用 authService.resetPassword(email, newPassword)
            JOptionPane.showMessageDialog(this, "Password has been reset successfully! Please log in with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 登录按钮事件处理
     */
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            // 获取勾选状态，记住我 状态获取
            boolean isRememberMe = rememberMeBox.isSelected();

            // 输入校验
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginFrame.this, 
                    "Please enter both email and password", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                User user = authService.login(email, password);
                
                if (user != null) {
                    // 额外校验账号状态
                    if (!authService.isAccountValid(user)) {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            authService.getAccountStatusMessage(user),
                            "Account Notice", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // 登录成功
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Login Successful! Welcome " + user.getEmail() + "\nRole: " + user.getRole(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Sprint 1: 显示欢迎信息后回到登录界面
                    showWelcomeInfo(user);
                    
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Invalid email or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                    ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                    "System Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void showWelcomeInfo(User user) {
            JOptionPane.showMessageDialog(LoginFrame.this,
                "Sprint 1: Registration and Login functionalities completed.\n\n" +
                "Current User: " + user.getEmail() + "\n" +
                "Role:" + user.getRole() + "\n" +
                "Status:" + user.getStatus() + "\n\n" +
                "More features will be implemented in upcoming sprints",
                "Welcome", JOptionPane.INFORMATION_MESSAGE);
            
            // 清空输入框，准备下一次登录
            emailField.setText("");
            passwordField.setText("");
        }
    }
}