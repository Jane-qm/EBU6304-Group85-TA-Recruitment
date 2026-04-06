package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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

import common.entity.MOJob;
import common.entity.TA;
import common.entity.User;
import common.ui.BaseFrame;
import ta.controller.TAApplicationController;
import ta.controller.TAAuthController;

/**
 * TA 课程目录界面
 * 
 * @author Can Chen
 * @version 1.0
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
                return column == 3;
            }
        };

        List<MOJob> availableJobs = applicationController.getAvailableJobs(ta.getUserId());
        int remainingSlots = applicationController.getRemainingApplicationSlots(ta.getUserId());

        for (MOJob job : availableJobs) {
            String action = remainingSlots > 0 ? "Apply" : "Full";
            model.addRow(new Object[]{
                    job.getModuleCode() + " - " + job.getTitle(),
                    job.getWeeklyHours(),
                    job.getDescription() != null ? 
                        (job.getDescription().length() > 60 ? job.getDescription().substring(0, 60) + "..." : job.getDescription()) 
                        : "",
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
        
        table.getColumnModel().getColumn(3).setCellRenderer(new ActionButtonRenderer());
        
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
     * 操作按钮渲染器
     */
    private class ActionButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String action = (String) value;
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            if ("Apply".equals(action)) {
                setForeground(PRIMARY_BLUE);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                setBackground(Color.WHITE);
            } else if ("Full".equals(action)) {
                setForeground(new Color(156, 163, 175));
                setFont(new Font("SansSerif", Font.PLAIN, 12));
                setBackground(Color.WHITE);
            } else {
                setForeground(new Color(107, 114, 128));
            }
            
            return this;
        }
    }
}