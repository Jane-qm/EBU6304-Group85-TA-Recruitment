package auth;

import common.entity.User;
import javax.swing.*;
import java.awt.*;

public class MOMainFrame extends JFrame {
    private User currentUser;

    public MOMainFrame(User user) {
        this.currentUser = user;
        //【下面三行新加的】
        setTitle("教师系统 - " + user.getEmail());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        setTitle("教师管理系统 (MO) - 欢迎: " + currentUser.getEmail());
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 主布局
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 顶部信息
        JLabel welcomeLabel = new JLabel("欢迎回来, 课程教师 (MO)", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // 中间功能区（预留 Sprint 2 功能）
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton postJobBtn = new JButton("发布新助教岗位 (Sprint 2)");
        JButton viewAppsBtn = new JButton("查看申请记录");
        
        centerPanel.add(postJobBtn);
        centerPanel.add(viewAppsBtn);
        panel.add(centerPanel, BorderLayout.CENTER);

        // 底部退出按钮
        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        panel.add(logoutBtn, BorderLayout.SOUTH);

        add(panel);
    }
}