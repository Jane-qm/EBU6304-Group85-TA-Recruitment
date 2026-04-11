package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
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
 * 显示所有申请记录，支持取消申请和查看详情
 * 
 * @author Can Chen
 * @version 2.0 - 添加查看详情功能，移除Feedback列
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
        // 列：课程、状态、申请日期、申请陈述、详情、操作（移除Feedback列）
        String[] columns = {"Course", "Status", "Applied Date", "Statement", "Detail", "Action"};
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
        
        // 设置渲染器
        applicationsTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        applicationsTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());  // Detail
        applicationsTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());  // Action
        
        // 添加鼠标点击事件
        applicationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = applicationsTable.rowAtPoint(e.getPoint());
                int col = applicationsTable.columnAtPoint(e.getPoint());
                
                if (row < applications.size()) {
                    TAApplication app = applications.get(row);
                    
                    if (col == 4) {  // Detail 列
                        showApplicationDetailDialog(app);
                    } else if (col == 5) {  // Action 列
                        String action = (String) applicationsTable.getValueAt(row, 5);
                        if ("Cancel".equals(action)) {
                            boolean success = applicationController.cancelApplicationWithFeedback(
                                app.getApplicationId(), null);
                            if (success) {
                                refresh();
                            }
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
        
        // 设置列宽
        applicationsTable.getColumnModel().getColumn(0).setPreferredWidth(280);
        applicationsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        applicationsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        applicationsTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        applicationsTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        applicationsTable.getColumnModel().getColumn(5).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(1000, 400));

        return scrollPane;
    }
    
    /**
     * 显示申请详情对话框
     */
    private void showApplicationDetailDialog(TAApplication app) {
        MOJob job = applicationController.getJobById(app.getJobId());
        if (job == null) {
            JOptionPane.showMessageDialog(this, "Course information not found.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        // 课程标题
        JLabel titleLabel = new JLabel(job.getModuleCode() + " - " + job.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        // 分隔线
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        // 申请状态区域
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(248, 250, 252));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(12, 15, 12, 15)
        ));
        statusPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel statusTitleLabel = new JLabel("Application Status: ");
        statusTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        String statusText = applicationController.getDisplayStatus(app);
        JLabel statusValueLabel = new JLabel(statusText);
        statusValueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // 根据状态设置颜色
        if (ApplicationStatus.isAccepted(app.getStatus()) || ApplicationStatus.isHired(app.getStatus())) {
            statusValueLabel.setForeground(new Color(34, 197, 94));
        } else if (ApplicationStatus.isAwaitingReview(app.getStatus())) {
            statusValueLabel.setForeground(new Color(234, 179, 8));
        } else if (ApplicationStatus.isRejected(app.getStatus())) {
            statusValueLabel.setForeground(new Color(239, 68, 68));
        } else if (ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
            statusValueLabel.setForeground(new Color(59, 130, 246));
        } else {
            statusValueLabel.setForeground(new Color(107, 114, 128));
        }
        
        JPanel statusInnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusInnerPanel.setBackground(new Color(248, 250, 252));
        statusInnerPanel.add(statusTitleLabel);
        statusInnerPanel.add(statusValueLabel);
        statusPanel.add(statusInnerPanel, BorderLayout.WEST);
        
        panel.add(statusPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // 课程详细信息
        JPanel infoPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        infoPanel.add(createInfoLabel("Module Code:"));
        infoPanel.add(createValueLabel(job.getModuleCode()));
        
        infoPanel.add(createInfoLabel("Weekly Hours:"));
        infoPanel.add(createValueLabel(job.getWeeklyHours() + " hours/week"));
        
        infoPanel.add(createInfoLabel("Applied Date:"));
        String appliedDate = app.getAppliedAt() != null ? 
            app.getAppliedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
        infoPanel.add(createValueLabel(appliedDate));
        
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(15));
        
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        // 申请陈述
        JLabel statementTitle = new JLabel("Application Statement");
        statementTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        statementTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(statementTitle);
        panel.add(Box.createVerticalStrut(8));
        
        JTextArea statementArea = new JTextArea(5, 50);
        statementArea.setText(app.getStatement() != null ? app.getStatement() : "No statement provided.");
        statementArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);
        statementArea.setEditable(false);
        statementArea.setBackground(Color.WHITE);
        statementArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        
        JScrollPane statementScroll = new JScrollPane(statementArea);
        statementScroll.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(statementScroll);
        
        // 显示对话框
        JOptionPane.showConfirmDialog(null, new JScrollPane(panel), 
            "Application Details", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * 创建信息标签（左侧）
     */
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }
    
    /**
     * 创建值标签（右侧）
     */
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(30, 35, 45));
        return label;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        applications = applicationController.getMyApplications(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TAApplication app : applications) {
            String status = applicationController.getDisplayStatus(app);
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String courseName = applicationController.getCourseName(app.getJobId());
            String statement = app.getStatement() != null ? 
                    (app.getStatement().length() > 40 ? app.getStatement().substring(0, 40) + "..." : app.getStatement()) 
                    : "";
            
            // 判断是否显示取消按钮
            boolean canCancel = ApplicationStatus.isCancellable(app.getStatus());
            String action = canCancel ? "Cancel" : "—";
            
            tableModel.addRow(new Object[]{courseName, status, appliedAt, statement, "Detail", action});
        }

        if (applications.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "—", "—", "No applications yet", "—", "—"});
        }
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