package mo.ui;

import common.entity.MOJob;
import common.entity.User;
import common.service.MOJobService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MOJobManagementPanel extends JPanel {
    private final User currentUser;
    private final MOJobService jobService = new MOJobService(); // 引用你的 Service
    private JTable jobTable;
    private DefaultTableModel tableModel;

    public MOJobManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(20, 20)); // 使用原版间距
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initHeader();
        initTable();
        loadJobData(); // 初始加载数据
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Manage Course Jobs");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 操作按钮面板
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton postJobBtn = new JButton("+ Post New Job");
        postJobBtn.setBackground(new Color(59, 130, 246));
        postJobBtn.setForeground(Color.BLUE);
        postJobBtn.setFocusPainted(false);
        postJobBtn.addActionListener(e -> showJobDialog(null)); // 传入 null 表示新增[cite: 41]

        JButton editJobBtn = new JButton("Edit Selected");
        editJobBtn.setBackground(new Color(245, 158, 11)); // 橙色
        editJobBtn.setForeground(Color.ORANGE);
        editJobBtn.setFocusPainted(false);
        editJobBtn.addActionListener(e -> editSelectedJob());

        JButton withdrawJobBtn = new JButton("Withdraw Selected");
        withdrawJobBtn.setBackground(new Color(239, 68, 68)); // 红色
        withdrawJobBtn.setForeground(Color.RED);
        withdrawJobBtn.setFocusPainted(false);
        withdrawJobBtn.addActionListener(e -> withdrawSelectedJob());

        actionPanel.add(withdrawJobBtn);
        actionPanel.add(editJobBtn);
        actionPanel.add(postJobBtn);

        headerPanel.add(actionPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        // 表头保持原版风格[cite: 41]
        String[] columns = {"Job ID", "Module Code", "Course Title","Skills", "Headcount", "Deadline","Weekly Hrs", "Status", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        jobTable = new JTable(tableModel);
        jobTable.setRowHeight(30);
        jobTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        jobTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // newly add！！
        jobTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Job ID
        jobTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Skills
        jobTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Headcount
        jobTable.getColumnModel().getColumn(5).setPreferredWidth(80); // Deadline

        JScrollPane scrollPane = new JScrollPane(jobTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
 * 【新增辅助方法】：用于把拼凑在 Description 里的信息拆解出来
 * 返回一个数组: [0]=Skills, [1]=Headcount, [2]=Deadline, [3]=Details
 */
    private String[] parseDescription(String desc) {
        String[] parsed = new String[]{"", "", "", desc != null ? desc : ""};
        if (desc != null && desc.startsWith("Skills:")) {
            try {
                String[] lines = desc.split("\n", 4); // 最多切成4份
                parsed[0] = lines[0].replace("Skills: ", "").trim();
                parsed[1] = lines[1].replace("Headcount: ", "").trim();
                parsed[2] = lines[2].replace("Deadline: ", "").trim();
                parsed[3] = lines[3].replace("Details: ", "").trim();
            } catch (Exception e) {
            // 解析失败则保留原样
            }
        }
        return parsed;
    }

    private void loadJobData() {
        tableModel.setRowCount(0);
        // 使用 stream 过滤当前 MO 的职位[cite: 41]
        List<MOJob> myJobs = jobService.listAll().stream()
                .filter(j -> j.getMoUserId().equals(currentUser.getUserId()))
                .collect(Collectors.toList());

        for (MOJob job : myJobs) {
            // 【修改点】：调用拆解方法，提取出额外字段
            String[] parsedDesc = parseDescription(job.getDescription());
            tableModel.addRow(new Object[]{
                    job.getJobId(), job.getModuleCode(), job.getTitle(),
                    parsedDesc[0], // 对应 Skills 列
                    parsedDesc[1], // 对应 Headcount 列
                    parsedDesc[2], // 对应 Deadline 列
                    job.getWeeklyHours(), job.getStatus(), 
                    job.getCreatedAt() != null ? job.getCreatedAt().toLocalDate() : "N/A"
            });
        }
    }

    /**
     * 统一的新增/编辑弹窗
     * @param existingJob 如果为 null 则表示发布新职位，如果不为 null 则表示编辑
     */
    private void showJobDialog(MOJob existingJob) {
        boolean isEdit = (existingJob != null);
        
        JTextField moduleCodeField = new JTextField(15);
        JTextField titleField = new JTextField(15);
        JTextField hoursField = new JTextField(15);
        JTextField skillsField = new JTextField(15);    // 技能要求
        JTextField headcountField = new JTextField(15); // 招聘人数
        JTextField deadlineField = new JTextField(15);  // 截止日期 (e.g., YYYY-MM-DD)
        JTextArea descField = new JTextArea(3, 15);

        // 如果是编辑状态，回显原数据
        /* 删了！！！！！！！
        if (isEdit) {
            moduleCodeField.setText(existingJob.getModuleCode());
            titleField.setText(existingJob.getTitle());
            hoursField.setText(String.valueOf(existingJob.getWeeklyHours()));
            // 简单处理：将额外信息都塞在原版 Description 里显示
            descField.setText(existingJob.getDescription()); 
        }
        */

        // 如果是编辑状态，回显原数据  新加的！！！！！！
        if (isEdit) {
            moduleCodeField.setText(existingJob.getModuleCode());
            titleField.setText(existingJob.getTitle());
            hoursField.setText(String.valueOf(existingJob.getWeeklyHours()));
            
            // 【修改点】：将数据拆解后，分别填入各自的输入框
            String[] parsedDesc = parseDescription(existingJob.getDescription());
            skillsField.setText(parsedDesc[0]);
            headcountField.setText(parsedDesc[1]);
            deadlineField.setText(parsedDesc[2]);
            descField.setText(parsedDesc[3]);
        }

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.add(new JLabel("Module Code:")); panel.add(moduleCodeField);
        panel.add(new JLabel("Course Title:")); panel.add(titleField);
        panel.add(new JLabel("Required Skills:")); panel.add(skillsField);
        panel.add(new JLabel("Headcount:")); panel.add(headcountField);
        panel.add(new JLabel("Deadline (YYYY-MM-DD):")); panel.add(deadlineField);
        panel.add(new JLabel("Weekly Hours:")); panel.add(hoursField);
        panel.add(new JLabel("Detailed Description:")); panel.add(new JScrollPane(descField));

        String dialogTitle = isEdit ? "Edit Job" : "Post New Job";
        int result = JOptionPane.showConfirmDialog(this, panel, dialogTitle, JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                MOJob jobToSave = isEdit ? existingJob : new MOJob();
                
                if (!isEdit) {
                    jobToSave.setMoUserId(currentUser.getUserId()); // 只有新增时设置属主[cite: 41]
                    jobToSave.setStatus("OPEN");
                }
                
                jobToSave.setModuleCode(moduleCodeField.getText());
                jobToSave.setTitle(titleField.getText());
                jobToSave.setWeeklyHours(Integer.parseInt(hoursField.getText()));
                
                // 将无法直接存入实体的额外字段，拼接成格式化文本存入 Description[cite: 41]
                String formattedDesc = String.format(
                    "Skills: %s\nHeadcount: %s\nDeadline: %s\nDetails: %s",
                    skillsField.getText(), headcountField.getText(), deadlineField.getText(), descField.getText()
                );
                jobToSave.setDescription(formattedDesc);
                
                // 保存并刷新[cite: 41]
                jobService.createOrUpdate(jobToSave);
                loadJobData(); 
                JOptionPane.showMessageDialog(this, isEdit ? "Job updated successfully!" : "Job posted successfully!");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Weekly hours must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 编辑选中的职位
     */
    private void editSelectedJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long jobId = (Long) tableModel.getValueAt(selectedRow, 0);
        
        // 从 Service 中找到对应的 Job 实体
        MOJob jobToEdit = jobService.listAll().stream()
                .filter(j -> j.getJobId().equals(jobId))
                .findFirst()
                .orElse(null);

        if (jobToEdit != null) {
            showJobDialog(jobToEdit);
        } else {
            JOptionPane.showMessageDialog(this, "Error finding job data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 撤回选中的职位
     */
    private void withdrawSelectedJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job to withdraw.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to withdraw this job? Applicants will no longer be able to apply.", 
            "Confirm Withdraw", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            Long jobId = (Long) tableModel.getValueAt(selectedRow, 0);
            
            MOJob jobToWithdraw = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(jobId))
                    .findFirst()
                    .orElse(null);

            if (jobToWithdraw != null) {
                //jobService.delete(jobToWithdraw);
                jobToWithdraw.setStatus("WITHDRAWN"); // 修改状态
                jobService.createOrUpdate(jobToWithdraw); // 保存更新[cite: 41]
                loadJobData(); // 刷新表格
                JOptionPane.showMessageDialog(this, "Job has been withdrawn.");
            }
        }
    }
}