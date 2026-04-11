package mo.ui;

import common.entity.MOJob;
import common.entity.User;
import common.service.MOJobService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MO job management panel.
 *
 * @version 2.0
 * @contributor Jiaze Wang
 * @update
 * - Added deadline format validation before job save
 * - Delegated application cycle enforcement to MOJobService
 * - Preserved the existing UI layout and description encoding format
 */
public class MOJobManagementPanel extends JPanel {
    private final User currentUser;
    private final MOJobService jobService = new MOJobService();
    private JTable jobTable;
    private DefaultTableModel tableModel;

    public MOJobManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initHeader();
        initTable();
        loadJobData();
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Manage Course Jobs");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton postJobBtn = new JButton("+ Post New Job");
        postJobBtn.setBackground(new Color(59, 130, 246));
        postJobBtn.setForeground(Color.BLUE);
        postJobBtn.setFocusPainted(false);
        postJobBtn.addActionListener(e -> showJobDialog(null));

        JButton editJobBtn = new JButton("Edit Selected");
        editJobBtn.setBackground(new Color(245, 158, 11));
        editJobBtn.setForeground(Color.ORANGE);
        editJobBtn.setFocusPainted(false);
        editJobBtn.addActionListener(e -> editSelectedJob());

        JButton withdrawJobBtn = new JButton("Withdraw Selected");
        withdrawJobBtn.setBackground(new Color(239, 68, 68));
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
        String[] columns = {"Job ID", "Module Code", "Course Title", "Skills", "Headcount", "Deadline", "Weekly Hrs", "Status", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobTable = new JTable(tableModel);
        jobTable.setRowHeight(30);
        jobTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        jobTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jobTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        jobTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        jobTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        jobTable.getColumnModel().getColumn(5).setPreferredWidth(100);

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
                String[] lines = desc.split("\n", 4);
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
        List<MOJob> myJobs = jobService.listAll().stream()
                .filter(j -> j.getMoUserId().equals(currentUser.getUserId()))
                .collect(Collectors.toList());

        for (MOJob job : myJobs) {
            String[] parsedDesc = parseDescription(job.getDescription());
            tableModel.addRow(new Object[]{
                    job.getJobId(),
                    job.getModuleCode(),
                    job.getTitle(),
                    parsedDesc[0],
                    parsedDesc[1],
                    parsedDesc[2],
                    job.getWeeklyHours(),
                    job.getStatus(),
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
        JTextField skillsField = new JTextField(15);
        JTextField headcountField = new JTextField(15);
        JTextField deadlineField = new JTextField(15);
        JTextArea descField = new JTextArea(3, 15);

        if (isEdit) {
            moduleCodeField.setText(existingJob.getModuleCode());
            titleField.setText(existingJob.getTitle());
            hoursField.setText(String.valueOf(existingJob.getWeeklyHours()));

            String[] parsedDesc = parseDescription(existingJob.getDescription());
            skillsField.setText(parsedDesc[0]);
            headcountField.setText(parsedDesc[1]);
            deadlineField.setText(parsedDesc[2]);
            descField.setText(parsedDesc[3]);
        }

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.add(new JLabel("Module Code:"));
        panel.add(moduleCodeField);
        panel.add(new JLabel("Course Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Required Skills:"));
        panel.add(skillsField);
        panel.add(new JLabel("Headcount:"));
        panel.add(headcountField);
        panel.add(new JLabel("Deadline (YYYY-MM-DD):"));
        panel.add(deadlineField);
        panel.add(new JLabel("Weekly Hours:"));
        panel.add(hoursField);
        panel.add(new JLabel("Detailed Description:"));
        panel.add(new JScrollPane(descField));

        String dialogTitle = isEdit ? "Edit Job" : "Post New Job";
        int result = JOptionPane.showConfirmDialog(this, panel, dialogTitle, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String deadlineText = deadlineField.getText().trim();
                if (deadlineText.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Deadline is required and must use format YYYY-MM-DD.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate format early for clearer UI feedback.
                LocalDate.parse(deadlineText);

                MOJob jobToSave = isEdit ? existingJob : new MOJob();

                if (!isEdit) {
                    jobToSave.setMoUserId(currentUser.getUserId());
                    jobToSave.setStatus("OPEN");
                }

                jobToSave.setModuleCode(moduleCodeField.getText().trim());
                jobToSave.setTitle(titleField.getText().trim());
                jobToSave.setWeeklyHours(Integer.parseInt(hoursField.getText().trim()));

                String formattedDesc = String.format(
                        "Skills: %s\nHeadcount: %s\nDeadline: %s\nDetails: %s",
                        skillsField.getText().trim(),
                        headcountField.getText().trim(),
                        deadlineText,
                        descField.getText().trim()
                );
                jobToSave.setDescription(formattedDesc);

                jobService.createOrUpdate(jobToSave);
                loadJobData();
                JOptionPane.showMessageDialog(this, isEdit ? "Job updated successfully!" : "Job posted successfully!");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Deadline must use format YYYY-MM-DD.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Weekly hours must be a valid number.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
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

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to withdraw this job? Applicants will no longer be able to apply.",
                "Confirm Withdraw",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            Long jobId = (Long) tableModel.getValueAt(selectedRow, 0);

            MOJob jobToWithdraw = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(jobId))
                    .findFirst()
                    .orElse(null);

            if (jobToWithdraw != null) {
                jobToWithdraw.setStatus("WITHDRAWN");
                jobService.createOrUpdate(jobToWithdraw);
                loadJobData();
                JOptionPane.showMessageDialog(this, "Job has been withdrawn.");
            }
        }
    }
}