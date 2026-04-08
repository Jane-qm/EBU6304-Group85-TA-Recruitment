package auth;

import common.dao.CVInfoDAO;
import common.dao.MOJobDAO;
import common.dao.MOOfferDAO;
import common.dao.NotificationDAO;
import common.dao.TAApplicationDAO;
import common.dao.TAProfileDAO;
import common.entity.AccountStatus;
import common.entity.User;
import common.entity.UserRole;
import common.service.UserService;
import common.util.CsvExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Admin portal.
 * Supports administrator operations such as account management
 * and system data inspection.
 *
 * @version 2.0
 * @contributor Jiaze Wang
 * @update
 * - Expanded the original demo admin page into a structured admin portal
 * - Added strict super-admin access validation
 * - Added MO approval, account disable/reactivate, and password reset actions
 *
 * @version 3.0
 * @contributor Jiaze Wang
 * @update
 * - Added system-wide data viewing
 * - Added CSV export for core datasets
 * - Kept existing account lifecycle management functions
 */
public class AdminHomeFrame extends JFrame {
    private final User currentUser;
    private final UserService userService = new UserService();

    private final TAProfileDAO taProfileDAO = new TAProfileDAO();
    private final MOJobDAO moJobDAO = new MOJobDAO();
    private final TAApplicationDAO applicationDAO = new TAApplicationDAO();
    private final CVInfoDAO cvInfoDAO = new CVInfoDAO();
    private final MOOfferDAO offerDAO = new MOOfferDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private JTable userTable;
    private DefaultTableModel userTableModel;

    private JTable dataTable;
    private DefaultTableModel dataTableModel;
    private JComboBox<String> datasetCombo;

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
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUi();
        refreshUserTable();
        refreshDataTable();
    }

    private void initUi() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("MO Account Approval", createUserManagementPanel());
        tabs.addTab("System Data", createDataPanel());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel title = new JLabel("Welcome, " + currentUser.getEmail() + " (SUPER ADMIN)");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        userTableModel = new DefaultTableModel(
                new Object[]{"User ID", "Email", "Role", "Status", "Last Login"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(userTableModel);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

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

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createDataPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        datasetCombo = new JComboBox<>(new String[]{
                "Users",
                "TA Profiles",
                "Jobs",
                "Applications",
                "CV Infos",
                "Offers",
                "Notifications"
        });

        JButton loadBtn = new JButton("Load Dataset");
        loadBtn.addActionListener(e -> refreshDataTable());

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.addActionListener(e -> exportCurrentDataset());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Dataset: "));
        top.add(datasetCombo);
        top.add(loadBtn);
        top.add(exportBtn);

        dataTableModel = new DefaultTableModel();
        dataTable = new JTable(dataTableModel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(dataTable), BorderLayout.CENTER);
        return panel;
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
     * Reloads the selected dataset into the data table.
     */
    private void refreshDataTable() {
        String selected = datasetCombo == null ? "Users" : (String) datasetCombo.getSelectedItem();
        if (selected == null) {
            selected = "Users";
        }

        switch (selected) {
            case "Users" -> loadGenericTable(
                    new String[]{"User ID", "Email", "Role", "Status", "Last Login"},
                    userService.listAllUsers().stream()
                            .map(u -> new Object[]{
                                    u.getUserId(),
                                    u.getEmail(),
                                    u.getRole(),
                                    u.getStatus(),
                                    u.getLastLogin()
                            })
                            .toList()
            );
            case "TA Profiles" -> loadGenericTable(
                    new String[]{"Profile ID", "User ID", "Name", "Major", "Grade", "Hours"},
                    taProfileDAO.findAll().stream()
                            .map(p -> new Object[]{
                                    p.getProfileId(),
                                    p.getUserId(),
                                    p.getName(),
                                    p.getMajor(),
                                    p.getGrade(),
                                    p.getAvailableWorkingHours()
                            })
                            .toList()
            );
            case "Jobs" -> loadGenericTable(
                    new String[]{"Job ID", "MO User ID", "Module", "Title", "Hours", "Status"},
                    moJobDAO.findAll().stream()
                            .map(j -> new Object[]{
                                    j.getJobId(),
                                    j.getMoUserId(),
                                    j.getModuleCode(),
                                    j.getTitle(),
                                    j.getWeeklyHours(),
                                    j.getStatus()
                            })
                            .toList()
            );
            case "Applications" -> loadGenericTable(
                    new String[]{"Application ID", "TA User ID", "Job ID", "Status", "Applied At"},
                    applicationDAO.findAll().stream()
                            .map(a -> new Object[]{
                                    a.getApplicationId(),
                                    a.getTaUserId(),
                                    a.getJobId(),
                                    a.getStatus(),
                                    a.getAppliedAt()
                            })
                            .toList()
            );
            case "CV Infos" -> loadGenericTable(
                    new String[]{"CV ID", "User ID", "Education", "File Path", "Updated At"},
                    cvInfoDAO.findAll().stream()
                            .map(c -> new Object[]{
                                    c.getCvId(),
                                    c.getUserId(),
                                    c.getEducationSummary(),
                                    c.getFilePath(),
                                    c.getUpdatedAt()
                            })
                            .toList()
            );
            case "Offers" -> loadGenericTable(
                    new String[]{"Offer ID", "Application ID", "TA User ID", "Module", "Hours", "Status"},
                    offerDAO.findAll().stream()
                            .map(o -> new Object[]{
                                    o.getOfferId(),
                                    o.getApplicationId(),
                                    o.getTaUserId(),
                                    o.getModuleCode(),
                                    o.getOfferedHours(),
                                    o.getStatus()
                            })
                            .toList()
            );
            case "Notifications" -> loadGenericTable(
                    new String[]{"Notification ID", "Recipient User ID", "Title", "Type", "Read", "Created At"},
                    notificationDAO.findAll().stream()
                            .map(n -> new Object[]{
                                    n.getNotificationId(),
                                    n.getRecipientUserId(),
                                    n.getTitle(),
                                    n.getType(),
                                    n.isRead(),
                                    n.getCreatedAt()
                            })
                            .toList()
            );
            default -> loadGenericTable(new String[]{"Info"}, List.of(new Object[]{"No dataset selected"}));
        }
    }

    /**
     * Loads rows into the generic data table.
     */
    private void loadGenericTable(String[] columns, List<Object[]> rows) {
        dataTableModel.setDataVector(new Object[0][0], columns);
        for (Object[] row : rows) {
            dataTableModel.addRow(row);
        }
    }

    /**
     * Exports the current dataset to a CSV file.
     */
    private void exportCurrentDataset() {
        String selected = (String) datasetCombo.getSelectedItem();
        if (selected == null) {
            return;
        }

        try {
            Path filePath = switch (selected) {
                case "Users" -> CsvExportUtil.exportObjects("users.csv", userService.listAllUsers());
                case "TA Profiles" -> CsvExportUtil.exportObjects("ta_profiles.csv", taProfileDAO.findAll());
                case "Jobs" -> CsvExportUtil.exportObjects("jobs.csv", moJobDAO.findAll());
                case "Applications" -> CsvExportUtil.exportObjects("applications.csv", applicationDAO.findAll());
                case "CV Infos" -> CsvExportUtil.exportObjects("cv_infos.csv", cvInfoDAO.findAll());
                case "Offers" -> CsvExportUtil.exportObjects("offers.csv", offerDAO.findAll());
                case "Notifications" -> CsvExportUtil.exportObjects("notifications.csv", notificationDAO.findAll());
                default -> null;
            };

            if (filePath != null) {
                showMessage("CSV exported successfully:\n" + filePath.toAbsolutePath());
            }
        } catch (Exception ex) {
            showMessage("Export failed: " + ex.getMessage());
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