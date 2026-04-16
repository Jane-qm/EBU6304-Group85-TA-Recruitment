package auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import common.entity.User;
import common.entity.UserRole;
import common.ui.BaseFrame;

/**
 * 注册窗口
 * 采用与 LoginFrame 完全一致的 UI 风格（圆角卡片、平滑阴影、蓝色主色调）
 *
 * @author System
 * @version 2.0
 * @update 继承 BaseFrame，支持窗口最大化/还原功能
 */
public class RegisterFrame extends BaseFrame {

    private final AuthService authService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private UserRole selectedRole = UserRole.TA; // 默认选中 TA 角色

    // 统一输入框的尺寸常量
    private static final int CONTENT_WIDTH = 360;
    private static final int FIELD_HEIGHT = 56;

    public RegisterFrame() {
        super("TA Recruitment System - Register", 760, 820);
        this.authService = new AuthService();
        initUI();
    }

    @Override
    protected void initUI() {
        // 1. 底层背景面板
        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBackground(new Color(245, 247, 251));
        rootPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // 2. 中间的白色圆角卡片
        LoginFrame.RoundedPanel cardPanel = new LoginFrame.RoundedPanel(28, Color.WHITE);

        // 强制锁定卡片大小为 620x760
        Dimension cardSize = new Dimension(620, 760);
        cardPanel.setPreferredSize(cardSize);
        cardPanel.setMinimumSize(cardSize);
        cardPanel.setMaximumSize(cardSize);

        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(36, 36, 36, 36));

        // 3. 标题区域
        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(26));

        // 4. 角色选择区域 (Tabs)
        JPanel rolePanel = new LoginFrame.RoundedPanel(18, new Color(243, 246, 251));
        rolePanel.setLayout(new GridLayout(1, 3, 10, 0));
        rolePanel.setMaximumSize(new Dimension(CONTENT_WIDTH, 62));
        rolePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        rolePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JToggleButton taTab = createRoleTab("TA", true);
        JToggleButton moTab = createRoleTab("MO", false);
        JToggleButton adminTab = createRoleTab("ADMIN", false);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(taTab);
        roleGroup.add(moTab);
        roleGroup.add(adminTab);

        // 绑定角色切换事件，更新 selectedRole 并重绘按钮样式
        taTab.addActionListener(e -> {
            selectedRole = UserRole.TA;
            updateRoleTabStyles(taTab, moTab, adminTab);
        });
        moTab.addActionListener(e -> {
            selectedRole = UserRole.MO;
            updateRoleTabStyles(taTab, moTab, adminTab);
        });
        adminTab.addActionListener(e -> {
            selectedRole = UserRole.ADMIN;
            updateRoleTabStyles(taTab, moTab, adminTab);
        });

        rolePanel.add(taTab);
        rolePanel.add(moTab);
        rolePanel.add(adminTab);
        cardPanel.add(rolePanel);
        cardPanel.add(Box.createVerticalStrut(24));

        // 5. 表单输入区域
        emailField = new JTextField();
        passwordField = new JPasswordField();
        confirmField = new JPasswordField();

        cardPanel.add(createLabel("University Email"));
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(wrapField(emailField));
        cardPanel.add(Box.createVerticalStrut(16));

        cardPanel.add(createLabel("Password"));
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(wrapField(passwordField));
        cardPanel.add(Box.createVerticalStrut(16));

        cardPanel.add(createLabel("Confirm Password"));
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(wrapField(confirmField));
        cardPanel.add(Box.createVerticalStrut(32));

        // 6. 注册按钮
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 22));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(new Color(37, 99, 235));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> handleRegister());

        LoginFrame.RoundedPanel btnPanel = new LoginFrame.RoundedPanel(16, new Color(37, 99, 235));
        btnPanel.setLayout(new BorderLayout());
        btnPanel.setMaximumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.add(registerBtn, BorderLayout.CENTER);
        cardPanel.add(btnPanel);
        cardPanel.add(Box.createVerticalStrut(24));

        // 7. 返回登录页按钮
        JPanel backPanel = new JPanel();
        backPanel.setOpaque(false);
        JButton backBtn = new JButton("Back to Login");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        backBtn.setForeground(new Color(107, 114, 128));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new LoginFrame(selectedRole).setVisible(true);
            dispose();
        });

        backPanel.add(backBtn);
        cardPanel.add(backPanel);

        rootPanel.add(cardPanel);
        add(rootPanel);

        // 绑定回车键触发注册
        getRootPane().setDefaultButton(registerBtn);
    }

    /**
     * 辅助方法：统一创建文本标签
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(17, 24, 39));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * 辅助方法：给输入框套上圆角边框
     */
    private JPanel wrapField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setBorder(new EmptyBorder(0, 0, 0, 0)); // 移除原生边框

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setMaximumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                new LoginFrame.RoundedBorder(16, new Color(220, 224, 230), 1),
                new EmptyBorder(0, 16, 0, 16) // 内边距
        ));
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * 辅助方法：创建角色选择切换按钮
     */
    private JToggleButton createRoleTab(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text, selected);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new LoginFrame.RoundedBorder(14, new Color(37, 99, 235), 1));
        applyRoleTabStyle(btn, selected);
        return btn;
    }

    /**
     * 更新所有角色按钮的样式状态
     */
    private void updateRoleTabStyles(JToggleButton ta, JToggleButton mo, JToggleButton admin) {
        applyRoleTabStyle(ta, ta.isSelected());
        applyRoleTabStyle(mo, mo.isSelected());
        applyRoleTabStyle(admin, admin.isSelected());
    }

    /**
     * 应用选中/未选中的按钮颜色
     */
    private void applyRoleTabStyle(JToggleButton btn, boolean selected) {
        btn.setBackground(selected ? new Color(37, 99, 235) : new Color(243, 246, 251));
        btn.setForeground(selected ? Color.WHITE : new Color(107, 114, 128));
    }

    /**
     * 处理注册提交逻辑
     */
    private void handleRegister() {
        String email = emailField.getText().trim();
        String pwd = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        // 基础表单校验
        if (email.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
            showWarning("All fields are required.");
            return;
        }
        if (!pwd.equals(confirm)) {
            showWarning("Passwords do not match.");
            return;
        }

        try {
            // 调用 AuthService 进行注册
            User user = authService.register(email, pwd, selectedRole);
            showInfo("Registration Successful!\nStatus: " + user.getStatus());

            // Return to login page with the same role tab pre-selected.
            new LoginFrame(selectedRole).setVisible(true);
            dispose();
        } catch (Exception ex) {
            // 捕获异常（如邮箱已存在、格式错误等）
            showError(ex.getMessage());
        }
    }

    @Override
    protected void onWindowMaximized() {
        // 窗口最大化时的回调
        System.out.println("Register window maximized");
    }

    @Override
    protected void onWindowRestored() {
        // 窗口还原时的回调
        System.out.println("Register window restored");
    }
}