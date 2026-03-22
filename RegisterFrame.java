package auth;

import common.entity.User;
import common.entity.UserRole;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 * 注册与个人资料窗口 (成员 6 负责)
 * 整合 Requirement A (注册) 与 Requirement C (资料完善)
 * * @author Can Chen
 * @version 1.1
 */
public class RegisterFrame extends JFrame {
    
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private final AuthService authService = new AuthService();

    // Requirement A 组件 (注册)
    private JTextField emailField;
    private JPasswordField passwordField, confirmField;
    private JComboBox<String> roleCombo;
    private JLabel strengthLabel;

    // Requirement C 组件 (个人资料)
    private JTextField nameField, majorField, gradeField, skillField, hoursField;
    private User currentUser; // 记录当前注册成功的用户

    public RegisterFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("TA招聘系统 - 账户与资料设置");
        setSize(550, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 初始化两个面板
        container.add(createRegisterPanel(), "RegisterCard");
        container.add(createProfilePanel(), "ProfileCard");

        add(container);
        cardLayout.show(container, "RegisterCard");
    }

    /**
     * Requirement A: 注册界面布局
     */
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("第一步：账户注册 (Requirement A)");
        title.setFont(new Font("微软雅黑", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        // 邮箱
        gbc.gridwidth = 1; gbc.gridy = 1;
        panel.add(new JLabel("邮箱:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1; panel.add(emailField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("密码:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1; panel.add(passwordField, gbc);

        // 密码强度实时显示
        gbc.gridx = 1; gbc.gridy = 3;
        strengthLabel = new JLabel("强度: 未输入");
        strengthLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        panel.add(strengthLabel, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("确认密码:"), gbc);
        confirmField = new JPasswordField(20);
        gbc.gridx = 1; panel.add(confirmField, gbc);

        // 角色
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("角色:"), gbc);
        roleCombo = new JComboBox<>(new String[]{"助教 (TA)", "课程教师 (MO)"});
        gbc.gridx = 1; panel.add(roleCombo, gbc);

        // 注册按钮
        JButton nextBtn = new JButton("下一步：完善资料");
        nextBtn.addActionListener(e -> handleRegister());
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(nextBtn, gbc);

        // 密码监听
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updateStrengthUI();
            }
        });

        return panel;
    }

    /**
     * Requirement C: 个人资料填写界面
     */
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel title = new JLabel("第二步：完善个人资料 (Requirement C)");
        title.setFont(new Font("微软雅黑", Font.BOLD, 16));
        title.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        // 姓名
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("真实姓名:"), gbc);
        nameField = new JTextField();
        gbc.gridx = 1; panel.add(nameField, gbc);

        // 专业
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("专业:"), gbc);
        majorField = new JTextField();
        gbc.gridx = 1; panel.add(majorField, gbc);

        // 年级
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("年级 (如 Year 2):"), gbc);
        gradeField = new JTextField();
        gbc.gridx = 1; panel.add(gradeField, gbc);

        // 技能标签
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("技能标签 (逗号隔开):"), gbc);
        skillField = new JTextField();
        gbc.gridx = 1; panel.add(skillField, gbc);

        // 可用工时
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("每周可用工时:"), gbc);
        hoursField = new JTextField();
        gbc.gridx = 1; panel.add(hoursField, gbc);

        // 保存按钮
        JButton saveBtn = new JButton("保存并完成注册");
        saveBtn.setBackground(new Color(0, 153, 76));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> handleSaveProfile());
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        panel.add(saveBtn, gbc);

        return panel;
    }

    private void handleRegister() {
        String email = emailField.getText().trim();
        String pwd = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (email.isEmpty() || pwd.isEmpty() || !pwd.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "请检查输入及密码一致性", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UserRole role = roleCombo.getSelectedItem().toString().contains("TA") ? UserRole.TA : UserRole.MO;
            currentUser = authService.register(email, pwd, role);
            
            if (currentUser != null) {
                // 注册成功，切换到 Requirement C 面板
                cardLayout.show(container, "ProfileCard");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "注册失败: " + ex.getMessage());
        }
    }

    private void handleSaveProfile() {
        // 这里应调用成员 3 负责的 UserService 进行数据保存
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "姓名不能为空");
            return;
        }

        JOptionPane.showMessageDialog(this, "资料保存成功！欢迎使用系统。");
        // 完成任务后返回登录
        dispose();
        // new LoginFrame().setVisible(true); // 成员 5 负责的部分
    }

    private void updateStrengthUI() {
        String pwd = new String(passwordField.getPassword());
        int score = 0;
        if (pwd.length() >= 8) score++;
        if (pwd.matches(".*\\d.*")) score++;
        if (pwd.matches(".*[A-Z].*")) score++;
        
        if (score <= 1) { strengthLabel.setText("强度: 弱"); strengthLabel.setForeground(Color.RED); }
        else if (score == 2) { strengthLabel.setText("强度: 中"); strengthLabel.setForeground(Color.ORANGE); }
        else { strengthLabel.setText("强度: 强"); strengthLabel.setForeground(new Color(0, 150, 0)); }
    }
}