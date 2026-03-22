package ta;

import auth.LoginFrame;
import common.entity.TA;
import common.service.UserService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TA 个人资料页面（Requirement C）
 *
 * <p>功能覆盖：</p>
 * <ul>
 *   <li>RC-1：填写姓名、专业、年级等基本信息</li>
 *   <li>RC-2：添加 / 删除技能标签</li>
 *   <li>RC-3：填写每周可投入工时</li>
 *   <li>RC-4：保存资料并支持重复编辑</li>
 * </ul>
 *
 * @author Zhixuan Guo
 * @version 1.0
 */
public class TAProfileFrame extends JFrame {

    /** 年级下拉选项 */
    private static final String[] GRADE_OPTIONS = {
        "-- 请选择年级 --",
        "Year 1 (大一)",
        "Year 2 (大二)",
        "Year 3 (大三)",
        "Year 4 (大四)",
        "Postgraduate (研究生)"
    };

    private final TA ta;
    private final UserService userService;

    // RC-1: Basic info fields
    private JTextField nameField;
    private JTextField majorField;
    private JComboBox<String> gradeCombo;
    private JTextField studentIdField;

    // RC-3: Working hours
    private JSpinner hoursSpinner;

    // RC-2: Skill tags
    private DefaultListModel<String> skillTagsModel;
    private JList<String> skillTagsList;
    private JTextField newTagField;

    /**
     * 构造函数 - 接收登录后的 TA 对象，自动加载已有资料。
     *
     * @param ta 当前登录的 TA 用户
     */
    public TAProfileFrame(TA ta) {
        this.ta          = ta;
        this.userService = new UserService();
        initUI();
        loadProfile();  // RC-4: 加载已保存的资料
    }

    // ==================== UI Construction ====================

    private void initUI() {
        setTitle("TA 个人资料 — " + ta.getEmail());
        setSize(560, 660);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleLogout();
            }
        });

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        root.add(buildHeaderPanel());
        root.add(Box.createVerticalStrut(16));
        root.add(buildBasicInfoPanel());       // RC-1
        root.add(Box.createVerticalStrut(12));
        root.add(buildWorkingHoursPanel());    // RC-3
        root.add(Box.createVerticalStrut(12));
        root.add(buildSkillTagsPanel());       // RC-2
        root.add(Box.createVerticalStrut(20));
        root.add(buildButtonPanel());          // RC-4

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll);
    }

    /** 标题 + 邮箱展示区 */
    private JPanel buildHeaderPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("个人资料");
        title.setFont(new Font("微软雅黑", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailHint = new JLabel("邮箱：" + ta.getEmail());
        emailHint.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        emailHint.setForeground(Color.GRAY);
        emailHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(emailHint);
        return p;
    }

    /**
     * RC-1：基本信息面板（姓名、专业、年级、学号）
     */
    private JPanel buildBasicInfoPanel() {
        JPanel panel = createSection("基本信息 (RC-1)");
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = defaultGbc();
        nameField      = new JTextField(22);
        majorField     = new JTextField(22);
        gradeCombo     = new JComboBox<>(GRADE_OPTIONS);
        gradeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        studentIdField = new JTextField(22);

        addRow(panel, gbc, 0, "姓名 *",  nameField);
        addRow(panel, gbc, 1, "专业 *",  majorField);
        addRow(panel, gbc, 2, "年级 *",  gradeCombo);
        addRow(panel, gbc, 3, "学号",    studentIdField);

        return panel;
    }

    /**
     * RC-3：每周工时面板
     */
    private JPanel buildWorkingHoursPanel() {
        JPanel panel = createSection("工时设置 (RC-3)");
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        JLabel lbl = new JLabel("每周可投入工时：");
        lbl.setFont(new Font("微软雅黑", Font.PLAIN, 13));

        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 40, 1));
        hoursSpinner.setPreferredSize(new Dimension(72, 28));
        ((JSpinner.DefaultEditor) hoursSpinner.getEditor())
                .getTextField().setFont(new Font("微软雅黑", Font.PLAIN, 13));

        JLabel unit = new JLabel("小时 / 周  （0 – 40）");
        unit.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        unit.setForeground(Color.GRAY);

        panel.add(lbl);
        panel.add(hoursSpinner);
        panel.add(unit);
        return panel;
    }

    /**
     * RC-2：技能标签面板（支持添加 / 删除）
     */
    private JPanel buildSkillTagsPanel() {
        JPanel panel = createSection("技能标签 (RC-2)");
        panel.setLayout(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
                panel.getBorder(),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));

        skillTagsModel = new DefaultListModel<>();
        skillTagsList  = new JList<>(skillTagsModel);
        skillTagsList.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        skillTagsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane listScroll = new JScrollPane(skillTagsList);
        listScroll.setPreferredSize(new Dimension(0, 100));
        panel.add(listScroll, BorderLayout.CENTER);

        // Add / Remove controls
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        newTagField = new JTextField(16);
        newTagField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        newTagField.setToolTipText("输入技能标签，按回车或点击「添加」");

        JButton addBtn    = styledBtn("添加",   new Color(70, 130, 180));
        JButton removeBtn = styledBtn("删除选中", new Color(180, 70, 70));

        addBtn.addActionListener(e    -> handleAddTag());
        removeBtn.addActionListener(e -> handleRemoveTag());
        newTagField.addActionListener(e -> handleAddTag());

        inputRow.add(new JLabel("标签："));
        inputRow.add(newTagField);
        inputRow.add(addBtn);
        inputRow.add(removeBtn);
        panel.add(inputRow, BorderLayout.SOUTH);
        return panel;
    }

    /** RC-4：保存 / 退出按钮区 */
    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn   = styledBtn("保存资料", new Color(60, 140, 60));
        JButton logoutBtn = styledBtn("退出登录", new Color(120, 120, 120));

        saveBtn.setPreferredSize(new Dimension(130, 38));
        logoutBtn.setPreferredSize(new Dimension(130, 38));

        saveBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        logoutBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        saveBtn.addActionListener(e   -> handleSave());
        logoutBtn.addActionListener(e -> handleLogout());

        p.add(saveBtn);
        p.add(logoutBtn);
        return p;
    }

    // ==================== Business Logic ====================

    /**
     * RC-4：加载已保存的 TA 资料到表单。
     */
    private void loadProfile() {
        if (ta.getName()      != null) nameField.setText(ta.getName());
        if (ta.getMajor()     != null) majorField.setText(ta.getMajor());
        if (ta.getStudentId() != null) studentIdField.setText(ta.getStudentId());

        hoursSpinner.setValue(ta.getWeeklyHours());

        if (ta.getGrade() != null) {
            for (int i = 0; i < gradeCombo.getItemCount(); i++) {
                if (gradeCombo.getItemAt(i).startsWith(ta.getGrade())) {
                    gradeCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        skillTagsModel.clear();
        ta.getSkillTags().forEach(skillTagsModel::addElement);
    }

    /**
     * RC-2：添加一个技能标签（自动去重、忽略空值）。
     */
    private void handleAddTag() {
        String tag = newTagField.getText().trim();
        if (tag.isEmpty()) return;
        if (!skillTagsModel.contains(tag)) {
            skillTagsModel.addElement(tag);
        }
        newTagField.setText("");
        newTagField.requestFocus();
    }

    /**
     * RC-2：删除列表中当前选中的技能标签。
     */
    private void handleRemoveTag() {
        int idx = skillTagsList.getSelectedIndex();
        if (idx >= 0) {
            skillTagsModel.remove(idx);
        } else {
            JOptionPane.showMessageDialog(this,
                    "请先在列表中点选一个要删除的标签。",
                    "未选中标签", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * RC-4：校验表单并将资料持久化。
     */
    private void handleSave() {
        String name        = nameField.getText().trim();
        String major       = majorField.getText().trim();
        String studentId   = studentIdField.getText().trim();
        int    weeklyHours = (int) hoursSpinner.getValue();
        String gradeItem   = (String) gradeCombo.getSelectedItem();

        // Validation
        if (name.isEmpty()) {
            showWarn("姓名不能为空，请填写后保存。");
            nameField.requestFocus();
            return;
        }
        if (major.isEmpty()) {
            showWarn("专业不能为空，请填写后保存。");
            majorField.requestFocus();
            return;
        }
        if (gradeItem == null || gradeItem.startsWith("--")) {
            showWarn("请选择您的年级。");
            gradeCombo.requestFocus();
            return;
        }

        // Extract the "Year X" / "Postgraduate" key before the parenthesis
        String grade = gradeItem.split("\\s*\\(")[0].trim();

        // Collect current skill tags
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < skillTagsModel.size(); i++) {
            tags.add(skillTagsModel.getElementAt(i));
        }

        // Push to TA entity
        ta.setName(name);
        ta.setMajor(major);
        ta.setGrade(grade);
        ta.setStudentId(studentId.isEmpty() ? null : studentId);
        ta.setWeeklyHours(weeklyHours);
        ta.setSkillTags(tags);

        // Persist via service layer (RC-4)
        try {
            userService.updateProfile(ta);
            JOptionPane.showMessageDialog(this,
                    """
                    资料已保存！

                    姓名：%s
                    专业：%s
                    年级：%s
                    每周工时：%d 小时
                    技能标签：%s
                    """.formatted(ta.getName(), ta.getMajor(), ta.getGrade(),
                                  ta.getWeeklyHours(), ta.getSkillTags()),
                    "保存成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "保存失败：" + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 退出登录并返回登录窗口。 */
    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "确定退出登录？", "确认退出", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    // ==================== UI Helpers ====================

    private JPanel createSection(String title) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 13)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return p;
    }

    private GridBagConstraints defaultGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(6, 8, 6, 8);
        return g;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc,
                        int row, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(lbl, gbc);

        if (field instanceof JTextField tf) {
            tf.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        }
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
        gbc.weightx = 0;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        return btn;
    }

    private void showWarn(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
    }
}
