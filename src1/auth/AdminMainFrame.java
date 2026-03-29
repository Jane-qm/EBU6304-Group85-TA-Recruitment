package auth;

import common.entity.User;
import javax.swing.*;
import java.awt.*;

public class AdminMainFrame extends JFrame {
    private User currentUser;

    public AdminMainFrame(User user) {
        this.currentUser = user;
        setTitle("管理员系统 - " + user.getEmail());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        setTitle("系统管理后台 (Admin) - 欢迎: " + currentUser.getEmail());
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("系统管理员控制台", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // 管理功能
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10)); 
        JButton manageUsersBtn = new JButton("管理待激活的 MO 账号");
        JButton systemLogBtn = new JButton("查看系统日志");

        // 模拟激活逻辑
        manageUsersBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "正在获取 PENDING 状态的教师列表...");
        });

        centerPanel.add(manageUsersBtn);
        centerPanel.add(systemLogBtn);
        panel.add(centerPanel, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        panel.add(logoutBtn, BorderLayout.SOUTH);

        add(panel);
    }
}