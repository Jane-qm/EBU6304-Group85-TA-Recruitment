package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import common.entity.TA;
import common.entity.User;
import common.ui.BaseFrame;
import ta.controller.TAProfileController;
import ta.entity.CVInfo;
import ta.entity.TAProfile;
import ta.service.CVService;

/**
 * TA 个人信息界面
 * 包含个人信息填写和 CV 上传功能
 * 
 * @author Can Chen
 * @version 3.0 - 使用 Controller 架构
 */
public class TAProfileFrame extends BaseFrame {
    
    private final TA ta;
    
    // 使用 Controller 替代直接使用 Service
    private final TAProfileController profileController;
    private final CVService cvService;
    
    private TAProfile profile;
    
    // 个人信息组件
    private JTextField surnameField;
    private JTextField forenameField;
    private JTextField chineseNameField;
    private JTextField studentIdField;
    private JTextField phoneField;
    private JComboBox<String> genderCombo;
    private JTextField schoolField;
    private JTextField supervisorField;
    private JComboBox<String> studentTypeCombo;
    private JComboBox<String> yearCombo;
    private JTextArea experienceArea;
    private JPanel skillTagsPanel;
    private JSpinner hoursSpinner;
    private List<String> skillTags;
    
    // CV 相关组件
    private JLabel cvFileNameLabel;
    private JLabel cvUploadTimeLabel;
    private JButton viewCVBtn;
    private JButton deleteCVBtn;
    private CVInfo currentCV;
    
    // 颜色常量
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color LABEL_FOREGROUND = new Color(55, 65, 81);
    
    public TAProfileFrame(User user) {
        super("TA Recruitment System - My Profile", 900, 750);
        this.ta = (TA) user;
        
        // 初始化 Controllers 和 Service
        this.profileController = new TAProfileController();
        this.cvService = new CVService();
        this.skillTags = new ArrayList<>();
        
        loadProfile();
        initUI();
    }
    
    /**
     * 加载 TA 个人信息（使用 Controller）
     */
    private void loadProfile() {
        // 使用 Controller 获取个人资料
        profile = profileController.getProfileForUI(ta);
        
        if (profile.getSkillTags() != null) {
            skillTags = new ArrayList<>(profile.getSkillTags());
        }
        
        // 加载 CV 信息
        currentCV = cvService.getDefaultCV(ta.getUserId());
    }
    
    @Override
    protected void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 251));
        
        // 顶部标题栏
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // 中间内容区域（可滚动）
        JScrollPane scrollPane = new JScrollPane(createContentPanel());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部按钮栏
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * 创建顶部标题栏
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_BLUE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Manage your personal information and CV");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 210, 255));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        // 返回按钮
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backBtn.setForeground(PRIMARY_BLUE);
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new TAMainFrame(ta).setVisible(true);
            dispose();
        });
        
        panel.add(textPanel, BorderLayout.WEST);
        panel.add(backBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建内容面板
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        // 个人信息部分
        JPanel personalInfoPanel = createPersonalInfoPanel();
        panel.add(personalInfoPanel);
        panel.add(Box.createVerticalStrut(30));
        
        // 技能标签部分
        JPanel skillsPanel = createSkillsPanel();
        panel.add(skillsPanel);
        panel.add(Box.createVerticalStrut(30));
        
        // CV 上传部分
        JPanel cvPanel = createCVPanel();
        panel.add(cvPanel);
        
        return panel;
    }
    
    /**
     * 创建个人信息面板
     */
    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                "Personal Information",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 16),
                PRIMARY_BLUE
        ));
        
        // 第一行：姓氏 + 名字
        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setOpaque(false);
        row1.setBorder(new EmptyBorder(15, 15, 10, 15));
        
        surnameField = createTextField(profile.getSurname());
        forenameField = createTextField(profile.getForename());
        row1.add(createLabeledField("Surname *", surnameField));
        row1.add(createLabeledField("Forename *", forenameField));
        
        // 第二行：中文名 + 学号
        JPanel row2 = new JPanel(new GridLayout(1, 2, 20, 0));
        row2.setOpaque(false);
        row2.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        chineseNameField = createTextField(profile.getChineseName());
        studentIdField = createTextField(profile.getStudentId());
        row2.add(createLabeledField("Chinese Name", chineseNameField));
        row2.add(createLabeledField("Student ID *", studentIdField));
        
        // 第三行：电话 + 性别
        JPanel row3 = new JPanel(new GridLayout(1, 2, 20, 0));
        row3.setOpaque(false);
        row3.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        phoneField = createTextField(profile.getPhone());
        genderCombo = new JComboBox<>(new String[]{"Male", "Female"});
        if (profile.getGender() != null) {
            genderCombo.setSelectedItem(profile.getGender().getEnglishName());
        }
        row3.add(createLabeledField("Phone Number *", phoneField));
        row3.add(createLabeledField("Gender *", genderCombo));
        
        // 第四行：学院 + 导师
        JPanel row4 = new JPanel(new GridLayout(1, 2, 20, 0));
        row4.setOpaque(false);
        row4.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        schoolField = createTextField(profile.getSchool());
        supervisorField = createTextField(profile.getSupervisor());
        row4.add(createLabeledField("School *", schoolField));
        row4.add(createLabeledField("Supervisor *", supervisorField));
        
        // 第五行：学生类型 + 年级
        JPanel row5 = new JPanel(new GridLayout(1, 2, 20, 0));
        row5.setOpaque(false);
        row5.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        studentTypeCombo = new JComboBox<>(new String[]{"Masters/MSc", "PhD"});
        if (profile.getStudentType() != null) {
            studentTypeCombo.setSelectedItem(profile.getStudentType().getEnglishName());
        }
        yearCombo = new JComboBox<>(new String[]{"Year 1", "Year 2", "Year 3", "Year 4", "Year 5"});
        if (profile.getCurrentYear() != null) {
            yearCombo.setSelectedItem(profile.getCurrentYear().getEnglishName());
        }
        row5.add(createLabeledField("Student Type *", studentTypeCombo));
        row5.add(createLabeledField("Current Year *", yearCombo));
        
        // 第六行：可用工时
        JPanel row6 = new JPanel(new GridLayout(1, 2, 20, 0));
        row6.setOpaque(false);
        row6.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        hoursSpinner = new JSpinner(new SpinnerNumberModel(profile.getAvailableWorkingHours(), 0, 40, 1));
        row6.add(createLabeledField("Available Hours/Week", hoursSpinner));
        row6.add(new JPanel()); // 占位
        
        // 第七行：过往经历
        JPanel row7 = new JPanel(new BorderLayout());
        row7.setOpaque(false);
        row7.setBorder(new EmptyBorder(10, 15, 15, 15));
        
        JLabel expLabel = new JLabel("Previous Experience");
        expLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        expLabel.setForeground(LABEL_FOREGROUND);
        
        experienceArea = new JTextArea(4, 30);
        experienceArea.setText(profile.getPreviousExperience());
        experienceArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        experienceArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        experienceArea.setLineWrap(true);
        experienceArea.setWrapStyleWord(true);
        
        JScrollPane expScroll = new JScrollPane(experienceArea);
        expScroll.setBorder(null);
        
        row7.add(expLabel, BorderLayout.NORTH);
        row7.add(expScroll, BorderLayout.CENTER);
        
        panel.add(row1);
        panel.add(row2);
        panel.add(row3);
        panel.add(row4);
        panel.add(row5);
        panel.add(row6);
        panel.add(row7);
        
        return panel;
    }
    
    /**
     * 创建技能标签面板
     */
    private JPanel createSkillsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                "Skills",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 16),
                PRIMARY_BLUE
        ));
        
        // 技能标签显示区域
        skillTagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        skillTagsPanel.setBackground(Color.WHITE);
        skillTagsPanel.setBorder(new EmptyBorder(15, 15, 10, 15));
        updateSkillTagsDisplay();
        
        // 添加技能区域
        JPanel addSkillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addSkillPanel.setBackground(Color.WHITE);
        addSkillPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        JTextField newSkillField = new JTextField(15);
        newSkillField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        JButton addSkillBtn = new JButton("+ Add Skill");
        addSkillBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        addSkillBtn.setBackground(PRIMARY_BLUE);
        addSkillBtn.setForeground(Color.WHITE);
        addSkillBtn.setFocusPainted(false);
        addSkillBtn.addActionListener(e -> {
            String skill = newSkillField.getText().trim();
            if (!skill.isEmpty() && !skillTags.contains(skill)) {
                skillTags.add(skill);
                updateSkillTagsDisplay();
                newSkillField.setText("");
            }
        });
        
        addSkillPanel.add(newSkillField);
        addSkillPanel.add(Box.createHorizontalStrut(10));
        addSkillPanel.add(addSkillBtn);
        
        panel.add(skillTagsPanel);
        panel.add(addSkillPanel);
        
        return panel;
    }
    
    /**
     * 更新技能标签显示
     */
    private void updateSkillTagsDisplay() {
        skillTagsPanel.removeAll();
        for (String tag : skillTags) {
            JPanel tagPanel = createSkillTag(tag);
            skillTagsPanel.add(tagPanel);
        }
        skillTagsPanel.revalidate();
        skillTagsPanel.repaint();
    }
    
    /**
     * 创建单个技能标签
     */
    private JPanel createSkillTag(String tag) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(new Color(243, 246, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        
        JLabel label = new JLabel(tag);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JButton removeBtn = new JButton("✖");
        removeBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        removeBtn.setForeground(new Color(107, 114, 128));
        removeBtn.setBorderPainted(false);
        removeBtn.setContentAreaFilled(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeBtn.addActionListener(e -> {
            skillTags.remove(tag);
            updateSkillTagsDisplay();
        });
        
        panel.add(label, BorderLayout.WEST);
        panel.add(removeBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建 CV 上传面板
     */
    private JPanel createCVPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                "CV / Resume",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 16),
                PRIMARY_BLUE
        ));
        
        // 上传区域
        JPanel uploadArea = new JPanel();
        uploadArea.setLayout(new BoxLayout(uploadArea, BoxLayout.Y_AXIS));
        uploadArea.setBackground(new Color(250, 251, 252));
        uploadArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(40, 40, 40, 40)
        ));
        
        JLabel iconLabel = new JLabel("📄");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel dragLabel = new JLabel("Drag & drop your CV here");
        dragLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        dragLabel.setForeground(new Color(55, 65, 81));
        dragLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel orLabel = new JLabel("or");
        orLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        orLabel.setForeground(new Color(107, 114, 128));
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton browseBtn = new JButton("Click to browse");
        browseBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        browseBtn.setForeground(Color.WHITE);
        browseBtn.setBackground(PRIMARY_BLUE);
        browseBtn.setBorderPainted(false);
        browseBtn.setFocusPainted(false);
        browseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        browseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseBtn.setPreferredSize(new Dimension(150, 38));
        browseBtn.addActionListener(e -> uploadCV());
        
        JLabel formatLabel = new JLabel("PDF / Word - File Size Limit: 5MB");
        formatLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        formatLabel.setForeground(new Color(107, 114, 128));
        formatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formatLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        uploadArea.add(iconLabel);
        uploadArea.add(Box.createVerticalStrut(15));
        uploadArea.add(dragLabel);
        uploadArea.add(Box.createVerticalStrut(8));
        uploadArea.add(orLabel);
        uploadArea.add(Box.createVerticalStrut(12));
        uploadArea.add(browseBtn);
        uploadArea.add(formatLabel);
        
        // 当前 CV 信息显示
        JPanel cvInfoPanel = new JPanel(new BorderLayout());
        cvInfoPanel.setBackground(Color.WHITE);
        cvInfoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        if (currentCV != null) {
            cvFileNameLabel = new JLabel("Current: " + currentCV.getCvName() + " (" + currentCV.getFileSizeDisplay() + ")");
            cvFileNameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            
            cvUploadTimeLabel = new JLabel("Uploaded: " + currentCV.getUploadedAtDisplay());
            cvUploadTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            cvUploadTimeLabel.setForeground(new Color(107, 114, 128));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            viewCVBtn = new JButton("View");
            viewCVBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            viewCVBtn.addActionListener(e -> viewCV());
            
            deleteCVBtn = new JButton("Delete");
            deleteCVBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            deleteCVBtn.setForeground(new Color(220, 38, 38));
            deleteCVBtn.addActionListener(e -> deleteCV());
            
            buttonPanel.add(viewCVBtn);
            buttonPanel.add(deleteCVBtn);
            
            JPanel infoTextPanel = new JPanel(new GridLayout(2, 1));
            infoTextPanel.setOpaque(false);
            infoTextPanel.add(cvFileNameLabel);
            infoTextPanel.add(cvUploadTimeLabel);
            
            cvInfoPanel.add(infoTextPanel, BorderLayout.WEST);
            cvInfoPanel.add(buttonPanel, BorderLayout.EAST);
        } else {
            JLabel noCVLabel = new JLabel("No CV uploaded yet");
            noCVLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noCVLabel.setForeground(new Color(107, 114, 128));
            cvInfoPanel.add(noCVLabel, BorderLayout.WEST);
        }
        
        panel.add(uploadArea);
        panel.add(cvInfoPanel);
        
        return panel;
    }
    
    /**
     * 创建带标签的输入框
     */
    private JPanel createLabeledField(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(LABEL_FOREGROUND);
        
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建文本框
     */
    private JTextField createTextField(String value) {
        JTextField field = new JTextField(value != null ? value : "");
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
    
    /**
     * 创建底部按钮栏
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        
        JButton saveBtn = new JButton("Save Profile");
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(PRIMARY_BLUE);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(140, 40));
        saveBtn.addActionListener(e -> saveProfile());
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cancelBtn.setForeground(new Color(107, 114, 128));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setPreferredSize(new Dimension(120, 40));
        cancelBtn.addActionListener(e -> {
            new TAMainFrame(ta).setVisible(true);
            dispose();
        });
        
        panel.add(saveBtn);
        panel.add(cancelBtn);
        
        return panel;
    }
    
    /**
     * 上传 CV
     */
    private void uploadCV() {
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
                
                // 从 profile 获取姓名
                String taName = (profile != null && profile.getFullName() != null) 
                    ? profile.getFullName() 
                    : "";
                
                CVInfo newCV = cvService.uploadCV(
                        ta.getUserId(),
                        ta.getEmail(),
                        taName,
                        cvName.trim(),
                        "",
                        fileName,
                        fileData
                );
                
                currentCV = newCV;
                showInfo("CV uploaded successfully: " + cvName);
                
                // 刷新界面
                dispose();
                new TAProfileFrame(ta).setVisible(true);
                
            } catch (Exception e) {
                showError("Upload failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * 查看 CV
     */
    private void viewCV() {
        if (currentCV == null) {
            showWarning("No CV to view");
            return;
        }
        
        // TODO: 实现 CV 查看功能（打开文件或显示内容）
        showInfo("View CV: " + currentCV.getCvName() + " - Coming soon");
    }
    
    /**
     * 删除 CV
     */
    private void deleteCV() {
        if (currentCV == null) {
            return;
        }
        
        boolean confirm = showConfirm("Are you sure you want to delete this CV?", "Confirm Delete");
        if (confirm) {
            boolean deleted = cvService.deleteCV(ta.getUserId(), currentCV.getCvId());
            if (deleted) {
                currentCV = null;
                showInfo("CV deleted successfully");
                // 刷新界面
                dispose();
                new TAProfileFrame(ta).setVisible(true);
            } else {
                showError("Failed to delete CV");
            }
        }
    }
    
    /**
     * 保存个人信息（使用 Controller）
     */
    private void saveProfile() {
        try {
            // 收集表单数据
            profile.setSurname(surnameField.getText().trim());
            profile.setForename(forenameField.getText().trim());
            profile.setChineseName(chineseNameField.getText().trim());
            profile.setStudentId(studentIdField.getText().trim());
            profile.setPhone(phoneField.getText().trim());
            profile.setGender(TAProfile.Gender.fromEnglishName((String) genderCombo.getSelectedItem()));
            profile.setSchool(schoolField.getText().trim());
            profile.setSupervisor(supervisorField.getText().trim());
            profile.setStudentType(TAProfile.StudentType.fromEnglishName((String) studentTypeCombo.getSelectedItem()));
            profile.setCurrentYear(TAProfile.Year.fromEnglishName((String) yearCombo.getSelectedItem()));
            profile.setPreviousExperience(experienceArea.getText().trim());
            profile.setSkillTags(skillTags);
            profile.setAvailableWorkingHours((Integer) hoursSpinner.getValue());
            
            // 使用 Controller 保存（带用户反馈）
            boolean success = profileController.saveProfileWithFeedback(profile, this);
            
            if (success) {
                // 返回主界面
                new TAMainFrame(ta).setVisible(true);
                dispose();
            }
            
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
        }
    }
    
    @Override
    protected void onWindowMaximized() {
        // 窗口最大化时的回调
    }
    
    @Override
    protected void onWindowRestored() {
        // 窗口还原时的回调
    }
}