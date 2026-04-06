package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import auth.LoginFrame;
import common.entity.MOJob;
import common.entity.TA;
import common.entity.User;
import common.service.MOJobService;
import common.service.NotificationService;
import common.ui.BaseFrame;
import common.ui.NotificationPopup;
import ta.entity.TAApplication;
import ta.entity.TAProfile;
import ta.service.TAApplicationService;
import ta.service.TAProfileService;           // 修改：改为 ta.entity

public class TAMainFrame extends BaseFrame {
    private final TA ta;
    private final TAProfileService profileService;
    private final TAApplicationService applicationService;  // 直接使用，不再重复声明
    private final MOJobService jobService;
    private final NotificationService notificationService;

    // 颜色常量
    private static final Color SIDEBAR_BG = new Color(30, 35, 45);
    private static final Color ACCEPTED_COLOR = new Color(34, 197, 94);
    private static final Color PENDING_COLOR = new Color(234, 179, 8);
    private static final Color REJECTED_COLOR = new Color(239, 68, 68);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);

    public TAMainFrame(User user) {
        super("TA Recruitment System - Dashboard", 1200, 750);
        this.ta = (TA) user;
        this.profileService = new TAProfileService();
        this.applicationService = new TAApplicationService();
        this.jobService = new MOJobService();
        this.notificationService = new NotificationService();
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));

        mainPanel.add(createSidebar(), BorderLayout.WEST);
        mainPanel.add(createMainContent(), BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel logoLabel = new JLabel("TA Recruitment");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(logoLabel);
        sidebar.add(Box.createVerticalStrut(32));

        sidebar.add(createNavButton("Dashboard", true));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Course Catalog", false));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("My Applications", false));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("My Profile", false));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Workload Tracking", false));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(16));
        
        JLabel userLabel = new JLabel(ta.getEmail());
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userLabel.setForeground(new Color(156, 163, 175));
        userLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(userLabel);
        
        sidebar.add(Box.createVerticalStrut(12));
        
        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        logoutBtn.setForeground(new Color(239, 68, 68));
        logoutBtn.setBackground(SIDEBAR_BG);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNavButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isActive) {
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(59, 130, 246, 50));
        } else {
            button.setForeground(new Color(156, 163, 175));
            button.setBackground(SIDEBAR_BG);
        }
        
        button.addActionListener(e -> {
            String cmd = text;
            switch (cmd) {
                case "My Profile":
                    new TAProfileFrame(ta).setVisible(true);
                    dispose();
                    break;
                case "Course Catalog":
                    showInfo("Course Catalog - Coming Soon");
                    break;
                case "My Applications":
                    refreshApplicationsTable();
                    break;
                case "Workload Tracking":
                    showInfo("Workload Tracking - Coming Soon");
                    break;
                default:
                    break;
            }
        });
        
        return button;
    }

    private JScrollPane createMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(248, 250, 252));
        content.setBorder(new EmptyBorder(30, 35, 30, 35));

        TAProfile profile = profileService.getProfileByTaId(ta.getUserId());
        String displayName = (profile != null && profile.getFullName() != null && !profile.getFullName().isEmpty()) 
                ? profile.getFullName() 
                : ta.getEmail();

        content.add(createWelcomeSection(displayName));
        content.add(Box.createVerticalStrut(25));
        content.add(createStatsCards());
        content.add(Box.createVerticalStrut(25));
        content.add(createApplicationsSection());
        content.add(Box.createVerticalStrut(25));
        content.add(createExploreSection());
        content.add(Box.createVerticalStrut(20));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(248, 250, 252));
        
        return scrollPane;
    }

    private JPanel createWelcomeSection(String displayName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + displayName + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(30, 35, 45));

        JLabel roleLabel = new JLabel("Teaching Assistant");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(107, 114, 128));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel);
        textPanel.add(roleLabel);

        JButton notifyBtn = new JButton("🔔");
        notifyBtn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        notifyBtn.setBackground(new Color(248, 250, 252));
        notifyBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        notifyBtn.setFocusPainted(false);
        notifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifyBtn.addActionListener(e -> 
                NotificationPopup.showUnreadNotifications(this, ta, notificationService));

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(notifyBtn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatsCards() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 15);

        List<TAApplication> applications = applicationService.listByTaUserId(ta.getUserId());
        long acceptedCount = applications.stream().filter(a -> "HIRED".equals(a.getStatus())).count();
        long pendingCount = applications.stream().filter(a -> "SUBMITTED".equals(a.getStatus())).count();
        long rejectedCount = applications.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();

        gbc.gridx = 0;
        panel.add(createStatCard("✅", "Accepted", String.valueOf(acceptedCount), ACCEPTED_COLOR), gbc);
        
        gbc.gridx = 1;
        panel.add(createStatCard("⏳", "Pending Review", String.valueOf(pendingCount), PENDING_COLOR), gbc);
        
        gbc.gridx = 2;
        panel.add(createStatCard("❌", "Rejected", String.valueOf(rejectedCount), REJECTED_COLOR), gbc);
        
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(createStatCard("📋", "In Progress", "2", PRIMARY_BLUE), gbc);

        return panel;
    }

    private JPanel createStatCard(String emoji, String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
        emojiLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(107, 114, 128));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(emojiLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(titleLabel);

        return card;
    }

    private JPanel createApplicationsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Recent Applications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        JButton viewAllBtn = new JButton("View All →");
        viewAllBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        viewAllBtn.setForeground(PRIMARY_BLUE);
        viewAllBtn.setBackground(Color.WHITE);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllBtn, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(16));
        panel.add(createApplicationsTable());  // 修复：直接添加 JScrollPane

        return panel;
    }

    private JScrollPane createApplicationsTable() {
        String[] columns = {"Course", "Status", "Applied", "Feedback"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<TAApplication> applications = applicationService.listByTaUserId(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TAApplication app : applications) {
            String status = app.getStatus();
            String statusText;
            
            switch (status) {
                case "HIRED":
                    statusText = "accepted";
                    break;
                case "SUBMITTED":
                    statusText = "pending";
                    break;
                case "REJECTED":
                    statusText = "rejected";
                    break;
                default:
                    statusText = status.toLowerCase();
            }
            
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String feedback = getFeedbackMessage(status);
            
            model.addRow(new Object[]{getModuleName(app.getJobId()), statusText, appliedAt, feedback});
        }

        if (applications.isEmpty()) {
            model.addRow(new Object[]{"—", "—", "—", "No applications yet"});
        }

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        table.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(720, 250));

        return scrollPane;
    }

    private String getModuleName(Long jobId) {
        List<MOJob> jobs = jobService.listPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return "Course #" + jobId;
    }

    private String getFeedbackMessage(String status) {
        switch (status) {
            case "HIRED":
                return "Excellent candidate with strong programming background.";
            case "SUBMITTED":
                return "—";
            case "REJECTED":
                return "Position filled by another candidate.";
            default:
                return "";
        }
    }

    private JPanel createExploreSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(59, 130, 246));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Explore New Opportunities");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("Browse open TA positions and apply today");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLabel.setForeground(new Color(191, 219, 254));
        descLabel.setAlignmentX(LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(descLabel);

        JButton browseBtn = new JButton("Browse Courses →");
        browseBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        browseBtn.setForeground(PRIMARY_BLUE);
        browseBtn.setBackground(Color.WHITE);
        browseBtn.setBorderPainted(false);
        browseBtn.setFocusPainted(false);
        browseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        browseBtn.addActionListener(e -> applyForFirstPublishedJob());

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(browseBtn, BorderLayout.EAST);

        return panel;
    }

    private void refreshApplicationsTable() {
        new TAMainFrame(ta).setVisible(true);
        dispose();
    }

    private void applyForFirstPublishedJob() {
        try {
            List<MOJob> jobs = jobService.listPublishedJobs();
            if (jobs.isEmpty()) {
                showWarning("No published jobs are available.");
                return;
            }
            TAApplication application = applicationService.submitApplication(
                    ta.getUserId(),
                    jobs.get(0).getJobId(),
                    "I am very interested in this TA position and believe my skills align well with the requirements."
            );
            showInfo("Application submitted successfully!\n\n" + applicationService.buildApplicationSummary(application));
            refreshApplicationsTable();
        } catch (Exception ex) {
            showWarning(ex.getMessage());
        }
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = (String) value;
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            if ("accepted".equalsIgnoreCase(status)) {
                setBackground(new Color(220, 252, 231));
                setForeground(new Color(22, 101, 52));
            } else if ("pending".equalsIgnoreCase(status)) {
                setBackground(new Color(254, 249, 195));
                setForeground(new Color(161, 98, 7));
            } else if ("rejected".equalsIgnoreCase(status)) {
                setBackground(new Color(254, 226, 226));
                setForeground(new Color(153, 27, 27));
            } else {
                setBackground(Color.WHITE);
                setForeground(new Color(107, 114, 128));
            }
            
            return this;
        }
    }
}