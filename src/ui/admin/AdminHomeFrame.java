package ui.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import ui.auth.LoginFrame;
import modules.job.JobDAO;
import modules.notification.NotificationDAO;
import modules.user.AccountStatus;
import modules.job.Job;
import modules.notification.NotificationMessage;
import modules.user.User;
import modules.user.UserRole;
import modules.config.SystemConfigService;
import modules.user.UserService;
import infrastructure.audit.AdminAuditLogger;
import infrastructure.util.CsvExportUtil;
import modules.cv.CVDao;
import modules.application.ApplicationDAO;
import modules.profile.TAProfileDAO;
import modules.cv.CVInfo;
import modules.application.Application;
import modules.profile.TAProfile;

/**
 * Admin Portal
 * Layout: Sidebar + Main Content Area
 * Main Content: Application Cycle Settings + System Data Export
 */
public class AdminHomeFrame extends JFrame {
    private final User currentUser;
    private final UserService userService = new UserService();
    private final SystemConfigService systemConfigService = new SystemConfigService();

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

    // Application Cycle components (JCalendar)
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

    // System Data fields
    private final TAProfileDAO taProfileDAO = new TAProfileDAO();
    private final JobDAO moJobDAO = new JobDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final CVDao cvDao = new CVDao();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private JTable dataTable;
    private DefaultTableModel dataTableModel;
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
                    "Access denied. Only active super admin account can enter Admin Portal.",
                    "Permission Denied", JOptionPane.ERROR_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();
            return;
        }

        setTitle("Admin Portal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadCycleFields();
        refreshDataTable();
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

        // Create dashboard panel (Application Cycle + System Data)
        mainCardPanel.add(createDashboardPanel(), CARD_DASHBOARD);

        // Management panels
        moPanel = new MOManagementPanel(this::refreshAllPanels);
        taPanel = new TAManagementPanel(this::refreshAllPanels);
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
     * Dashboard Panel - contains Application Cycle Settings and System Data Export
     */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(APP_BG);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Top: Application Cycle Settings
        panel.add(createApplicationCyclePanel(), BorderLayout.NORTH);

        // Bottom: System Data Export
        panel.add(createSystemDataPanel(), BorderLayout.CENTER);

        return panel;
    }

    // ==================== Application Cycle Panel ====================

    private JPanel createApplicationCyclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(20, 30, 20, 30)));

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Application Cycle Settings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titlePanel.add(titleLabel);

        panel.add(titlePanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Start Date
        formPanel.add(new JLabel("Start Date:"));
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        startDateChooser.setPreferredSize(new Dimension(150, 30));
        formPanel.add(startDateChooser);

        // End Date
        formPanel.add(new JLabel("End Date:"));
        endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        endDateChooser.setPreferredSize(new Dimension(150, 30));
        formPanel.add(endDateChooser);

        // Buttons
        JButton saveBtn = createButton("Save Settings");
        saveBtn.addActionListener(e -> saveApplicationCycle());
        formPanel.add(saveBtn);

        JButton refreshBtn = createButton("Refresh");
        refreshBtn.addActionListener(e -> loadCycleFields());
        formPanel.add(refreshBtn);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==================== System Data Export Panel ====================

    private JPanel createSystemDataPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(20, 30, 20, 30)));

        // Title
        JLabel titleLabel = new JLabel("System Data Export");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Dataset selector controls
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

        // Data table
        dataTableModel = new DefaultTableModel();
        dataTable = new JTable(dataTableModel);
        dataTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dataTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
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

        // Logout button
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
        topBarTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
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
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
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
            showMessage("Application cycle saved successfully.");

        } catch (Exception ex) {
            showMessage("Save failed: " + ex.getMessage());
        }
    }

    // ==================== System Data Methods ====================

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
        for (Object[] row : rows) {
            dataTableModel.addRow(row);
        }
    }

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

    // ==================== Row Mapping Methods ====================

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

    private Object[] toJobRow(Job job) {
        return new Object[]{
                job.getJobId(),
                job.getMoUserId(),
                job.getModuleCode(),
                job.getTitle(),
                job.getWeeklyHours(),
                job.getStatus(),
                job.getApplicationDeadline()
        };
    }

    private Object[] toApplicationRow(Application application) {
        return new Object[]{
                application.getApplicationId(),
                application.getTaUserId(),
                application.getJobId(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getOfferedHours(),
                application.getOfferExpiryAt(),
                application.getRespondedAt()
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