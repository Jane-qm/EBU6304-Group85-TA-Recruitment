package auth;

import common.entity.User;
import common.entity.UserRole;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 * 注册窗口
 * Swing 实现，处理用户注册交互
 * 
 * @author Can Chen
 * @version 1.0
 */
public class RegisterFrame extends JFrame {
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JComboBox<String> roleCombo;
    private JLabel strengthLabel;
    private final AuthService authService;  // 改为 final（提示优化）
    
    public RegisterFrame() {
        this.authService = new AuthService();
        initUI();
    }
    
    private void initUI() {
        setTitle("TA招聘系统 - 注册");
        setSize(500, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // 标题
        JLabel titleLabel = new JLabel("用户注册");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // 邮箱
        JLabel emailLabel = new JLabel("邮箱:");
        emailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(emailLabel, gbc);
        
        emailField = new JTextField(20);
        emailField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(emailField, gbc);
        
        // 密码
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);
        
        // 确认密码
        JLabel confirmLabel = new JLabel("确认密码:");
        confirmLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(confirmLabel, gbc);
        
        confirmField = new JPasswordField(20);
        confirmField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(confirmField, gbc);
        
        // 角色选择
        JLabel roleLabel = new JLabel("角色:");
        roleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(roleLabel, gbc);
        
        roleCombo = new JComboBox<>(new String[]{"助教 (TA)", "课程教师 (MO)"});
        roleCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 4;
        mainPanel.add(roleCombo, gbc);
        
        // 密码强度提示
        JLabel strengthTitleLabel = new JLabel("密码强度:");
        strengthTitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(strengthTitleLabel, gbc);
        
        strengthLabel = new JLabel("未输入");
        strengthLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        strengthLabel.setForeground(Color.GRAY);
        gbc.gridx = 1;
        gbc.gridy = 5;
        mainPanel.add(strengthLabel, gbc);
        
        // 添加密码监听器（实时显示密码强度）
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String pwd = new String(passwordField.getPassword());
                String strength = checkPasswordStrength(pwd);
                strengthLabel.setText(strength);
                
                if (strength.equals("弱")) {
                    strengthLabel.setForeground(Color.RED);
                } else if (strength.equals("中")) {
                    strengthLabel.setForeground(Color.ORANGE);
                } else if (strength.equals("强")) {
                    strengthLabel.setForeground(new Color(0, 150, 0));
                } else {
                    strengthLabel.setForeground(Color.GRAY);
                }
            }
        });
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(100, 35));
        registerButton.addActionListener(e -> handleRegister());
        
        JButton cancelButton = new JButton("返回");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
        getRootPane().setDefaultButton(registerButton);
    }
    
    /**
     * 检查密码强度
     */
    private String checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "未输入";
        }
        
        int score = 0;
        
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;
        
        if (score <= 2) return "弱";
        else if (score <= 4) return "中";
        else return "强";
    }
    
    /**
     * 处理注册事件
     */
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String roleStr = (String) roleCombo.getSelectedItem();
        
        // 输入校验
        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "请填写所有字段", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 密码一致性校验
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, 
                "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 密码强度校验 - 修复：将 int confirm 改为 int option
        String strength = checkPasswordStrength(password);
        if (strength.equals("弱")) {
            int option = JOptionPane.showConfirmDialog(this,
                "密码强度较弱，建议使用更复杂的密码（包含大小写字母、数字和特殊字符）\n是否继续注册？",
                "密码提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // 转换角色
        UserRole role = roleStr.contains("TA") ? UserRole.TA : UserRole.MO;
        
        try {
            User user = authService.register(email, password, role);
            
            if (user != null) {
                String message = "注册成功！\n\n";
                message += "邮箱: " + user.getEmail() + "\n";
                message += "角色: " + (role == UserRole.TA ? "助教 (TA)" : "课程教师 (MO)") + "\n";
                if (role == UserRole.MO) {
                    message += "⚠️ 账号待激活，请联系管理员\n";
                }
                message += "\n请返回登录";
                
                JOptionPane.showMessageDialog(this, message, "成功", JOptionPane.INFORMATION_MESSAGE);
                
                new LoginFrame().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "注册失败，邮箱已存在", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, 
                ex.getMessage(), "输入错误", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "注册失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}