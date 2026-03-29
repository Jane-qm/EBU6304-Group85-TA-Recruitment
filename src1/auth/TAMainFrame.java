package auth;

import common.entity.User;
import javax.swing.*;
import java.awt.*;

public class TAMainFrame extends JFrame {
    private User currentUser;

    public TAMainFrame(User user) {
        this.currentUser = user;
        setTitle("助教系统 - " + user.getEmail());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        setTitle("助教申请系统 (TA) - 欢迎: " + currentUser.getEmail());
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("欢迎回来, 助教同学 (TA)", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // TA 功能区
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton searchJobBtn = new JButton("搜索可用岗位 (Sprint 2)");
        JButton myAppsBtn = new JButton("我的申请进度");
        
        centerPanel.add(searchJobBtn);
        centerPanel.add(myAppsBtn);
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