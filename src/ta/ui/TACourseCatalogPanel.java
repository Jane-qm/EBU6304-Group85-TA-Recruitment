package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
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
import javax.swing.SwingUtilities;
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
import ta.entity.TAProfile;
import ta.service.CVService;
import ta.service.TAProfileService;
import ta.ui.components.ActionButtonRenderer;

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
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
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
        
        panel.add(createCoursesTable());
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }
    
    /**
     * 从 job 的 description 中解析 deadline 字符串
     * description 格式示例: "Skills: ...\nHeadcount: ...\nDeadline: 2026-05-01\nDetails: ..."
     */
    private String parseDeadlineFromDescription(MOJob job) {
        String desc = job.getDescription();
        if (desc == null) return "Not set";
        // 查找 "Deadline: " 行
        String[] lines = desc.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("Deadline:")) {
                String deadlinePart = line.substring(line.indexOf("Deadline:") + 9).trim();
                return deadlinePart.isEmpty() ? "Not set" : deadlinePart;
            }
        }
        // 如果 description 中没有，则使用 applicationDeadline 字段（如果有）
        if (job.getApplicationDeadline() != null) {
            return job.getApplicationDeadline().toLocalDate().toString();
        }
        return "Not set";
    }
    
    private JScrollPane createCoursesTable() {
        String[] columns = {"Course Name", "Module Code", "Hours/Week", "Deadline", "Detail", "Apply"};
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
        
        coursesTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
        coursesTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        
        coursesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = coursesTable.rowAtPoint(e.getPoint());
                int col = coursesTable.columnAtPoint(e.getPoint());
                
                if (row < availableJobs.size()) {
                    MOJob job = availableJobs.get(row);
                    
                    if (col == 4) {
                        showCourseDetailDialog(job);
                    } else if (col == 5) {
                        // 检查职位是否可申请
                        if (!job.isApplicable()) {
                            if (job.isExpired()) {
                                showWarning("The application deadline for this position has passed.");
                            } else {
                                showWarning("This position is not open for applications.");
                            }
                            return;
                        }
                        
                        refresh();
                        if (!applicationController.canSubmitMoreApplications(ta.getUserId())) {
                            showWarning("You can only have " + applicationController.getMaxActiveApplications() 
                                + " active applications at once. Please wait for decisions before applying for more.");
                            return;
                        }
                        showApplicationDialog(job);
                    }
                }
            }
        });
        
        JTableHeader header = coursesTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        coursesTable.getColumnModel().getColumn(0).setPreferredWidth(280);
        coursesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        coursesTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        coursesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        coursesTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        coursesTable.getColumnModel().getColumn(5).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(900, 400));

        return scrollPane;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        availableJobs = applicationController.getAvailableJobs(ta.getUserId());

        for (MOJob job : availableJobs) {
            String deadlineText = parseDeadlineFromDescription(job);
            
            tableModel.addRow(new Object[]{
                    job.getTitle(),
                    job.getModuleCode(),
                    job.getWeeklyHours(),
                    deadlineText,
                    "Detail",
                    "Apply"
            });
        }

        if (availableJobs.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "—", "—", "—", "—", "—"});
        }
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(30, 35, 45));
        return label;
    }
    
    private void showCourseDetailDialog(MOJob job) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(job.getModuleCode() + " - " + job.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        JPanel infoPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        infoPanel.add(createInfoLabel("Module Code:"));
        infoPanel.add(createValueLabel(job.getModuleCode()));
        
        infoPanel.add(createInfoLabel("Weekly Hours:"));
        infoPanel.add(createValueLabel(job.getWeeklyHours() + " hours/week"));
        
        infoPanel.add(createInfoLabel("Application Deadline:"));
        String deadlineText = parseDeadlineFromDescription(job);
        infoPanel.add(createValueLabel(deadlineText));
        
        infoPanel.add(createInfoLabel("Status:"));
        String statusText = job.isApplicable() ? "Open for Applications" : 
                           (job.isExpired() ? "Closed - Deadline Passed" : job.getStatus());
        JLabel statusLabel = createValueLabel(statusText);
        if (job.isApplicable()) {
            statusLabel.setForeground(new Color(34, 197, 94));
        } else {
            statusLabel.setForeground(new Color(239, 68, 68));
        }
        infoPanel.add(statusLabel);
        
        if (job.getCreatedAt() != null) {
            infoPanel.add(createInfoLabel("Posted Date:"));
            infoPanel.add(createValueLabel(job.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        }
        
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(15));
        
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        JLabel skillsTitle = new JLabel("Required Skills");
        skillsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        skillsTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(skillsTitle);
        panel.add(Box.createVerticalStrut(8));
        
        JPanel skillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
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
        
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        JLabel descTitle = new JLabel("Job Description");
        descTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
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
        
        JOptionPane.showConfirmDialog(null, new JScrollPane(panel), 
            "Course Details", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }
    
    private void showApplicationDialog(MOJob job) {
        TAProfileService directService = new TAProfileService();
        TAProfile freshProfile = directService.getProfileByTaId(ta.getUserId());
        
        boolean isComplete = freshProfile != null && freshProfile.isProfileCompleted();
        
        if (!isComplete) {
            JOptionPane.showMessageDialog(this,
                "Please complete your personal profile first!\n\n" +
                "Go to My Profile to fill in all required information.",
                "Profile Incomplete", JOptionPane.WARNING_MESSAGE);
            TAMainFrame mainFrame = (TAMainFrame) getTopLevelAncestor();
            if (mainFrame instanceof TAMainFrame) {
                mainFrame.switchToProfile();
            }
            return;
        }
        cvService.refreshCVs(ta.getUserId());
        
        List<CVInfo> cvList = cvService.getAllCVs(ta.getUserId());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel courseLabel = new JLabel("Applying for: " + job.getModuleCode() + " - " + job.getTitle());
        courseLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        courseLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(courseLabel);
        panel.add(Box.createVerticalStrut(15));
        
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
        
        JButton uploadNewBtn = new JButton("📄 Upload New CV");
        uploadNewBtn.setAlignmentX(LEFT_ALIGNMENT);
        uploadNewBtn.setMaximumSize(new Dimension(200, 30));
        uploadNewBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(uploadNewBtn);
        panel.add(Box.createVerticalStrut(15));
        
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
        
        final Long[] selectedCvId = {null};
        
        if (!cvList.isEmpty()) {
            String selected = (String) cvCombo.getSelectedItem();
            if (selected != null && cvMap.containsKey(selected)) {
                selectedCvId[0] = cvMap.get(selected).getCvId();
            }
        }
        
        cvCombo.addActionListener(e -> {
            String selected = (String) cvCombo.getSelectedItem();
            if (selected != null && cvMap.containsKey(selected)) {
                selectedCvId[0] = cvMap.get(selected).getCvId();
            }
        });
        
        uploadNewBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("CV Files", "pdf", "doc", "docx"));
            java.awt.Window owner = SwingUtilities.getWindowAncestor(this);
            int result = fileChooser.showOpenDialog(owner != null ? owner : this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String fileName = file.getName();
                String defaultCvName = fileName;
                int dot = defaultCvName.lastIndexOf('.');
                if (dot > 0) {
                    defaultCvName = defaultCvName.substring(0, dot);
                }
                java.awt.Component dlgParent = owner != null ? owner : this;
                String cvName = JOptionPane.showInputDialog(dlgParent, "Enter a name for this CV:",
                        defaultCvName);

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
    
    public void refresh() {
        refreshTable();
        revalidate();
        repaint();
    }
}