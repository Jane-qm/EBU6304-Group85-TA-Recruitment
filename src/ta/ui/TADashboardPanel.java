package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import common.entity.MOJob;
import common.entity.TA;
import ta.controller.TAApplicationController;
import ta.entity.TAApplication;
import ta.ui.components.StatusCellRenderer;

/**
 * TA Dashboard 面板
 * 显示申请统计和最近申请记录
 * 
 * @author Can Chen
 * @version 1.0
 */
public class TADashboardPanel extends JPanel {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    
    private static final Color ACCEPTED_COLOR = new Color(34, 197, 94);
    private static final Color PENDING_COLOR = new Color(234, 179, 8);
    private static final Color REJECTED_COLOR = new Color(239, 68, 68);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    
    public TADashboardPanel(TA ta) {
        this.ta = ta;
        this.applicationController = new TAApplicationController();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(30, 35, 30, 35));
        
        initUI();
    }
    
    private void initUI() {
        add(createStatsCards());
        add(Box.createVerticalStrut(25));
        add(createApplicationsSection());
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
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
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
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshTable();
        
        applicationsTable = new JTable(tableModel);
        applicationsTable.setRowHeight(45);
        applicationsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        applicationsTable.setShowGrid(false);
        applicationsTable.setIntercellSpacing(new Dimension(0, 0));
        
        applicationsTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        
        JTableHeader header = applicationsTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(720, 220));

        return scrollPane;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        List<TAApplication> applications = applicationController.getMyApplications(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int displayCount = Math.min(applications.size(), 5);
        for (int i = 0; i < displayCount; i++) {
            TAApplication app = applications.get(i);
            String status = applicationController.getDisplayStatus(app);
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String feedback = applicationController.getFeedbackMessage(app);
            String courseName = getCourseName(app.getJobId());
            
            tableModel.addRow(new Object[]{courseName, status, appliedAt, feedback});
        }

        if (applications.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "—", "—", "No applications yet"});
        }
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
    
    public void refresh() {
        refreshTable();
        // 刷新统计卡片 - 通过重新创建UI或更新数据
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
}
