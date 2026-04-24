package mo.ui;

import common.entity.MOJob;
import common.entity.User;
import common.service.MOJobService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MO job management panel.
 *
 * @version 3.0
 * @contributor Jiaze Wang
 * @update
 * - Added deadline format validation before job save
 * - Delegated application cycle enforcement to MOJobService
 * - Simplified table view: only Module Code, Course Title, Headcount, Deadline + Detail button
 * - Added detail dialog to show full job information
 */
public class MOJobManagementPanel extends JPanel {
    private final User currentUser;
    private final MOJobService jobService = new MOJobService();
    private JTable jobTable;
    private DefaultTableModel tableModel;
    private List<MOJob> currentJobs;   // 存储当前显示的职位列表，与表格行对应

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
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton postJobBtn = createStyledButton("+ Post New Job", new Color(59, 130, 246));
        postJobBtn.setBackground(new Color(59, 130, 246));
        //postJobBtn.setForeground(Color.WHITE);
        postJobBtn.setFocusPainted(false);
        postJobBtn.addActionListener(e -> showJobDialog(null));

        JButton editJobBtn = createStyledButton("Edit Selected", new Color(245, 158, 11));
        editJobBtn.setBackground(new Color(245, 158, 11));
        //editJobBtn.setForeground(Color.WHITE);
        editJobBtn.setFocusPainted(false);
        editJobBtn.addActionListener(e -> editSelectedJob());

        JButton withdrawJobBtn = createStyledButton("Withdraw Selected", new Color(239, 68, 68));
        withdrawJobBtn.setBackground(new Color(239, 68, 68));
        //withdrawJobBtn.setForeground(Color.WHITE);
        withdrawJobBtn.setFocusPainted(false);
        withdrawJobBtn.addActionListener(e -> withdrawSelectedJob());

        JButton closeJobBtn = createStyledButton("Close Recruitment", new Color(107, 114, 128));
        closeJobBtn.setBackground(new Color(107, 114, 128));
        //closeJobBtn.setForeground(Color.WHITE);
        closeJobBtn.setFocusPainted(false);
        closeJobBtn.setToolTipText("Stop accepting new applications for the selected job");
        closeJobBtn.addActionListener(e -> closeSelectedJob());

        actionPanel.add(withdrawJobBtn);
        actionPanel.add(closeJobBtn);
        actionPanel.add(editJobBtn);
        actionPanel.add(postJobBtn);

        headerPanel.add(actionPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(color);
        button.setBackground(null);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(color, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    private void initTable() {
        // 精简列：Module Code, Course Title, Headcount, Deadline, Detail (按钮)
        String[] columns = {"Module Code", "Course Title", "Headcount", "Deadline", "Detail"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobTable = new JTable(tableModel);
        jobTable.setRowHeight(40);
        jobTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        jobTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 设置列宽
        jobTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        jobTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        jobTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        jobTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        jobTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        // 鼠标监听处理 Detail 按钮点击
        jobTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = jobTable.rowAtPoint(e.getPoint());
                int col = jobTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 4) { // Detail 列
                    MOJob job = currentJobs.get(row);
                    showDetailDialog(job);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(jobTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 解析 description 字段中的信息
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
        currentJobs = jobService.listAll().stream()
                .filter(j -> j.getMoUserId().equals(currentUser.getUserId()))
                .collect(Collectors.toList());

        for (MOJob job : currentJobs) {
            String[] parsedDesc = parseDescription(job.getDescription());
            String headcount = parsedDesc[1].isEmpty() ? "N/A" : parsedDesc[1];
            String deadline = parsedDesc[2].isEmpty() ? "N/A" : parsedDesc[2];

            tableModel.addRow(new Object[]{
                    job.getModuleCode(),
                    job.getTitle(),
                    headcount,
                    deadline,
                    "🔍 Detail"
            });
        }
    }

    /**
     * 显示职位详细信息弹窗
     */
    private void showDetailDialog(MOJob job) {
        String[] parsedDesc = parseDescription(job.getDescription());
        StringBuilder info = new StringBuilder();
        info.append("Job ID: ").append(job.getJobId()).append("\n");
        info.append("Module Code: ").append(job.getModuleCode()).append("\n");
        info.append("Course Title: ").append(job.getTitle()).append("\n");
        info.append("Weekly Hours: ").append(job.getWeeklyHours()).append("\n");
        info.append("Headcount: ").append(parsedDesc[1].isEmpty() ? "N/A" : parsedDesc[1]).append("\n");
        info.append("Deadline: ").append(parsedDesc[2].isEmpty() ? "N/A" : parsedDesc[2]).append("\n");
        info.append("Status: ").append(job.getStatus()).append("\n");
        info.append("Skills: ").append(parsedDesc[0].isEmpty() ? "Not specified" : parsedDesc[0]).append("\n");
        info.append("Created At: ").append(job.getCreatedAt() != null ? job.getCreatedAt().toLocalDate() : "N/A").append("\n");
        info.append("\n--- Detailed Description ---\n");
        info.append(parsedDesc[3].isEmpty() ? "No description" : parsedDesc[3]);

        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Job Details - " + job.getModuleCode(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 统一的新增/编辑弹窗 (保持不变)
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

    private void editSelectedJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MOJob jobToEdit = currentJobs.get(selectedRow);
        showJobDialog(jobToEdit);
    }

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
            MOJob jobToWithdraw = currentJobs.get(selectedRow);
            jobToWithdraw.setStatus("WITHDRAWN");
            jobService.createOrUpdate(jobToWithdraw);
            loadJobData();
            JOptionPane.showMessageDialog(this, "Job has been withdrawn.");
        }
    }

    private void closeSelectedJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a job to close.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MOJob job = currentJobs.get(selectedRow);
        String currentStatus = job.getStatus();

        if ("CLOSED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "This job is already closed.",
                    "Already Closed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if ("WITHDRAWN".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "This job has been withdrawn and cannot be closed.",
                    "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html>Close recruitment for:<br><b>" + job.getModuleCode() + " — " + job.getTitle() + "</b>?<br><br>"
                        + "TAs will no longer be able to apply for this job.<br>"
                        + "This action can only be undone by re-editing the job.</html>",
                "Confirm Close Recruitment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                jobService.closeJob(job.getJobId());
                loadJobData();
                JOptionPane.showMessageDialog(this,
                        "Recruitment closed. The job is no longer visible to TAs.",
                        "Closed", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}