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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import auth.LoginFrame;
import common.entity.MOJob;
import common.entity.TA;
import common.entity.User;
import common.service.NotificationService;
import common.ui.BaseFrame;
import common.ui.NotificationPopup;
import ta.controller.TAApplicationController;
import ta.controller.TAOfferController;
import ta.controller.TAProfileController;
import ta.controller.TAAuthController;
import ta.entity.TAApplication;
import ta.ui.components.StatusCellRenderer;

/**
 * TA 主界面 - Dashboard
 * 
 * @author Can Chen
 * @version 3.1 - 使用公共 StatusCellRenderer
 */
public class TAMainFrame extends BaseFrame {
    
    private final TA ta;
    
    private final TAProfileController profileController;
    private final TAApplicationController applicationController;
    private final TAOfferController offerController;
    private final TAAuthController authController;
    private final NotificationService notificationService;

    private static final Color SIDEBAR_BG = new Color(30, 35, 45);
    private static final Color ACCEPTED_COLOR = new Color(34, 197, 94);
    private static final Color PENDING_COLOR = new Color(234, 179, 8);
    private static final Color REJECTED_COLOR = new Color(239, 68, 68);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);

    public TAMainFrame(User user) {
        super("TA Recruitment System - Dashboard", 1200, 750);
        this.ta = (TA) user;
        
        this.profileController = new TAProfileController();
        this.applicationController = new TAApplicationController();
        this.offerController = new TAOfferController();
        this.authController = new TAAuthController();
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

        JLabel logoLabel = new JLabel("TA Recruit");
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
        
        String displayName = authController.getDisplayName(ta);
        JLabel userLabel = new JLabel(displayName);
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLabel.setForeground(new Color(156, 163, 175));
        userLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(userLabel);
        
        JLabel roleLabel = new JLabel(authController.getRoleDisplayText());
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(107, 114, 128));
        roleLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(roleLabel);
        
        sidebar.add(Box.createVerticalStrut(16));
        
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
            switch (text) {
                case "My Profile":
                    new TAProfileFrame(ta).setVisible(true);
                    dispose();
                    break;
                case "My Applications":
                    new TAApplicationsFrame(ta).setVisible(true);
                    dispose();
                    break;
                case "Course Catalog":
                    new TACourseCatalogFrame(ta).setVisible(true);
                    dispose();
                    break;
                case "Workload Tracking":
                    new TAWorkloadFrame(ta).setVisible(true);
                    dispose();
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

        content.add(createWelcomeSection());
        content.add(Box.createVerticalStrut(25));
        content.add(createStatsCards());
        content.add(Box.createVerticalStrut(25));
        content.add(createApplicationsSection());
        content.add(Box.createVerticalStrut(20));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }

    private JPanel createWelcomeSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String displayName = authController.getDisplayName(ta);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + displayName);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(30, 35, 45));

        JLabel roleLabel = new JLabel(authController.getRoleDisplayText());
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(107, 114, 128));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel);
        textPanel.add(roleLabel);

        int unreadCount = applicationController.getUnreadNotificationCount(ta.getUserId());
        JButton notifyBtn = new JButton("🔔" + (unreadCount > 0 ? " " + unreadCount : ""));
        notifyBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
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

        TAApplicationController.ApplicationStats stats = 
                applicationController.getApplicationStats(ta.getUserId());

        gbc.gridx = 0;
        panel.add(createStatCard("✅", "Accepted", String.valueOf(stats.accepted), ACCEPTED_COLOR), gbc);
        
        gbc.gridx = 1;
        panel.add(createStatCard("⏳", "Pending", String.valueOf(stats.pending), PENDING_COLOR), gbc);
        
        gbc.gridx = 2;
        panel.add(createStatCard("❌", "Rejected", String.valueOf(stats.rejected), REJECTED_COLOR), gbc);
        
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(createStatCard("📋", "Total", String.valueOf(stats.getTotal()), PRIMARY_BLUE), gbc);

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
        viewAllBtn.addActionListener(e -> {
            new TAApplicationsFrame(ta).setVisible(true);
            dispose();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllBtn, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        int remainingSlots = applicationController.getRemainingApplicationSlots(ta.getUserId());
        int maxApps = applicationController.getMaxActiveApplications();
        JLabel limitLabel = new JLabel("You can only have " + maxApps + " active applications at once. " +
                (remainingSlots > 0 ? remainingSlots + " slots remaining." : "No slots remaining."));
        limitLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        limitLabel.setForeground(remainingSlots > 0 ? new Color(107, 114, 128) : new Color(239, 68, 68));
        limitLabel.setBorder(new EmptyBorder(8, 0, 16, 0));
        panel.add(limitLabel, BorderLayout.CENTER);
        
        panel.add(createApplicationsTable(), BorderLayout.SOUTH);

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

        List<TAApplication> applications = applicationController.getMyApplications(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int displayCount = Math.min(applications.size(), 5);
        for (int i = 0; i < displayCount; i++) {
            TAApplication app = applications.get(i);
            String status = applicationController.getDisplayStatus(app);
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String feedback = applicationController.getFeedbackMessage(app);
            String courseName = getCourseName(app.getJobId());
            
            model.addRow(new Object[]{courseName, status, appliedAt, feedback});
        }

        if (applications.isEmpty()) {
            model.addRow(new Object[]{"—", "—", "—", "No applications yet"});
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // 使用公共的 StatusCellRenderer
        table.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(720, 220));

        return scrollPane;
    }

    private String getCourseName(Long jobId) {
        List<MOJob> jobs = applicationController.getPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return "Course #" + jobId;
    }
}