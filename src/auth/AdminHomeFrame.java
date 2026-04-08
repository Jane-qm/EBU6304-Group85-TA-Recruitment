package auth;

import common.entity.AccountStatus;
import common.entity.User;
import common.entity.UserRole;
import common.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Admin portal.
 * Supports administrator account management operations.
 *
 * @version 2.0
 * @contributor Jiaze Wang
 * @update
 * - Expanded the original demo admin page into a structured admin portal
 * - Added strict super-admin access validation
 * - Added MO approval, account disable/reactivate, and password reset actions
 */
public class AdminHomeFrame extends JFrame {
    private final User currentUser;
    private final UserService userService = new UserService();

    private JTable userTable;
    private DefaultTableModel userTableModel;

    public AdminHomeFrame(User user) {
        this.currentUser = user;

        if (!userService.isStrictAdmin(user)) {
            JOptionPane.showMessageDialog(
                    null,
                    "Access denied. Only active super admin account admin@test.com can enter Admin Portal.",
                    "Permission Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            new LoginFrame().setVisible(true);
            dispose();
            return;
        }

        setTitle("Admin Portal");
        setSize(920, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUi();
        refreshUserTable();
    }

    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Welcome, " + currentUser.getEmail() + " (SUPER ADMIN)");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);

        userTableModel = new DefaultTableModel(
                new Object[]{"User ID", "Email", "Role", "Status", "Last Login"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(userTableModel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve MO");
        JButton disableBtn = new JButton("Disable Account");
        JButton reactivateBtn = new JButton("Reactivate Account");
        JButton resetPwdBtn = new JButton("Reset Password");

        refreshBtn.addActionListener(e -> refreshUserTable());

        approveBtn.addActionListener(e -> {
            String email = getSelectedEmail();
            if (email == null) {
                return;
            }

            User user = userService.findByEmail(email);
            if (user == null) {
                showMessage("User not found.");
                return;
            }
            if (user.getRole() != UserRole.MO) {
                showMessage("Only MO accounts can be approved.");
                return;
            }

            userService.approveMoAccount(email);
            refreshUserTable();
            showMessage("MO account approved: " + email);
        });

        disableBtn.addActionListener(e -> {
            String email = getSelectedEmail();
            if (email == null) {
                return;
            }

            if ("admin@test.com".equalsIgnoreCase(email)) {
                showMessage("Super admin cannot be disabled.");
                return;
            }

            userService.disableAccount(email);
            refreshUserTable();
            showMessage("Account disabled: " + email);
        });

        reactivateBtn.addActionListener(e -> {
            String email = getSelectedEmail();
            if (email == null) {
                return;
            }

            userService.updateAccountStatus(email, AccountStatus.ACTIVE);
            refreshUserTable();
            showMessage("Account reactivated: " + email);
        });

        resetPwdBtn.addActionListener(e -> {
            String email = getSelectedEmail();
            if (email == null) {
                return;
            }

            String newPassword = JOptionPane.showInputDialog(this, "Enter new password for " + email);
            if (newPassword == null || newPassword.isBlank()) {
                return;
            }

            userService.resetPasswordByAdmin(email, newPassword.trim());
            showMessage("Password reset completed for: " + email);
        });

        buttonPanel.add(refreshBtn);
        buttonPanel.add(approveBtn);
        buttonPanel.add(disableBtn);
        buttonPanel.add(reactivateBtn);
        buttonPanel.add(resetPwdBtn);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(new JScrollPane(userTable), BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    /**
     * Reloads all users into the table.
     */
    private void refreshUserTable() {
        userTableModel.setRowCount(0);
        for (User user : userService.listAllUsers()) {
            userTableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus(),
                    user.getLastLogin()
            });
        }
    }

    /**
     * Returns the email from the selected row.
     */
    private String getSelectedEmail() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            showMessage("Please select a user first.");
            return null;
        }
        Object email = userTableModel.getValueAt(row, 1);
        return email == null ? null : email.toString();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}