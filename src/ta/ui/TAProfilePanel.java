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
import ta.controller.TAProfileController;
import ta.entity.CVInfo;
import ta.entity.TAProfile;
import ta.service.CVService;

/**
 * TA 个人资料面板
 * 统一风格 - 白色卡片设计，与其他面板一致
 * 
 * @author Can Chen
 * @version 2.0 - 统一风格，添加校区选择
 *
 * @version 2.1
 * @contributor Jiaze Wang
 * @update
 * - Added a Major input field to match the TAProfile entity and admin data table
 * - Saved Major together with the rest of the TA profile fields
 */
public class TAProfilePanel extends JPanel {
    
    private final TA ta;
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
    private JTextField majorField;
    private JComboBox<String> studentTypeCombo;
    private JComboBox<String> yearCombo;
    private JComboBox<String> campusCombo;  // 新增校区选择
    private JTextArea experienceArea;
    private JPanel skillTagsPanel;
    private JSpinner hoursSpinner;
    private List<String> skillTags;
    
    // CV 相关组件
    private JPanel cvInfoPanel;
    private CVInfo currentCV;
    
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color LABEL_FOREGROUND = new Color(55, 65, 81);
    
    public TAProfilePanel(TA ta) {
        this.ta = ta;
        this.profileController = new TAProfileController();
        this.cvService = new CVService();
        this.skillTags = new ArrayList<>();
        
        loadProfile();
        
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        
        initUI();
    }
    
    private void loadProfile() {
        profile = profileController.getProfileForUI(ta);
        
        if (profile.getSkillTags() != null) {
            skillTags = new ArrayList<>(profile.getSkillTags());
        }
        
        currentCV = cvService.getDefaultCV(ta.getUserId());
    }
    
    private void initUI() {
        // 标题区域
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // 内容区域（可滚动）
        JScrollPane scrollPane = new JScrollPane(createContentPanel());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(0, 30, 30, 30));
        
        // 个人信息卡片
        panel.add(createPersonalInfoCard());
        panel.add(Box.createVerticalStrut(25));
        
        // 技能卡片
        panel.add(createSkillsCard());
        panel.add(Box.createVerticalStrut(25));
        
        // CV 卡片
        panel.add(createCvCard());
        
        // 保存按钮区域
        panel.add(Box.createVerticalStrut(25));
        panel.add(createButtonPanel());
        
        return panel;
    }
    
    private JPanel createPersonalInfoCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));
        
        // 卡片标题
        JLabel cardTitle = new JLabel("Personal Information");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardTitle.setForeground(new Color(30, 35, 45));
        card.add(cardTitle, BorderLayout.NORTH);
        
        // 表单内容
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 20, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
        
        surnameField = createTextField(profile.getSurname());
        forenameField = createTextField(profile.getForename());
        chineseNameField = createTextField(profile.getChineseName());
        studentIdField = createTextField(profile.getStudentId());
        phoneField = createTextField(profile.getPhone());
        schoolField = createTextField(profile.getSchool());
        supervisorField = createTextField(profile.getSupervisor());
        majorField = createTextField(profile.getMajor());
        
        genderCombo = new JComboBox<>(new String[]{"Male", "Female"});
        genderCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        if (profile.getGender() != null) {
            genderCombo.setSelectedItem(profile.getGender().getEnglishName());
        }
        
        studentTypeCombo = new JComboBox<>(new String[]{"Masters/MSc", "PhD"});
        studentTypeCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        if (profile.getStudentType() != null) {
            studentTypeCombo.setSelectedItem(profile.getStudentType().getEnglishName());
        }
        
        yearCombo = new JComboBox<>(new String[]{"Year 1", "Year 2", "Year 3", "Year 4", "Year 5"});
        yearCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        if (profile.getCurrentYear() != null) {
            yearCombo.setSelectedItem(profile.getCurrentYear().getEnglishName());
        }
        
        // 校区选择
        campusCombo = new JComboBox<>(new String[]{"XituCheng", "ShaHe"});
        campusCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        if (profile.getCampus() != null) {
            campusCombo.setSelectedItem(profile.getCampus().getEnglishName());
        }
        
        hoursSpinner = new JSpinner(new SpinnerNumberModel(profile.getAvailableWorkingHours(), 0, 40, 1));
        hoursSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        formPanel.add(createLabeledField("Surname *", surnameField));
        formPanel.add(createLabeledField("Forename *", forenameField));
        formPanel.add(createLabeledField("Chinese Name", chineseNameField));
        formPanel.add(createLabeledField("Student ID *", studentIdField));
        formPanel.add(createLabeledField("Phone Number *", phoneField));
        formPanel.add(createLabeledField("Gender *", genderCombo));
        formPanel.add(createLabeledField("School *", schoolField));
        formPanel.add(createLabeledField("Supervisor *", supervisorField));
        formPanel.add(createLabeledField("Major", majorField));
        formPanel.add(createLabeledField("Student Type *", studentTypeCombo));
        formPanel.add(createLabeledField("Current Year *", yearCombo));
        formPanel.add(createLabeledField("Campus *", campusCombo));
        formPanel.add(createLabeledField("Available Hours/Week", hoursSpinner));
        
        card.add(formPanel, BorderLayout.CENTER);
        
        // 过往经历区域
        JPanel experiencePanel = new JPanel(new BorderLayout());
        experiencePanel.setBackground(Color.WHITE);
        experiencePanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
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
        
        experiencePanel.add(expLabel, BorderLayout.NORTH);
        experiencePanel.add(expScroll, BorderLayout.CENTER);
        
        card.add(experiencePanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createSkillsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));
        
        // 卡片标题
        JLabel cardTitle = new JLabel("Skills");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardTitle.setForeground(new Color(30, 35, 45));
        card.add(cardTitle, BorderLayout.NORTH);
        
        // 技能标签区域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
        
        skillTagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        skillTagsPanel.setBackground(Color.WHITE);
        updateSkillTagsDisplay();
        
        // 添加技能区域
        JPanel addSkillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addSkillPanel.setBackground(Color.WHITE);
        
        JTextField newSkillField = new JTextField(15);
        newSkillField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        JButton addSkillBtn = new JButton("+ Add Skill");
        addSkillBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        addSkillBtn.setBackground(new Color(243, 246, 251));
        addSkillBtn.setForeground(Color.BLACK);
        addSkillBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        addSkillBtn.setFocusPainted(false);
        addSkillBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        
        contentPanel.add(skillTagsPanel, BorderLayout.NORTH);
        contentPanel.add(addSkillPanel, BorderLayout.SOUTH);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createCvCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));
        
        // 卡片标题
        JLabel cardTitle = new JLabel("CV / Resume");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardTitle.setForeground(new Color(30, 35, 45));
        card.add(cardTitle, BorderLayout.NORTH);
        
        // 上传区域
        JPanel uploadArea = new JPanel();
        uploadArea.setLayout(new BoxLayout(uploadArea, BoxLayout.Y_AXIS));
        uploadArea.setBackground(new Color(250, 251, 252));
        uploadArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(30, 30, 30, 30)
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
        
        card.add(uploadArea, BorderLayout.CENTER);
        
        // CV 信息区域
        cvInfoPanel = new JPanel(new BorderLayout());
        cvInfoPanel.setBackground(Color.WHITE);
        cvInfoPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        updateCVInfoDisplay();
        
        card.add(cvInfoPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private void updateCVInfoDisplay() {
        cvInfoPanel.removeAll();
        
        if (currentCV != null) {
            JLabel cvFileNameLabel = new JLabel("Current: " + currentCV.getCvName() + " (" + currentCV.getFileSizeDisplay() + ")");
            cvFileNameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            
            JLabel cvUploadTimeLabel = new JLabel("Uploaded: " + currentCV.getUploadedAtDisplay());
            cvUploadTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            cvUploadTimeLabel.setForeground(new Color(107, 114, 128));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonPanel.setBackground(Color.WHITE);
            
            JButton viewCVBtn = new JButton("View");
            viewCVBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            viewCVBtn.setBackground(PRIMARY_BLUE);
            viewCVBtn.setForeground(Color.WHITE);
            viewCVBtn.setBorderPainted(false);
            viewCVBtn.setFocusPainted(false);
            viewCVBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            viewCVBtn.addActionListener(e -> viewCV());
            
            JButton deleteCVBtn = new JButton("Delete");
            deleteCVBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            deleteCVBtn.setForeground(new Color(220, 38, 38));
            deleteCVBtn.setBackground(Color.WHITE);
            deleteCVBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38)));
            deleteCVBtn.setFocusPainted(false);
            deleteCVBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
            noCVLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
            noCVLabel.setForeground(new Color(107, 114, 128));
            cvInfoPanel.add(noCVLabel, BorderLayout.WEST);
        }
        
        cvInfoPanel.revalidate();
        cvInfoPanel.repaint();
    }
    
    private void updateSkillTagsDisplay() {
        skillTagsPanel.removeAll();
        for (String tag : skillTags) {
            JPanel tagPanel = createSkillTag(tag);
            skillTagsPanel.add(tagPanel);
        }
        skillTagsPanel.revalidate();
        skillTagsPanel.repaint();
    }
    
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
    
    private JTextField createTextField(String value) {
        JTextField field = new JTextField(value != null ? value : "");
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panel.setBackground(new Color(248, 250, 252));
        
        JButton saveBtn = new JButton("Save Profile");
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.setBackground(Color.WHITE);
        saveBtn.setForeground(Color.BLACK);
        saveBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(160, 42));
        saveBtn.addActionListener(e -> saveProfile());
        
        panel.add(saveBtn);
        
        return panel;
    }
    
    private void uploadCV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF or Word Documents", "pdf", "doc", "docx"));

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        try {
            byte[] fileData = Files.readAllBytes(selectedFile.toPath());
            String cvName = selectedFile.getName();
            String description = "Uploaded from TA profile panel";

            String taName = profile.getFullName();
            if (taName == null || taName.isBlank()) {
                taName = ta.getEmail();
            }

            currentCV = cvService.uploadCV(
                    ta.getUserId(),
                    ta.getEmail(),
                    taName,
                    cvName,
                    description,
                    selectedFile.getName(),
                    fileData
            );
            showInfo("CV uploaded successfully!");
            updateCVInfoDisplay();
        } catch (Exception e) {
            showError("Failed to upload CV: " + e.getMessage());
        }
    }
    
    private void viewCV() {
        if (currentCV == null) {
            showWarning("No CV available.");
            return;
        }

        try {
            byte[] fileData = cvService.downloadCV(ta.getUserId(), currentCV.getCvId());
            if (fileData == null || fileData.length == 0) {
                showError("Failed to read CV file.");
                return;
            }

            String extension = currentCV.getOriginalFileName() != null
                    && currentCV.getOriginalFileName().contains(".")
                    ? currentCV.getOriginalFileName().substring(currentCV.getOriginalFileName().lastIndexOf('.') + 1)
                    : "pdf";

            File tempFile = File.createTempFile("cv_", "." + extension);
            tempFile.deleteOnExit();

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(fileData);
            }

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                showInfo("CV file saved to: " + tempFile.getAbsolutePath());
            }
            
        } catch (Exception e) {
            showError("Failed to open CV: " + e.getMessage());
        }
    }
    
    private void deleteCV() {
        if (currentCV == null) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this CV?", "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = cvService.deleteCV(ta.getUserId(), currentCV.getCvId());
            if (deleted) {
                currentCV = null;
                showInfo("CV deleted successfully");
                updateCVInfoDisplay();
            } else {
                showError("Failed to delete CV");
            }
        }
    }
    
    private void saveProfile() {
        try {
            profile.setSurname(surnameField.getText().trim());
            profile.setForename(forenameField.getText().trim());
            profile.setChineseName(chineseNameField.getText().trim());
            profile.setStudentId(studentIdField.getText().trim());
            profile.setPhone(phoneField.getText().trim());
            profile.setGender(TAProfile.Gender.fromEnglishName((String) genderCombo.getSelectedItem()));
            profile.setSchool(schoolField.getText().trim());
            profile.setSupervisor(supervisorField.getText().trim());
            profile.setMajor(majorField.getText().trim());
            profile.setStudentType(TAProfile.StudentType.fromEnglishName((String) studentTypeCombo.getSelectedItem()));
            profile.setCurrentYear(TAProfile.Year.fromEnglishName((String) yearCombo.getSelectedItem()));
            profile.setCampus(TAProfile.Campus.fromEnglishName((String) campusCombo.getSelectedItem()));
            profile.setPreviousExperience(experienceArea.getText().trim());
            profile.setSkillTags(skillTags);
            profile.setAvailableWorkingHours((Integer) hoursSpinner.getValue());
            
            boolean success = profileController.saveProfileWithFeedback(profile, null);
            
            if (success) {
                showInfo("Profile saved successfully!");
                refresh();
            }
            
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
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
        loadProfile();
        // 重新创建UI以更新数据
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
}