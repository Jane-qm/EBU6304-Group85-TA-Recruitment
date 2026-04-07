package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import common.entity.MOJob;
import common.entity.TA;
import common.entity.User;
import common.ui.BaseFrame;
import ta.controller.TAApplicationController;
import ta.controller.TAAuthController;
import ta.controller.TAProfileController;
import ta.entity.CVInfo;
import ta.service.CVService;
import ta.ui.components.ActionButtonRenderer;

/**
 * TA 课程目录界面
 * 
 * @author Can Chen
 * @version 3.0 - 添加 CV 选择功能
 */
public class TACourseCatalogFrame extends BaseFrame {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    private final TAAuthController authController;
    private final TAProfileController profileController;
    private final CVService cvService;
    
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);

    public TACourseCatalogFrame(User user) {
        super("TA Recruitment System - Course Catalog", 1000, 700);
        this.ta = (TA) user;
        this.applicationController = new TAApplicationController();
        this.authController = new TAAuthController();
        this.profileController = new TAProfileController();
        this.cvService = new CVService();
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
        
        table.getColumnModel().getColumn(3).setCellRenderer(new ActionButtonRenderer());
        
        // 添加鼠标点击事件处理
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                
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
     * 显示申请对话框（带 CV 选择）
     */
    private void showApplicationDialog(MOJob job) {
        // 1. 检查个人资料是否完整
        if (!profileController.isProfileComplete(ta.getUserId())) {
            JOptionPane.showMessageDialog(this,
                "Please complete your personal profile first!\n\n" +
                "Go to My Profile to fill in all required information.",
                "Profile Incomplete", JOptionPane.WARNING_MESSAGE);
            new TAProfileFrame(ta).setVisible(true);
            dispose();
            return;
        }
        
        // 2. 获取 TA 的所有 CV
        List<CVInfo> cvList = cvService.getAllCVs(ta.getUserId());
        
        // 3. 创建对话框面板
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 课程信息
        JLabel courseLabel = new JLabel("Applying for: " + job.getModuleCode() + " - " + job.getTitle());
        courseLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        courseLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(courseLabel);
        panel.add(Box.createVerticalStrut(15));
        
        // CV 选择区域
        JLabel cvLabel = new JLabel("Select CV:");
        cvLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        cvLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(cvLabel);
        panel.add(Box.createVerticalStrut(5));
        
        JComboBox<String> cvCombo = new JComboBox<>();
        cvCombo.setMaximumSize(new Dimension(400, 30));
        cvCombo.setAlignmentX(LEFT_ALIGNMENT);
        
        Map<String, CVInfo> cvMap = new HashMap<>();
        
        if (cvList.isEmpty()) {
            cvCombo.addItem("-- No CV available, please upload --");
        } else {
            for (CVInfo cv : cvList) {
                String displayName = cv.getCvName() + " (" + cv.getFileSizeDisplay() + ")";
                if (cv.isDefault()) {
                    displayName = displayName + " [Default]";
                }
                cvCombo.addItem(displayName);
                cvMap.put(displayName, cv);
            }
        }
        panel.add(cvCombo);
        panel.add(Box.createVerticalStrut(10));
        
        // 上传新 CV 按钮
        JButton uploadNewBtn = new JButton("📄 Upload New CV");
        uploadNewBtn.setAlignmentX(LEFT_ALIGNMENT);
        uploadNewBtn.setMaximumSize(new Dimension(200, 30));
        uploadNewBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(uploadNewBtn);
        panel.add(Box.createVerticalStrut(15));
        
        // 申请陈述
        JLabel statementLabel = new JLabel("Application Statement:");
        statementLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statementLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(statementLabel);
        panel.add(Box.createVerticalStrut(5));
        
        JTextArea statementArea = new JTextArea(5, 40);
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);
        JScrollPane statementScroll = new JScrollPane(statementArea);
        statementScroll.setMaximumSize(new Dimension(500, 100));
        statementScroll.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(statementScroll);
        
        // 选中的 CV ID
        final Long[] selectedCvId = {null};
        
        // 初始化选中的 CV
        if (!cvList.isEmpty()) {
            String selected = (String) cvCombo.getSelectedItem();
            if (selected != null && cvMap.containsKey(selected)) {
                selectedCvId[0] = cvMap.get(selected).getCvId();
            }
        }
        
        // CV 选择变化事件
        cvCombo.addActionListener(e -> {
            String selected = (String) cvCombo.getSelectedItem();
            if (selected != null && cvMap.containsKey(selected)) {
                selectedCvId[0] = cvMap.get(selected).getCvId();
            }
        });
        
        // 上传新 CV 按钮事件
        uploadNewBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("CV Files", "pdf", "doc", "docx"));
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String fileName = file.getName();
                String cvName = JOptionPane.showInputDialog(this, "Enter a name for this CV:", 
                        "CV Name", JOptionPane.QUESTION_MESSAGE);
                
                if (cvName == null || cvName.trim().isEmpty()) {
                    showWarning("CV name is required");
                    return;
                }
                
                try {
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    String taName = authController.getDisplayName(ta);
                    
                    CVInfo newCV = cvService.uploadCV(
                            ta.getUserId(),
                            ta.getEmail(),
                            taName,
                            cvName.trim(),
                            "",
                            fileName,
                            fileData
                    );
                    
                    String displayName = newCV.getCvName() + " (" + newCV.getFileSizeDisplay() + ")";
                    cvCombo.addItem(displayName);
                    cvMap.put(displayName, newCV);
                    cvCombo.setSelectedItem(displayName);
                    selectedCvId[0] = newCV.getCvId();
                    
                    showInfo("CV uploaded successfully!");
                    
                } catch (Exception ex) {
                    showError("Upload failed: " + ex.getMessage());
                }
            }
        });
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Submit Application", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String statement = statementArea.getText().trim();
            if (statement.isEmpty()) {
                showWarning("Please provide a statement for your application.");
                return;
            }
            
            if (selectedCvId[0] == null && !cvList.isEmpty()) {
                showWarning("Please select a CV.");
                return;
            }
            
            if (selectedCvId[0] == null && cvList.isEmpty()) {
                showWarning("Please upload a CV first.");
                return;
            }
            
            // 提交申请
            boolean success = applicationController.submitApplicationWithFeedback(
                ta.getUserId(), job.getJobId(), statement, selectedCvId[0], this);
            
            if (success) {
                dispose();
                new TACourseCatalogFrame(ta).setVisible(true);
            }
        }
    }
}