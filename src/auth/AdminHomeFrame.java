package auth;

import common.entity.AccountStatus;
import common.entity.User;
import common.service.UserService;

import javax.swing.*;
import java.awt.*;

public class AdminHomeFrame extends JFrame {
    private final User currentUser;
    private final UserService userService = new UserService();

    public AdminHomeFrame(User user) {
        this.currentUser = user;
        setTitle("Admin Home");
        setSize(640, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUi();
    }

    private void initUi() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Welcome, " + currentUser.getEmail() + " (ADMIN)"));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JButton approveMoBtn = new JButton("Approve demo MO account");
        approveMoBtn.addActionListener(e -> {
            User mo = userService.findByEmail("mo@test.com");
            if (mo == null) {
                JOptionPane.showMessageDialog(this, "Demo MO account not found.");
                return;
            }
            mo.setStatus(AccountStatus.ACTIVE);
            userService.saveUser(mo);
            JOptionPane.showMessageDialog(this, "MO account approved.");
        });

        panel.add(approveMoBtn);
        panel.add(logoutBtn);
        setContentPane(panel);
    }
}
