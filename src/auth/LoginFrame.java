package auth;

import common.entity.TA;
import common.entity.User;
import ta.TAProfileFrame;

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
 */
public class LoginFrame extends JFrame {
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private final AuthService authService;
    
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
        setTitle("TA招聘系统 - 登录");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // 居中显示
        setResizable(false);
        
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // 标题
        JLabel titleLabel = new JLabel("国际学校助教招聘系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // 邮箱标签
        JLabel emailLabel = new JLabel("邮箱:");
        emailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(emailLabel, gbc);
        
        // 邮箱输入框
        emailField = new JTextField(20);
        emailField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(emailField, gbc);
        
        // 密码标签
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);
        
        // 密码输入框
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.addActionListener(new LoginAction());
        
        JButton registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerButton.setPreferredSize(new Dimension(100, 35));
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);
        
        // 添加主面板
        add(mainPanel);
        
        // 按回车键触发登录
        getRootPane().setDefaultButton(loginButton);
    }
    
    /**
     * 登录按钮事件处理
     */
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            // 输入校验
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginFrame.this, 
                    "请输入邮箱和密码", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                User user = authService.login(email, password);
                
                if (user != null) {
                    // 账号状态校验
                    if (!authService.isAccountValid(user)) {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            authService.getAccountStatusMessage(user),
                            "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // 按角色路由到对应页面
                    routeByRole(user);

                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "邮箱或密码错误", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                    ex.getMessage(), "输入错误", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                    "系统错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        /**
         * 根据用户角色跳转到对应功能页面。
         *
         * @author Zhixuan Guo
         */
        /**
         * 根据用户角色跳转到对应功能页面。
         *
         * @author Zhixuan Guo
         */
        private void routeByRole(User user) {
            switch (user.getRole()) {
                case TA -> {
                    // RC-4: 跳转到 TA 个人资料页，支持查看与编辑
                    new TAProfileFrame((TA) user).setVisible(true);
                    dispose();
                }
                case MO -> {
                    // TODO Sprint 3: 跳转到 MO 岗位管理页
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "登录成功！欢迎 " + user.getEmail() +
                        "\n角色：课程教师 (MO)\n\nMO 功能将在后续 Sprint 中实现。",
                        "欢迎", JOptionPane.INFORMATION_MESSAGE);
                    emailField.setText("");
                    passwordField.setText("");
                }
                case ADMIN -> {
                    // TODO Sprint 4: 跳转到管理员控制台
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "登录成功！欢迎 " + user.getEmail() +
                        "\n角色：管理员 (Admin)\n\n管理员功能将在后续 Sprint 中实现。",
                        "欢迎", JOptionPane.INFORMATION_MESSAGE);
                    emailField.setText("");
                    passwordField.setText("");
                }
                default -> JOptionPane.showMessageDialog(LoginFrame.this,
                        "未知角色，请联系管理员。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}