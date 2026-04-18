package auth;

import common.dao.MOJobDAO;
import common.dao.MOOfferDAO;
import common.dao.NotificationDAO;
import common.entity.AccountStatus;
import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.NotificationMessage;
import common.entity.User;
import common.entity.UserRole;
import common.service.SystemConfigService;
import common.service.UserService;
import common.util.AdminAuditLogger;
import common.util.CsvExportUtil;
import ta.dao.CVDao;
import ta.dao.TAApplicationDAO;
import ta.dao.TAProfileDAO;
import ta.entity.CVInfo;
import ta.entity.TAApplication;
import ta.entity.TAProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Admin portal.
 * Supports administrator operations such as account management,
 * system data inspection, and global configuration.
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
 *
 * @version 4.0
 * @contributor Jiaze Wang
 * @update
 * - Added global application cycle configuration support
 * - Structured the admin portal into three functional tabs
 * - Kept account management, system data inspection, and CSV export
 *
 * @version 5.0
 * @contributor Jiaze Wang
 * @update
 * - Aligned System Data with ta.* profile/application/CV data sources
 * - Removed dependency on outdated common.* profile/application/CV stores
 * - Fixed TA profile and CV dataset rendering and export consistency
 */
public class AdminHomeFrame extends JFrame {
    private final User currentUser;
    private final UserService userService = new UserService();
    private final SystemConfigService systemConfigService = new SystemConfigService();

    // Align visual style with TA portal (TAMainFrame)
    private static final Color APP_BG = new Color(248, 250, 252);
    private static final Color SIDEBAR_BG = new Color(30, 35, 45);
    private static final Color NAV_ACTIVE_BG = new Color(37, 99, 235);
    private static final Color NAV_HOVER_BG = new Color(55, 65, 81);
    private static final Color NAV_ACTIVE_FG = Color.WHITE;
    private static final Color NAV_INACTIVE_FG = new Color(156, 163, 175);

    private JButton currentActiveBtn = null;
    private JButton navAccountsBtn;
    private JButton navDataBtn;
    private JButton navCycleBtn;
    private JLabel topBarTitleLabel;

    private final TAProfileDAO taProfileDAO = new TAProfileDAO();
    private final MOJobDAO moJobDAO = new MOJobDAO();
    private final TAApplicationDAO applicationDAO = new TAApplicationDAO();
    private final CVDao cvDao = new CVDao();
    private final MOOfferDAO offerDAO = new MOOfferDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private JTable userTable;
    private DefaultTableModel userTableModel;

    private JTable dataTable;
    private DefaultTableModel dataTableModel;
    private JComboBox<String> datasetCombo;

    private JTextField cycleStartField;
    private JTextField cycleEndField;

    private JPanel mainCardPanel;
    private CardLayout cardLayout;

    private static final String CARD_ACCOUNTS = "ACCOUNTS";
    private static final String CARD_DATA = "DATA";
    private static final String CARD_CYCLE = "CYCLE";

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
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUi();
        refreshUserTable();
        refreshDataTable();
        loadCycleFields();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(APP_BG);

        add(createSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(APP_BG);
        main.add(createTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(APP_BG);
        mainCardPanel.add(createUserManagementPanel(), CARD_ACCOUNTS);
        mainCardPanel.add(createDataPanel(), CARD_DATA);
        mainCardPanel.add(createCyclePanel(), CARD_CYCLE);
        main.add(mainCardPanel, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);

        // Default selection
        setActiveButton(navAccountsBtn);
        cardLayout.show(mainCardPanel, CARD_ACCOUNTS);
        updateTopBarTitle(CARD_ACCOUNTS);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(new EmptyBorder(28, 20, 28, 20));

        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoLabel = new JLabel("TA Recruit");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoSubLabel = new JLabel("Admin Portal");
        logoSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logoSubLabel.setForeground(new Color(107, 114, 128));
        logoSubLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(logoLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(logoSubLabel);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(26));

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBackground(SIDEBAR_BG);
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel(currentUser.getEmail());
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel metaLabel = new JLabel("SUPER ADMIN");
        metaLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        metaLabel.setForeground(new Color(107, 114, 128));
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userPanel.add(userLabel);
        userPanel.add(Box.createVerticalStrut(4));
        userPanel.add(metaLabel);

        sidebar.add(userPanel);
        sidebar.add(Box.createVerticalStrut(26));

        navAccountsBtn = createNavButton("MO Account Approval", CARD_ACCOUNTS);
        navDataBtn = createNavButton("System Data", CARD_DATA);
        navCycleBtn = createNavButton("Application Cycle", CARD_CYCLE);

        sidebar.add(navAccountsBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navDataBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navCycleBtn);

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(239, 68, 68));
        logoutBtn.setOpaque(true);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(240, 46));
        logoutBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(NAV_INACTIVE_FG);
        btn.setBackground(SIDEBAR_BG);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(240, 46));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (cardLayout != null && mainCardPanel != null) {
                cardLayout.show(mainCardPanel, cardName);
            }
            setActiveButton(btn);
            updateTopBarTitle(cardName);
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != currentActiveBtn) {
                    btn.setBackground(NAV_HOVER_BG);
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != currentActiveBtn) {
                    btn.setBackground(SIDEBAR_BG);
                    btn.setForeground(NAV_INACTIVE_FG);
                }
            }
        });

        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (currentActiveBtn != null && currentActiveBtn != btn) {
            currentActiveBtn.setBackground(SIDEBAR_BG);
            currentActiveBtn.setForeground(NAV_INACTIVE_FG);
        }
        currentActiveBtn = btn;
        if (currentActiveBtn != null) {
            currentActiveBtn.setBackground(NAV_ACTIVE_BG);
            currentActiveBtn.setForeground(NAV_ACTIVE_FG);
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(APP_BG);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        topBarTitleLabel = new JLabel("Admin Portal");
        topBarTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        topBarTitleLabel.setForeground(new Color(30, 35, 45));

        topBar.add(topBarTitleLabel, BorderLayout.WEST);
        return topBar;
    }

    private void updateTopBarTitle(String cardName) {
        if (topBarTitleLabel == null) {
            return;
        }
        if (CARD_ACCOUNTS.equals(cardName)) {
            topBarTitleLabel.setText("MO Account Approval");
            return;
        }
        if (CARD_DATA.equals(cardName)) {
            topBarTitleLabel.setText("System Data");
            return;
        }
        if (CARD_CYCLE.equals(cardName)) {
            topBarTitleLabel.setText("Application Cycle");
            return;
        }
        topBarTitleLabel.setText("Admin Portal");
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
            AdminAuditLogger.log(currentUser.getEmail(), "APPROVE_MO", email);
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
            AdminAuditLogger.log(currentUser.getEmail(), "DISABLE_ACCOUNT", email);
            refreshUserTable();
            showMessage("Account disabled: " + email);
        });

        reactivateBtn.addActionListener(e -> {
            String email = getSelectedEmail();
            if (email == null) {
                return;
            }

            userService.updateAccountStatus(email, AccountStatus.ACTIVE);
            AdminAuditLogger.log(currentUser.getEmail(), "REACTIVATE_ACCOUNT", email);
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
            AdminAuditLogger.log(currentUser.getEmail(), "RESET_PASSWORD", email);
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

    private JPanel createCyclePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel tip = new JLabel("Datetime format example: 2026-04-07T09:00:00");
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);

        cycleStartField = new JTextField();
        cycleEndField = new JTextField();

        JButton saveBtn = new JButton("Save Application Cycle");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> saveApplicationCycle());

        panel.add(new JLabel("Application Start"));
        panel.add(cycleStartField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(new JLabel("Application End"));
        panel.add(cycleEndField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(tip);
        panel.add(Box.createVerticalStrut(16));
        panel.add(saveBtn);

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
                    new String[]{"TA ID", "Email", "Name", "Major", "Year", "Hours"},
                    taProfileDAO.findAll().stream()
                            .map(this::toTaProfileRow)
                            .toList()
            );
            case "Jobs" -> loadGenericTable(
                    new String[]{"Job ID", "MO User ID", "Module", "Title", "Hours", "Status"},
                    moJobDAO.findAll().stream()
                            .map(this::toJobRow)
                            .toList()
            );
            case "Applications" -> loadGenericTable(
                    new String[]{"Application ID", "TA User ID", "Job ID", "Status", "Applied At", "CV ID"},
                    applicationDAO.findAll().stream()
                            .map(this::toApplicationRow)
                            .toList()
            );
            case "CV Infos" -> loadGenericTable(
                    new String[]{"CV ID", "TA ID", "TA Email", "CV Name", "File Path", "Updated At"},
                    cvDao.findAll().stream()
                            .map(this::toCvRow)
                            .toList()
            );
            case "Offers" -> loadGenericTable(
                    new String[]{"Offer ID", "Application ID", "TA User ID", "Module", "Hours", "Status"},
                    offerDAO.findAll().stream()
                            .map(this::toOfferRow)
                            .toList()
            );
            case "Notifications" -> loadGenericTable(
                    new String[]{"Notification ID", "Recipient User ID", "Title", "Type", "Read", "Created At"},
                    notificationDAO.findAll().stream()
                            .map(this::toNotificationRow)
                            .toList()
            );
            default -> loadGenericTable(
                    new String[]{"Info"},
                    java.util.Collections.singletonList(new Object[]{"No dataset selected"})
            );
        }
    }

    private Object[] toTaProfileRow(TAProfile profile) {
        String fullName = profile.getFullName();
        String year = profile.getCurrentYear() == null ? "" : profile.getCurrentYear().getEnglishName();
        return new Object[]{
                profile.getTaId(),
                profile.getEmail(),
                fullName == null ? "" : fullName,
                profile.getMajor(),
                year,
                profile.getAvailableWorkingHours()
        };
    }

    private Object[] toJobRow(MOJob job) {
        return new Object[]{
                job.getJobId(),
                job.getMoUserId(),
                job.getModuleCode(),
                job.getTitle(),
                job.getWeeklyHours(),
                job.getStatus()
        };
    }

    private Object[] toApplicationRow(TAApplication application) {
        return new Object[]{
                application.getApplicationId(),
                application.getTaUserId(),
                application.getJobId(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getCvId()
        };
    }

    private Object[] toCvRow(CVInfo cv) {
        return new Object[]{
                cv.getCvId(),
                cv.getTaId(),
                cv.getTaEmail(),
                cv.getCvName(),
                cv.getFilePath(),
                cv.getUpdatedAt()
        };
    }

    private Object[] toOfferRow(MOOffer offer) {
        return new Object[]{
                offer.getOfferId(),
                offer.getApplicationId(),
                offer.getTaUserId(),
                offer.getModuleCode(),
                offer.getOfferedHours(),
                offer.getStatus()
        };
    }

    private Object[] toNotificationRow(NotificationMessage notification) {
        return new Object[]{
                notification.getNotificationId(),
                notification.getRecipientUserId(),
                notification.getTitle(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        };
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
                case "Users" -> CsvExportUtil.exportUsers("users.csv", userService.listAllUsers());
                case "TA Profiles" -> CsvExportUtil.exportObjects("ta_profiles.csv", taProfileDAO.findAll());
                case "Jobs" -> CsvExportUtil.exportObjects("jobs.csv", moJobDAO.findAll());
                case "Applications" -> CsvExportUtil.exportObjects("applications.csv", applicationDAO.findAll());
                case "CV Infos" -> CsvExportUtil.exportObjects("ta_cvs.csv", cvDao.findAll());
                case "Offers" -> CsvExportUtil.exportObjects("offers.csv", offerDAO.findAll());
                case "Notifications" -> CsvExportUtil.exportObjects("notifications.csv", notificationDAO.findAll());
                default -> null;
            };

            if (filePath != null) {
                AdminAuditLogger.log(currentUser.getEmail(), "EXPORT_CSV",
                        selected + " -> " + filePath.toAbsolutePath());
                showMessage("CSV exported successfully:\n" + filePath.toAbsolutePath());
            }
        } catch (Exception ex) {
            showMessage("Export failed: " + ex.getMessage());
        }
    }

    /**
     * Loads saved application cycle values into the form.
     */
    private void loadCycleFields() {
        var config = systemConfigService.getConfig();
        cycleStartField.setText(config.getApplicationStart() == null ? "" : config.getApplicationStart().toString());
        cycleEndField.setText(config.getApplicationEnd() == null ? "" : config.getApplicationEnd().toString());
    }

    /**
     * Saves the application cycle to system_config.json.
     */
    private void saveApplicationCycle() {
        try {
            LocalDateTime start = LocalDateTime.parse(cycleStartField.getText().trim());
            LocalDateTime end = LocalDateTime.parse(cycleEndField.getText().trim());

            systemConfigService.updateApplicationCycle(start, end, currentUser.getEmail());
            AdminAuditLogger.log(currentUser.getEmail(), "SAVE_CYCLE",
                    "start=" + start + " end=" + end);
            showMessage("Application cycle saved successfully.");
        } catch (DateTimeParseException ex) {
            showMessage("Invalid datetime format. Use format like 2026-04-07T09:00:00");
        } catch (Exception ex) {
            showMessage("Save failed: " + ex.getMessage());
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