package ui.ta;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import modules.job.Job;
import modules.user.MO;
import modules.user.TA;
import modules.user.User;
import modules.user.UserService;
import modules.application.ApplicationController;
import modules.auth.TAAuthController;
import modules.profile.TAProfileController;
import modules.cv.CVInfo;
import modules.profile.TAProfile;
import modules.cv.CVService;
import modules.profile.TAProfileService;
import ui.common.JobDetailDialog;
import ui.common.TableScrollUtil;
import ui.ta.components.ActionButtonRenderer;

public class TACourseCatalogPanel extends JPanel {
    
    private final TA ta;
    private final ApplicationController applicationController;
    private final TAAuthController authController;
    private final TAProfileController profileController;
    private final CVService cvService;
    private final UserService userService = UserService.getInstance();
    
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    
    private JTable coursesTable;
    private DefaultTableModel tableModel;
    private List<Job> availableJobs;
    
    public TACourseCatalogPanel(TA ta) {
        this.ta = ta;
        this.applicationController = new ApplicationController();
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
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
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
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }
    
    private String parseDeadlineFromDescription(Job job) {
        return JobDetailDialog.formatApplicationDeadline(job);
    }
    
    private JScrollPane createCoursesTable() {
        String[] columns = {"Course Name", "Module Code", "MO", "Hours/Week", "Deadline", "Detail", "Apply"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshTable();
        
        coursesTable = new JTable(tableModel);
        coursesTable.setRowHeight(50);
        coursesTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        coursesTable.setShowGrid(false);
        coursesTable.setIntercellSpacing(new Dimension(0, 0));
        
        coursesTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        coursesTable.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        
        coursesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = coursesTable.rowAtPoint(e.getPoint());
                int col = coursesTable.columnAtPoint(e.getPoint());
                
                if (row < availableJobs.size()) {
                    Job job = availableJobs.get(row);
                    
                    if (col == 5) {
                        JobDetailDialog.show(TACourseCatalogPanel.this, job);
                    } else if (col == 6) {
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
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        TableScrollUtil.ColumnSpec[] courseCols = {
                TableScrollUtil.ColumnSpec.flex(140, 260),
                TableScrollUtil.ColumnSpec.fixed(88),
                TableScrollUtil.ColumnSpec.flex(88, 130),
                TableScrollUtil.ColumnSpec.fixed(78),
                TableScrollUtil.ColumnSpec.flex(88, 115),
                TableScrollUtil.ColumnSpec.fixed(68),
                TableScrollUtil.ColumnSpec.fixed(68),
        };

        JScrollPane scrollPane = TableScrollUtil.wrapTable(coursesTable);
        TableScrollUtil.installResponsiveColumns(coursesTable, scrollPane, courseCols);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(900, 400));

        return scrollPane;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        availableJobs = applicationController.getAvailableJobs(ta.getUserId());

        for (Job job : availableJobs) {
            String deadlineText = parseDeadlineFromDescription(job);
            User mo = job.getMoUserId() != null ? userService.findById(job.getMoUserId()) : null;

            tableModel.addRow(new Object[]{
                    job.getTitle(),
                    job.getModuleCode(),
                    moDisplayName(mo),
                    job.getWeeklyHours(),
                    deadlineText,
                    "Detail",
                    "Apply"
            });
        }

        if (availableJobs.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "—", "—", "—", "—", "—", "—"});
        }
    }
    
    private static String moDisplayName(User mo) {
        if (mo == null) {
            return "—";
        }
        if (mo instanceof MO) {
            String n = ((MO) mo).getName();
            if (n != null && !n.isBlank()) {
                return n.trim();
            }
        }
        String email = mo.getEmail();
        if (email == null) {
            return "—";
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private void showApplicationDialog(Job job) {
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
        courseLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        courseLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(courseLabel);
        panel.add(Box.createVerticalStrut(15));
        
        JLabel cvLabel = new JLabel("Select CV:");
        cvLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
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
        uploadNewBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(uploadNewBtn);
        panel.add(Box.createVerticalStrut(15));
        
        JLabel statementLabel = new JLabel("Application Statement:");
        statementLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
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