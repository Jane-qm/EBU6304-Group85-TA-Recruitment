package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import common.domain.ApplicationStatus;
import common.entity.MOJob;
import common.entity.TA;
import ta.controller.TAApplicationController;
import ta.entity.TAApplication;
import ta.ui.components.ActionButtonRenderer;
import ta.ui.components.StatusCellRenderer;

/**
 * TA 我的申请面板
 * 显示所有申请记录，支持取消申请
 * 
 * @author Can Chen
 * @version 1.0
 */
public class TAApplicationsPanel extends JPanel {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    private List<TAApplication> applications;
    
    public TAApplicationsPanel(TA ta) {
        this.ta = ta;
        this.applicationController = new TAApplicationController();
        
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        
        initUI();
    }
    
    private void initUI() {
        // 标题区域
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // 内容区域
        JScrollPane contentScroll = createContentPanel();
        add(contentScroll, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("My Applications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JScrollPane createContentPanel() {
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
        String[] columns = {"Course", "Status", "Applied Date", "Statement", "Feedback", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshTable();
        
        applicationsTable = new JTable(tableModel);
        applicationsTable.setRowHeight(50);
        applicationsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        applicationsTable.setShowGrid(false);
        applicationsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // 使用公共的渲染器
        applicationsTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        applicationsTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        
        // 添加鼠标点击事件处理取消按钮
        applicationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = applicationsTable.rowAtPoint(e.getPoint());
                int col = applicationsTable.columnAtPoint(e.getPoint());
                if (col == 5 && row < applications.size()) {  // Action 列
                    String action = (String) applicationsTable.getValueAt(row, col);
                    if ("Cancel".equals(action)) {
                        TAApplication app = applications.get(row);
                        boolean success = applicationController.cancelApplicationWithFeedback(
                            app.getApplicationId(), null);
                        if (success) {
                            refresh();
                        }
                    }
                }
            }
        });
        
        JTableHeader header = applicationsTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(800, 400));

        return scrollPane;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        applications = applicationController.getMyApplications(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TAApplication app : applications) {
            String status = applicationController.getDisplayStatus(app);
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String feedback = applicationController.getFeedbackMessage(app);
            String courseName = getCourseName(app.getJobId());
            String statement = app.getStatement() != null ? 
                    (app.getStatement().length() > 50 ? app.getStatement().substring(0, 50) + "..." : app.getStatement()) 
                    : "";
            
            // 判断是否显示取消按钮
            boolean canCancel = ApplicationStatus.isCancellable(app.getStatus());
            String action = canCancel ? "Cancel" : "—";
            
            tableModel.addRow(new Object[]{courseName, status, appliedAt, statement, feedback, action});
        }

        if (applications.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "—", "—", "No applications yet", "—", "—"});
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
    
    /**
     * 刷新面板数据
     */
    public void refresh() {
        refreshTable();
        revalidate();
        repaint();
    }
}