package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import common.entity.MOJob;
import common.entity.TA;
import common.entity.User;
import common.ui.BaseFrame;
import ta.controller.TAApplicationController;
import ta.controller.TAAuthController;
import ta.ui.components.ActionButtonRenderer;

/**
 * TA 课程目录界面
 * 
 * @author Can Chen
 * @version 2.0 - 修复 Apply 按钮点击功能
 */
public class TACourseCatalogFrame extends BaseFrame {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    private final TAAuthController authController;
    
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);

    public TACourseCatalogFrame(User user) {
        super("TA Recruitment System - Course Catalog", 1000, 700);
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
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("Course Catalog");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        backBtn.setForeground(PRIMARY_BLUE);
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
        if (remainingSlots <= 0) {
            JLabel warningLabel = new JLabel("⚠ You have reached the maximum number of active applications (" 
                    + applicationController.getMaxActiveApplications() + "). Please wait for decisions before applying for more.");
            warningLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            warningLabel.setForeground(new Color(239, 68, 68));
            warningLabel.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(warningLabel);
            panel.add(Box.createVerticalStrut(16));
        }
        
        panel.add(createCoursesTable());
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }
    
    private JScrollPane createCoursesTable() {
        String[] columns = {"Course", "Hours/Week", "Description", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<MOJob> availableJobs = applicationController.getAvailableJobs(ta.getUserId());
        int remainingSlots = applicationController.getRemainingApplicationSlots(ta.getUserId());

        for (MOJob job : availableJobs) {
            String action = remainingSlots > 0 ? "Apply" : "Full";
            String description = job.getDescription() != null ? 
                    (job.getDescription().length() > 60 ? job.getDescription().substring(0, 60) + "..." : job.getDescription()) : "";
            
            model.addRow(new Object[]{
                    job.getModuleCode() + " - " + job.getTitle(),
                    job.getWeeklyHours(),
                    description,
                    action
            });
        }

        if (availableJobs.isEmpty()) {
            model.addRow(new Object[]{"—", "—", "No available courses at this time", "—"});
        }

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // 使用公共的 ActionButtonRenderer
        table.getColumnModel().getColumn(3).setCellRenderer(new ActionButtonRenderer());
        
        // 添加鼠标点击事件处理
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                
                // 检查是否点击了 Action 列（第4列，索引为3）
                if (col == 3 && row < availableJobs.size()) {
                    String action = (String) table.getValueAt(row, 3);
                    if ("Apply".equals(action)) {
                        MOJob job = availableJobs.get(row);
                        showApplicationDialog(job);
                    }
                }
            }
        });
        
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
    
    /**
     * 显示申请对话框
     */
    private void showApplicationDialog(MOJob job) {
        JTextArea statementArea = new JTextArea(5, 30);
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Application for: " + job.getModuleCode() + " - " + job.getTitle()), BorderLayout.NORTH);
        panel.add(new JScrollPane(statementArea), BorderLayout.CENTER);
        panel.add(new JLabel("Please explain why you are suitable for this position:"), BorderLayout.SOUTH);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Submit Application", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String statement = statementArea.getText().trim();
            if (statement.isEmpty()) {
                showWarning("Please provide a statement for your application.");
                return;
            }
            
            boolean success = applicationController.submitApplicationWithFeedback(
                ta.getUserId(), job.getJobId(), statement, this);
            
            if (success) {
                // 刷新界面
                dispose();
                new TACourseCatalogFrame(ta).setVisible(true);
            }
        }
    }
}