package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import common.entity.TA;
import common.entity.User;
import common.ui.BaseFrame;
import ta.controller.TAApplicationController;
import ta.controller.TAAuthController;
import ta.entity.TAApplication;

/**
 * TA 我的申请界面
 * 
 * @author Can Chen
 * @version 1.0
 */
public class TAApplicationsFrame extends BaseFrame {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    private final TAAuthController authController;
    
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);

    public TAApplicationsFrame(User user) {
        super("TA Recruitment System - My Applications", 1000, 700);
        this.ta = (TA) user;
        this.applicationController = new TAApplicationController();
        this.authController = new TAAuthController();
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));
        
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createContent(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("My Applications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        backBtn.setForeground(TAWorkloadFrame.PRIMARY_BLUE);
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new TAMainFrame(ta).setVisible(true);
            dispose();
        });
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(backBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    private JScrollPane createContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(0, 30, 30, 30));
        
        // 限制提示
        int remainingSlots = applicationController.getRemainingApplicationSlots(ta.getUserId());
        int maxApps = applicationController.getMaxActiveApplications();
        JLabel limitLabel = new JLabel("You can only have " + maxApps + " active applications at once. " +
                (remainingSlots > 0 ? remainingSlots + " slots remaining." : "No slots remaining."));
        limitLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        limitLabel.setForeground(remainingSlots > 0 ? new Color(107, 114, 128) : new Color(239, 68, 68));
        limitLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(limitLabel);
        panel.add(Box.createVerticalStrut(16));
        
        panel.add(createApplicationsTable());
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }
    
    private JScrollPane createApplicationsTable() {
        String[] columns = {"Course", "Status", "Applied Date", "Statement", "Feedback"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<TAApplication> applications = applicationController.getMyApplications(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TAApplication app : applications) {
            String status = applicationController.getDisplayStatus(app);
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String feedback = applicationController.getFeedbackMessage(app);
            String courseName = getCourseName(app.getJobId());
            String statement = app.getStatement() != null ? 
                    (app.getStatement().length() > 50 ? app.getStatement().substring(0, 50) + "..." : app.getStatement()) 
                    : "";
            
            model.addRow(new Object[]{courseName, status, appliedAt, statement, feedback});
        }

        if (applications.isEmpty()) {
            model.addRow(new Object[]{"—", "—", "—", "No applications yet", "—"});
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
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(800, 400));

        return scrollPane;
    }
    
    private String getCourseName(Long jobId) {
        List<common.entity.MOJob> jobs = applicationController.getPublishedJobs();
        for (common.entity.MOJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return "Course #" + jobId;
    }
    
    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 30, 20, 30));
        
        JButton browseBtn = new JButton("+ Browse More Courses");
        browseBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        browseBtn.setForeground(Color.WHITE);
        browseBtn.setBackground(TAWorkloadFrame.PRIMARY_BLUE);
        browseBtn.setBorderPainted(false);
        browseBtn.setFocusPainted(false);
        browseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        browseBtn.addActionListener(e -> {
            new TACourseCatalogFrame(ta).setVisible(true);
            dispose();
        });
        
        panel.add(browseBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 状态单元格渲染器
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
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