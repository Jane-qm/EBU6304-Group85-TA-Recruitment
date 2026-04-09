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
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import common.entity.MOJob;
import common.entity.TA;
import ta.controller.TAApplicationController;
import ta.controller.TAAuthController;
import ta.controller.TAProfileController;
import ta.entity.CVInfo;
import ta.service.CVService;
import ta.ui.components.ActionButtonRenderer;

/**
 * TA 课程目录面板
 * 显示可申请的课程列表，支持查看详情和提交申请
 * 
 * @author Can Chen
 * @version 1.0
 */
public class TACourseCatalogPanel extends JPanel {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    private final TAAuthController authController;
    private final TAProfileController profileController;
    private final CVService cvService;
    
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    
    private JTable coursesTable;
    private DefaultTableModel tableModel;
    private List<MOJob> availableJobs;
    
    public TACourseCatalogPanel(TA ta) {
        this.ta = ta;
        this.applicationController = new TAApplicationController();
        this.authController = new TAAuthController();
        this.profileController = new TAProfileController();
        this.cvService = new CVService();
        
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
        
        JLabel titleLabel = new JLabel("Course Catalog");
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
        // 添加 Detail 和 Apply 两列
        String[] columns = {"Course", "Hours/Week", "Description", "", ""};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshTable();
        
        coursesTable = new JTable(tableModel);
        coursesTable.setRowHeight(50);
        coursesTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        coursesTable.setShowGrid(false);
        coursesTable.setIntercellSpacing(new Dimension(0, 0));
        
        // 设置渲染器
        coursesTable.getColumnModel().getColumn(3).setCellRenderer(new ActionButtonRenderer());
        coursesTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
        
        // 添加鼠标点击事件处理
        coursesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = coursesTable.rowAtPoint(e.getPoint());
                int col = coursesTable.columnAtPoint(e.getPoint());
                
                if (row < availableJobs.size()) {
                    MOJob job = availableJobs.get(row);
                    
                    if (col == 3) {  // Detail 列
                        showCourseDetailDialog(job);
                    } else if (col == 4) {  // Apply 列
                        String action = (String) coursesTable.getValueAt(row, 4);
                        if ("Apply".equals(action)) {
                            showApplicationDialog(job);
                        }
                    }
                }
            }
        });
        
        JTableHeader header = coursesTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // 设置列宽
        coursesTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        coursesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        coursesTable.getColumnModel().getColumn(2).setPreferredWidth(400);
        coursesTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        coursesTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(1000, 400));

        return scrollPane;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        availableJobs = applicationController.getAvailableJobs(ta.getUserId());
        int remainingSlots = applicationController.getRemainingApplicationSlots(ta.getUserId());

        for (MOJob job : availableJobs) {
            String applyAction = remainingSlots > 0 ? "Apply" : "Full";
            String description = job.getDescription() != null ? 
                    (job.getDescription().length() > 60 ? job.getDescription().substring(0, 60) + "..." : job.getDescription()) : "";
            
            tableModel.addRow(new Object[]{
                    job.getModuleCode() + " - " + job.getTitle(),
                    job.getWeeklyHours(),
                    description,
                    "Detail",
                    applyAction
            });
        }

        if (availableJobs.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "—", "No available courses at this time", "—", "—"});
        }
    }
    
    /**
     * 显示课程详情对话框
     */
    private void showCourseDetailDialog(MOJob job) {
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
        JSeparator separator = new JSeparator();
        separator.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(separator);
        panel.add(Box.createVerticalStrut(15));
        
        // 详细信息网格
        JPanel infoPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        // 课程代码
        infoPanel.add(createInfoLabel("Module Code:"));
        infoPanel.add(createValueLabel(job.getModuleCode()));
        
        // 每周工时
        infoPanel.add(createInfoLabel("Weekly Hours:"));
        infoPanel.add(createValueLabel(job.getWeeklyHours() + " hours/week"));
        
        // 状态
        infoPanel.add(createInfoLabel("Status:"));
        String statusText = "OPEN".equals(job.getStatus()) ? "Open for Applications" : 
                           ("PUBLISHED".equals(job.getStatus()) ? "Published" : job.getStatus());
        JLabel statusLabel = createValueLabel(statusText);
        if ("OPEN".equals(job.getStatus()) || "PUBLISHED".equals(job.getStatus())) {
            statusLabel.setForeground(new Color(34, 197, 94));
        }
        infoPanel.add(statusLabel);
        
        // 发布时间
        if (job.getCreatedAt() != null) {
            infoPanel.add(createInfoLabel("Posted Date:"));
            infoPanel.add(createValueLabel(job.getCreatedAt().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        }
        
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // 分隔线
        JSeparator separator2 = new JSeparator();
        separator2.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(separator2);
        panel.add(Box.createVerticalStrut(15));
        
        // 所需技能
        JLabel skillsTitle = new JLabel("Required Skills");
        skillsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        skillsTitle.setForeground(new Color(55, 65, 81));
        skillsTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(skillsTitle);
        panel.add(Box.createVerticalStrut(8));
        
        JPanel skillsPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
        skillsPanel.setBackground(Color.WHITE);
        skillsPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        List<String> skills = job.getRequiredSkills();
        if (skills != null && !skills.isEmpty()) {
            for (String skill : skills) {
                JLabel skillTag = new JLabel("  " + skill + "  ");
                skillTag.setFont(new Font("SansSerif", Font.PLAIN, 12));
                skillTag.setBackground(new Color(243, 246, 251));
                skillTag.setForeground(PRIMARY_BLUE);
                skillTag.setOpaque(true);
                skillTag.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
                skillsPanel.add(skillTag);
            }
        } else {
            skillsPanel.add(new JLabel("No specific skills required"));
        }
        panel.add(skillsPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // 分隔线
        JSeparator separator3 = new JSeparator();
        separator3.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(separator3);
        panel.add(Box.createVerticalStrut(15));
        
        // 职位描述
        JLabel descTitle = new JLabel("Job Description");
        descTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        descTitle.setForeground(new Color(55, 65, 81));
        descTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(descTitle);
        panel.add(Box.createVerticalStrut(8));
        
        JTextArea descArea = new JTextArea(8, 50);
        descArea.setText(job.getDescription() != null ? job.getDescription() : "No description provided.");
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(Color.WHITE);
        descArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(500, 150));
        panel.add(descScroll);
        
        // 对话框
        JOptionPane.showConfirmDialog(null, new JScrollPane(panel), 
            "Course Details", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
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
            // 切换到Profile面板
            TAMainFrame mainFrame = (TAMainFrame) getTopLevelAncestor();
            if (mainFrame instanceof TAMainFrame) {
                mainFrame.switchToProfile();
            }
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
                ta.getUserId(), job.getJobId(), statement, selectedCvId[0], null);
            
            if (success) {
                refresh();
            }
        }
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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