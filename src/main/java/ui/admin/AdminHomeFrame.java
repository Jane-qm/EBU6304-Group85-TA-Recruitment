package ui.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import infrastructure.audit.AdminAuditLogger;
import infrastructure.util.CsvExportUtil;
import modules.application.Application;
import modules.application.ApplicationDAO;
import modules.auth.AuthService;
import modules.config.SystemConfigService;
import modules.cv.CVDao;
import modules.cv.CVInfo;
import modules.job.Job;
import modules.job.JobDAO;
import modules.notification.NotificationDAO;
import modules.notification.NotificationMessage;
import modules.profile.TAProfile;
import modules.profile.TAProfileDAO;
import modules.user.User;
import modules.user.UserService;
import ui.auth.LoginFrame;
import ui.common.TableScrollUtil;

/**
 * Admin Portal
 * Layout: Top - Change Password, Middle - Recruitment period (MO publish window), Bottom - System Data Export
 *
 * @version 1.1
 * @contributor Jiaze Wang
 * @update
 * - Aligned Admin access messages with the dual seeded admin policy
 */
public class AdminHomeFrame extends JFrame {
    private final User currentUser;
    private final UserService userService = UserService.getInstance();
    private final SystemConfigService systemConfigService = new SystemConfigService();
    private static final String ADMIN_ACCESS_DENIED_MESSAGE =
            "Only approved active system administrator accounts can access Admin Portal.";

    // Color scheme for sidebar
    private static final Color APP_BG = new Color(248, 250, 252);
    private static final Color SIDEBAR_BG = new Color(30, 35, 45);
    private static final Color NAV_ACTIVE_BG = new Color(37, 99, 235);
    private static final Color NAV_HOVER_BG = new Color(55, 65, 81);
    private static final Color NAV_ACTIVE_FG = Color.WHITE;
    private static final Color NAV_INACTIVE_FG = new Color(156, 163, 175);

    private JButton currentActiveBtn = null;
    private JButton navDashboardBtn;
    private JButton navMOBtn;
    private JButton navTABtn;
    private JButton navCourseBtn;
    private JLabel topBarTitleLabel;

    // Change Password fields
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    // Recruitment period (JCalendar); stored as applicationStart/applicationEnd in config
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

    // System Data fields
    private final TAProfileDAO taProfileDAO = TAProfileDAO.getInstance();
    private final JobDAO moJobDAO = new JobDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final CVDao cvDao = CVDao.getInstance();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private JTable dataTable;
    private DefaultTableModel dataTableModel;
    private JScrollPane dataTableScrollPane;
    private JComboBox<String> datasetCombo;

    // Main content
    private JPanel mainCardPanel;
    private CardLayout cardLayout;

    // Panel instances
    private MOManagementPanel moPanel;
    private TAManagementPanel taPanel;
    private CourseManagementPanel coursePanel;

    // Card constants
    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_MO = "MO";
    private static final String CARD_TA = "TA";
    private static final String CARD_COURSE = "COURSE";

    public AdminHomeFrame(User user) {
        this.currentUser = user;

        if (!userService.isStrictAdmin(user)) {
            JOptionPane.showMessageDialog(null,
                    ADMIN_ACCESS_DENIED_MESSAGE,
                    "Permission Denied", JOptionPane.ERROR_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();
            return;
        }

        setTitle("Admin Portal");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadCycleFields();
        refreshDataTable();

        SwingUtilities.invokeLater(() ->
                setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH));
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(APP_BG);

        // Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Main content area with CardLayout
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(APP_BG);
        main.add(createTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(APP_BG);

        // Dashboard: Change Password + Recruitment period + System Data Export
        mainCardPanel.add(createDashboardPanel(), CARD_DASHBOARD);

        // Management panels
        moPanel = new MOManagementPanel(this::refreshAllPanels, currentUser.getEmail());
        taPanel = new TAManagementPanel(this::refreshAllPanels, currentUser.getEmail());
        coursePanel = new CourseManagementPanel();

        mainCardPanel.add(moPanel, CARD_MO);
        mainCardPanel.add(taPanel, CARD_TA);
        mainCardPanel.add(coursePanel, CARD_COURSE);

        main.add(mainCardPanel, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        // Default selection - show dashboard
        setActiveButton(navDashboardBtn);
        cardLayout.show(mainCardPanel, CARD_DASHBOARD);
        updateTopBarTitle(CARD_DASHBOARD);
    }

    /**
     * Dashboard Panel - contains Change Password, Recruitment period, System Data Export
     */
    private JPanel createDashboardPanel() {
        JPanel inner = new JPanel(new BorderLayout(15, 15));
        inner.setBackground(APP_BG);
        inner.setBorder(new EmptyBorder(20, 30, 20, 30));

        inner.add(createChangePasswordPanel(), BorderLayout.NORTH);
        inner.add(createApplicationCyclePanel(), BorderLayout.CENTER);
        inner.add(createSystemDataPanel(), BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(inner,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(APP_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(APP_BG);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ==================== Top Panel: Change Password ====================

    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(20, 30, 20, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 35, 45));
        panel.add(titleLabel, gbc);

        // Admin info
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        JLabel adminLabel = new JLabel("Admin: " + currentUser.getEmail());
        adminLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        adminLabel.setForeground(new Color(107, 114, 128));
        panel.add(adminLabel, gbc);

        // Old Password
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Current Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        oldPasswordField = new JPasswordField(20);
        panel.add(oldPasswordField, gbc);

        // New Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        newPasswordField = new JPasswordField(20);
        panel.add(newPasswordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        // Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton changeBtn = createButton("Update Password");
        changeBtn.addActionListener(e -> changePassword());
        panel.add(changeBtn, gbc);

        return panel;
    }

    // ==================== Center Panel: Recruitment period ====================

    private JPanel createApplicationCyclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(20, 30, 20, 30)));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Recruitment Period");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(titleLabel);

        titlePanel.add(Box.createVerticalStrut(6));


        panel.add(titlePanel, BorderLayout.NORTH);

        // Stacked rows so narrow windows do not clip the date pickers
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        contentPanel.add(new JLabel("Recruitment start (date):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        startDateChooser.setPreferredSize(new Dimension(150, 32));
        contentPanel.add(startDateChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        contentPanel.add(new JLabel("Recruitment end (date):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        endDateChooser.setPreferredSize(new Dimension(150, 32));
        contentPanel.add(endDateChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveBtn = createButton("Save Settings");
        saveBtn.addActionListener(e -> saveApplicationCycle());
        buttonPanel.add(saveBtn);

        JButton refreshBtn = createButton("Refresh");
        refreshBtn.addActionListener(e -> loadCycleFields());
        buttonPanel.add(refreshBtn);

        contentPanel.add(buttonPanel, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==================== Bottom Panel: System Data Export ====================

    private JPanel createSystemDataPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(20, 30, 20, 30)));

        JLabel titleLabel = new JLabel("System Data Export");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        datasetCombo = new JComboBox<>(new String[]{
                "Users", "TA Profiles", "Jobs", "Applications", "CV Infos", "Notifications"
        });
        datasetCombo.setPreferredSize(new Dimension(150, 28));

        JButton loadBtn = createButton("Load Dataset");
        loadBtn.addActionListener(e -> refreshDataTable());

        JButton exportBtn = createButton("Export CSV");
        exportBtn.addActionListener(e -> exportCurrentDataset());

        controlPanel.add(new JLabel("Dataset:"));
        controlPanel.add(datasetCombo);
        controlPanel.add(loadBtn);
        controlPanel.add(exportBtn);
        panel.add(controlPanel, BorderLayout.CENTER);

        dataTableModel = new DefaultTableModel();
        dataTable = new JTable(dataTableModel);
        dataTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        dataTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));

        dataTableScrollPane = TableScrollUtil.wrapTable(dataTable);
        dataTableScrollPane.setPreferredSize(new Dimension(480, 220));
        dataTableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(dataTableScrollPane, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== Change Password Logic ====================

    private void changePassword() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("All fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("New passwords do not match.");
            return;
        }

        if (newPassword.length() < 6) {
            showMessage("Password must be at least 6 characters.");
            return;
        }

        // Use AuthService so the same static UserService as LoginFrame is updated in memory.
        AuthService authService = new AuthService();
        User user = authService.login(currentUser.getEmail(), oldPassword);
        if (user == null) {
            showMessage("Current password is incorrect.");
            return;
        }

        // Update password
        try {
            authService.resetPassword(currentUser.getEmail(), newPassword);

            // Clear fields
            oldPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");

            showMessage("Password changed successfully.");

        } catch (Exception e) {
            showMessage("Failed to change password: " + e.getMessage());
        }
    }

    // ==================== Sidebar ====================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBorder(new EmptyBorder(28, 20, 28, 20));

        // Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoLabel = new JLabel("TA Recruit");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoSubLabel = new JLabel("Admin Portal");
        logoSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        logoSubLabel.setForeground(new Color(107, 114, 128));
        logoSubLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(logoLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(logoSubLabel);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(36));

        // Navigation buttons
        navDashboardBtn = createNavButton("Dashboard", CARD_DASHBOARD);
        navMOBtn = createNavButton("MO Management", CARD_MO);
        navTABtn = createNavButton("TA Management", CARD_TA);
        navCourseBtn = createNavButton("Course Management", CARD_COURSE);

        sidebar.add(navDashboardBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navMOBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navTABtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navCourseBtn);

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = createNavButton("Logout", null);
        logoutBtn.setForeground(new Color(239, 68, 68));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setForeground(NAV_INACTIVE_FG);
        btn.setBackground(SIDEBAR_BG);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 46));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (cardName != null) {
            btn.addActionListener(e -> {
                if (cardLayout != null && mainCardPanel != null) {
                    cardLayout.show(mainCardPanel, cardName);
                }
                setActiveButton(btn);
                updateTopBarTitle(cardName);
            });
        }

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
        topBarTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        topBarTitleLabel.setForeground(new Color(30, 35, 45));

        topBar.add(topBarTitleLabel, BorderLayout.WEST);
        return topBar;
    }

    private void updateTopBarTitle(String cardName) {
        if (topBarTitleLabel == null) return;
        switch (cardName) {
            case CARD_DASHBOARD:
                topBarTitleLabel.setText("Dashboard");
                break;
            case CARD_MO:
                topBarTitleLabel.setText("MO Management");
                break;
            case CARD_TA:
                topBarTitleLabel.setText("TA Management");
                break;
            case CARD_COURSE:
                topBarTitleLabel.setText("Course Management");
                break;
            default:
                topBarTitleLabel.setText("Admin Portal");
        }
    }

    // ==================== Helper Methods ====================

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadCycleFields() {
        var config = systemConfigService.getConfig();
        if (config.getApplicationStart() != null) {
            startDateChooser.setDate(Date.from(config.getApplicationStart()
                    .atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (config.getApplicationEnd() != null) {
            endDateChooser.setDate(Date.from(config.getApplicationEnd()
                    .atZone(ZoneId.systemDefault()).toInstant()));
        }
    }

    private void saveApplicationCycle() {
        try {
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();

            if (startDate == null || endDate == null) {
                showMessage("Please select both start and end dates.");
                return;
            }

            LocalDateTime start = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
            LocalDateTime end = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

            if (end.isBefore(start)) {
                showMessage("End date must be after start date.");
                return;
            }

            systemConfigService.updateApplicationCycle(start, end, currentUser.getEmail());
            AdminAuditLogger.log(currentUser.getEmail(), "SAVE_CYCLE",
                    "start=" + start + " end=" + end);
            loadCycleFields();
            showMessage("Recruitment period saved successfully.");

        } catch (Exception ex) {
            showMessage("Save failed: " + ex.getMessage());
        }
    }

    private void refreshDataTable() {
        String selected = datasetCombo == null ? "Users" : (String) datasetCombo.getSelectedItem();
        if (selected == null) selected = "Users";

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
                    new String[]{"Job ID", "MO User ID", "Module", "Title", "Hours", "Status", "Deadline"},
                    moJobDAO.findAll().stream()
                            .map(this::toJobRow)
                            .toList()
            );
            case "Applications" -> loadGenericTable(
                    new String[]{"Application ID", "TA User ID", "Job ID", "Status", "Applied At",
                            "Offered Hours", "Offer Expiry", "Responded At"},
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

    private void loadGenericTable(String[] columns, List<Object[]> rows) {
        dataTableModel.setDataVector(new Object[0][0], columns);
        for (Object[] row : rows) dataTableModel.addRow(row);
        TableScrollUtil.autoSizeColumnsFromContent(dataTable, 12, 420);
        if (dataTableScrollPane != null && dataTable.getColumnCount() > 0) {
            TableScrollUtil.installResponsiveColumns(dataTable, dataTableScrollPane,
                    TableScrollUtil.flexSpecsFromCurrentWidths(dataTable, 0.42));
        }
    }

    private void exportCurrentDataset() {
        String selected = (String) datasetCombo.getSelectedItem();
        if (selected == null) return;

        try {
            Path filePath = switch (selected) {
                case "Users" -> CsvExportUtil.exportUsers("users.csv", userService.listAllUsers());
                case "TA Profiles" -> CsvExportUtil.exportObjects("ta_profiles.csv", taProfileDAO.findAll());
                case "Jobs" -> CsvExportUtil.exportObjects("jobs.csv", moJobDAO.findAll());
                case "Applications" -> CsvExportUtil.exportObjects("applications.csv", applicationDAO.findAll());
                case "CV Infos" -> CsvExportUtil.exportObjects("ta_cvs.csv", cvDao.findAll());
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

    private Object[] toTaProfileRow(TAProfile profile) {
        String fullName = profile.getFullName();
        String year = profile.getCurrentYear() == null ? "" : profile.getCurrentYear().getEnglishName();
        return new Object[]{profile.getTaId(), profile.getEmail(), fullName == null ? "" : fullName,
                profile.getMajor(), year, profile.getAvailableWorkingHours()};
    }

    private Object[] toJobRow(Job job) {
        return new Object[]{job.getJobId(), job.getMoUserId(), job.getModuleCode(), job.getTitle(),
                job.getWeeklyHours(), job.getStatus(), job.getApplicationDeadline()};
    }

    private Object[] toApplicationRow(Application application) {
        return new Object[]{application.getApplicationId(), application.getTaUserId(), application.getJobId(),
                application.getStatus(), application.getAppliedAt(), application.getOfferedHours(),
                application.getOfferExpiryAt(), application.getRespondedAt()};
    }

    private Object[] toCvRow(CVInfo cv) {
        return new Object[]{cv.getCvId(), cv.getTaId(), cv.getTaEmail(), cv.getCvName(),
                cv.getFilePath(), cv.getUpdatedAt()};
    }

    private Object[] toNotificationRow(NotificationMessage notification) {
        return new Object[]{notification.getNotificationId(), notification.getRecipientUserId(),
                notification.getTitle(), notification.getType(), notification.isRead(), notification.getCreatedAt()};
    }

    private void refreshAllPanels() {
        if (moPanel != null) moPanel.refresh();
        if (taPanel != null) taPanel.refresh();
        if (coursePanel != null) coursePanel.refresh();
        refreshDataTable();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}