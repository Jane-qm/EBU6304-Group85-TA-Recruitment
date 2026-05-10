package ui.mo;

import com.toedter.calendar.JDateChooser;
import modules.course.Course;
import modules.course.CourseService;
import modules.job.Job;
import modules.job.JobService;
import modules.user.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import ui.common.TableListActionStyle;
import ui.common.TableScrollUtil;

/**
 * MO job management: course dropdown, calendar deadline, draft/publish, row actions.
 */
public class MOJobManagementPanel extends JPanel {

    private static final String DISABLED_ACTION = "—";

    @FunctionalInterface
    private interface JobFormBinder {
        void bind(Job target, String status, LocalDate deadline);
    }

    private final User currentUser;
    private final JobService jobService = new JobService();
    private final CourseService courseService = new CourseService();

    private JTable jobTable;
    private DefaultTableModel tableModel;
    private List<Job> currentJobs;

    public MOJobManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(24, 32, 24, 32));

        initHeader();
        initTable();
        loadJobData();
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JButton postBtn = new JButton("+ Post New Job");
        postBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        MoUiStyles.applyTextButton(postBtn);
        postBtn.addActionListener(e -> showJobEditorDialog(null));

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        east.setOpaque(false);
        east.add(postBtn);
        headerPanel.add(east, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"Module code", "Course name", "Status", "Edit", "Close"};
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

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                boolean actionCol = column >= 3;
                String s = value == null ? "" : value.toString();
                if (!actionCol) {
                    l.setBorder(null);
                    l.setCursor(Cursor.getDefaultCursor());
                    return l;
                }
                if (isSelected) {
                    l.setForeground(table.getSelectionForeground());
                    l.setBackground(table.getSelectionBackground());
                    l.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                    if (TableListActionStyle.isDisabledActionText(s)) {
                        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        l.setCursor(Cursor.getDefaultCursor());
                    } else {
                        l.setFont(new Font("SansSerif", Font.BOLD, 12));
                        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    return l;
                }
                l.setBackground(Color.WHITE);
                TableListActionStyle.applyToLabel(l, s, true);
                return l;
            }
        };
        for (int c = 0; c < columns.length; c++) {
            jobTable.getColumnModel().getColumn(c).setCellRenderer(cellRenderer);
        }

        jobTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = jobTable.rowAtPoint(e.getPoint());
                int col = jobTable.columnAtPoint(e.getPoint());
                if (row < 0 || col < 3) {
                    return;
                }
                Job job = currentJobs.get(row);
                if (col == 3) {
                    showJobEditorDialog(job);
                } else if (col == 4) {
                    if (DISABLED_ACTION.equals(jobTable.getValueAt(row, 4))) {
                        return;
                    }
                    closeJobFromRow(job);
                }
            }
        });

        TableScrollUtil.ColumnSpec[] jobCols = {
                TableScrollUtil.ColumnSpec.fixed(104),
                TableScrollUtil.ColumnSpec.flex(160, 280),
                TableScrollUtil.ColumnSpec.fixed(94),
                TableScrollUtil.ColumnSpec.fixed(78),
                TableScrollUtil.ColumnSpec.fixed(78),
        };

        JScrollPane scrollPane = TableScrollUtil.wrapTable(jobTable);
        TableScrollUtil.installResponsiveColumns(jobTable, scrollPane, jobCols);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private String displayStatus(Job job) {
        String s = job.getStatus();
        if (s == null) {
            return "—";
        }
        if ("DRAFT".equalsIgnoreCase(s)) {
            return "Draft";
        }
        if ("OPEN".equalsIgnoreCase(s) || "PUBLISHED".equalsIgnoreCase(s)) {
            return "Open";
        }
        if ("CLOSED".equalsIgnoreCase(s) || "WITHDRAWN".equalsIgnoreCase(s)) {
            return "Closed";
        }
        return s;
    }

    private void loadJobData() {
        jobService.autoCloseExpiredJobs();
        tableModel.setRowCount(0);
        currentJobs = jobService.listAll().stream()
                .filter(j -> j.getMoUserId() != null && j.getMoUserId().equals(currentUser.getUserId()))
                .collect(Collectors.toList());

        for (Job job : currentJobs) {
            String closeLabel = isJobClosed(job) ? DISABLED_ACTION : "Close";
            tableModel.addRow(new Object[]{
                    job.getModuleCode() != null ? job.getModuleCode() : "",
                    job.getTitle() != null ? job.getTitle() : "",
                    displayStatus(job),
                    "Edit",
                    closeLabel
            });
        }
    }

    private boolean isJobClosed(Job job) {
        String st = job.getStatus();
        return st != null && ("CLOSED".equalsIgnoreCase(st) || "WITHDRAWN".equalsIgnoreCase(st));
    }

    private void closeJobFromRow(Job job) {
        String st = job.getStatus();
        if ("CLOSED".equalsIgnoreCase(st) || "WITHDRAWN".equalsIgnoreCase(st)) {
            JOptionPane.showMessageDialog(this, "This job is already closed.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Close recruitment? TAs will no longer be able to apply.\n"
                        + job.getModuleCode() + " — " + job.getTitle(),
                "Confirm close",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            jobService.closeJob(job.getJobId());
            loadJobData();
            JOptionPane.showMessageDialog(this, "Recruitment closed.", "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] parseDescription(String desc) {
        String[] parsed = new String[]{"", "", "", desc != null ? desc : ""};
        if (desc != null && desc.startsWith("Skills:")) {
            try {
                String[] lines = desc.split("\n", 4);
                parsed[0] = lines[0].replace("Skills: ", "").trim();
                parsed[1] = lines[1].replace("Headcount: ", "").trim();
                parsed[2] = lines[2].replace("Deadline: ", "").trim();
                parsed[3] = lines.length > 3 ? lines[3].replace("Details: ", "").trim() : "";
            } catch (Exception ignored) {
                // keep defaults
            }
        }
        return parsed;
    }

    private int effectiveHeadcount(Job job) {
        if (job.getHeadcount() > 0) {
            return job.getHeadcount();
        }
        String hc = parseDescription(job.getDescription())[1];
        try {
            return Integer.parseInt(hc.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String effectiveSkillsText(Job job) {
        if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) {
            return String.join(", ", job.getRequiredSkills());
        }
        return parseDescription(job.getDescription())[0];
    }

    private LocalDate effectiveDeadlineDate(Job job) {
        if (job.getApplicationDeadline() != null) {
            return job.getApplicationDeadline().toLocalDate();
        }
        String d = parseDescription(job.getDescription())[2];
        if (d == null || d.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(d.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String effectiveDetails(Job job) {
        return parseDescription(job.getDescription())[3];
    }

    private void assembleDescription(Job job, String detailsPlain, LocalDate deadline) {
        String skillsLine = job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()
                ? ""
                : String.join(", ", job.getRequiredSkills());
        String dl = deadline != null ? deadline.toString() : "";
        String hc = String.valueOf(Math.max(0, job.getHeadcount()));
        job.setDescription(String.format(
                "Skills: %s\nHeadcount: %s\nDeadline: %s\nDetails: %s",
                skillsLine,
                hc,
                dl,
                detailsPlain == null ? "" : detailsPlain.trim()));
    }

    private List<String> splitSkills(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Course> sortedCourses() {
        List<Course> list = new ArrayList<>(courseService.getAllCourses());
        list.sort(Comparator.comparing(
                c -> c.getModuleCode() != null ? c.getModuleCode() : "",
                String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    private void showJobEditorDialog(Job existing) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, existing == null ? "Post new job" : "Edit job",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(12, 12));

        List<Course> allCourses = sortedCourses();
        if (allCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No courses in the catalog. Ask an administrator to import courses first.",
                    "No courses",
                    JOptionPane.WARNING_MESSAGE);
            dlg.dispose();
            return;
        }

        DefaultComboBoxModel<Course> courseModel = new DefaultComboBoxModel<>();
        for (Course c : allCourses) {
            courseModel.addElement(c);
        }
        JComboBox<Course> courseCombo = new JComboBox<>(courseModel);
        courseCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JTextField courseFilterField = new JTextField(18);
        courseFilterField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        courseFilterField.setToolTipText("Filter by module code or course title, then click Search");
        JButton courseSearchBtn = new JButton("Search");
        MoUiStyles.applyTextButton(courseSearchBtn);
        Runnable applyCourseFilter = () -> {
            String q = courseFilterField.getText().trim().toLowerCase();
            Course selected = (Course) courseCombo.getSelectedItem();
            courseModel.removeAllElements();
            for (Course c : allCourses) {
                if (q.isEmpty()) {
                    courseModel.addElement(c);
                    continue;
                }
                String code = c.getModuleCode() != null ? c.getModuleCode().toLowerCase() : "";
                String title = c.getTitle() != null ? c.getTitle().toLowerCase() : "";
                if (code.contains(q) || title.contains(q)) {
                    courseModel.addElement(c);
                }
            }
            if (courseModel.getSize() == 0) {
                JOptionPane.showMessageDialog(dlg, "No courses match your search.", "Search",
                        JOptionPane.INFORMATION_MESSAGE);
                for (Course c : allCourses) {
                    courseModel.addElement(c);
                }
                return;
            }
            if (selected != null && selected.getModuleCode() != null) {
                String selCode = selected.getModuleCode();
                for (int i = 0; i < courseModel.getSize(); i++) {
                    Course c = courseModel.getElementAt(i);
                    if (c.getModuleCode() != null && c.getModuleCode().equalsIgnoreCase(selCode)) {
                        courseCombo.setSelectedIndex(i);
                        return;
                    }
                }
            }
            courseCombo.setSelectedIndex(0);
        };
        courseSearchBtn.addActionListener(e -> applyCourseFilter.run());
        courseFilterField.addActionListener(e -> applyCourseFilter.run());

        JDateChooser deadlineChooser = new JDateChooser();
        deadlineChooser.setDateFormatString("yyyy-MM-dd");
        deadlineChooser.setPreferredSize(new Dimension(160, 32));

        JSpinner headcountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JTextField skillsField = new JTextField(24);
        JTextField hoursField = new JTextField("10", 8);
        JTextArea detailsArea = new JTextArea(4, 28);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        final boolean isNew = (existing == null);
        final String originalStatus = existing != null ? existing.getStatus() : null;
        final boolean isClosed = existing != null && "CLOSED".equalsIgnoreCase(originalStatus);

        if (existing != null) {
            skillsField.setText(effectiveSkillsText(existing));
            headcountSpinner.setValue(Math.max(1, effectiveHeadcount(existing)));
            hoursField.setText(String.valueOf(Math.max(0, existing.getWeeklyHours())));
            detailsArea.setText(effectiveDetails(existing));
            LocalDate dl = effectiveDeadlineDate(existing);
            if (dl != null) {
                deadlineChooser.setDate(Date.from(dl.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            String code = existing.getModuleCode();
            for (int j = 0; j < courseCombo.getItemCount(); j++) {
                Course c = courseCombo.getItemAt(j);
                if (code != null && c.getModuleCode() != null && c.getModuleCode().equalsIgnoreCase(code)) {
                    courseCombo.setSelectedIndex(j);
                    break;
                }
            }
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        JPanel coursePanel = new JPanel(new BorderLayout(8, 6));
        coursePanel.setOpaque(false);
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterRow.setOpaque(false);
        filterRow.add(courseFilterField);
        filterRow.add(courseSearchBtn);
        coursePanel.add(filterRow, BorderLayout.NORTH);
        coursePanel.add(courseCombo, BorderLayout.CENTER);
        form.add(coursePanel, gbc);
        gbc.weightx = 0;

        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Application deadline:"), gbc);
        gbc.gridx = 1;
        form.add(deadlineChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Number of TAs to hire:"), gbc);
        gbc.gridx = 1;
        form.add(headcountSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Required skills (comma-separated):"), gbc);
        gbc.gridx = 1;
        form.add(skillsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Weekly hours:"), gbc);
        gbc.gridx = 1;
        form.add(hoursField, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++y;
        gbc.gridwidth = 2;
        form.add(new JLabel("Job description:"), gbc);
        gbc.gridy = ++y;
        form.add(new JScrollPane(detailsArea), gbc);

        boolean readOnly = isClosed;
        courseCombo.setEnabled(!readOnly);
        deadlineChooser.setEnabled(!readOnly);
        headcountSpinner.setEnabled(!readOnly);
        skillsField.setEnabled(!readOnly);
        hoursField.setEnabled(!readOnly);
        detailsArea.setEnabled(!readOnly);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton cancelBtn = new JButton("Cancel");
        MoUiStyles.applyTextButton(cancelBtn);
        cancelBtn.addActionListener(e -> dlg.dispose());

        JobFormBinder applyForm = (target, status, deadline) -> {
            Course sel = (Course) courseCombo.getSelectedItem();
            if (sel == null) {
                throw new IllegalArgumentException("Please select a course.");
            }
            int hours;
            try {
                hours = Integer.parseInt(hoursField.getText().trim());
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Weekly hours must be a valid number.");
            }
            if (hours < 0) {
                throw new IllegalArgumentException("Weekly hours cannot be negative.");
            }
            target.setModuleCode(sel.getModuleCode());
            target.setTitle(sel.getTitle());
            target.setWeeklyHours(hours);
            target.setHeadcount((Integer) headcountSpinner.getValue());
            target.setRequiredSkills(splitSkills(skillsField.getText()));
            target.setStatus(status);
            assembleDescription(target, detailsArea.getText(), deadline);
        };

        if (!readOnly && (isNew || "DRAFT".equalsIgnoreCase(originalStatus))) {
            JButton draftBtn = new JButton("Save draft");
            MoUiStyles.applyTextButton(draftBtn);
            draftBtn.addActionListener(e -> {
                try {
                    Job job = existing != null ? existing : new Job();
                    if (isNew) {
                        job.setMoUserId(currentUser.getUserId());
                    }
                    Date d = deadlineChooser.getDate();
                    LocalDate deadline = d == null ? null
                            : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    applyForm.bind(job, "DRAFT", deadline);
                    jobService.createOrUpdate(job);
                    loadJobData();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Draft saved.", "Done", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttons.add(draftBtn);

            JButton publishBtn = new JButton("Publish");
            MoUiStyles.applyTextButton(publishBtn);
            publishBtn.addActionListener(e -> {
                try {
                    Date d = deadlineChooser.getDate();
                    if (d == null) {
                        throw new IllegalArgumentException("Choose an application deadline before publishing.");
                    }
                    LocalDate deadline = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Job job = existing != null ? existing : new Job();
                    if (isNew) {
                        job.setMoUserId(currentUser.getUserId());
                    }
                    applyForm.bind(job, "OPEN", deadline);
                    jobService.createOrUpdate(job);
                    loadJobData();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Job published.", "Done", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, formatPublishError(ex.getMessage()),
                            "Cannot publish", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttons.add(publishBtn);
        } else if (!readOnly) {
            JButton saveBtn = new JButton("Save");
            MoUiStyles.applyTextButton(saveBtn);
            saveBtn.addActionListener(e -> {
                try {
                    Date d = deadlineChooser.getDate();
                    if (d == null) {
                        throw new IllegalArgumentException("Choose an application deadline.");
                    }
                    LocalDate deadline = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Job job = existing;
                    String st = "OPEN";
                    if ("PUBLISHED".equalsIgnoreCase(originalStatus)) {
                        st = "PUBLISHED";
                    }
                    applyForm.bind(job, st, deadline);
                    jobService.createOrUpdate(job);
                    loadJobData();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Saved.", "Done", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, formatPublishError(ex.getMessage()),
                            "Cannot save", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttons.add(saveBtn);
        } else {
            JButton okBtn = new JButton("Close");
            MoUiStyles.applyTextButton(okBtn);
            okBtn.addActionListener(e -> dlg.dispose());
            buttons.add(okBtn);
        }

        if (!readOnly) {
            buttons.add(cancelBtn);
        }

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(buttons, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /**
     * Clarifies recruitment window and deadline rules from the admin configuration.
     */
    private static String formatPublishError(String msg) {
        if (msg == null) {
            return "Unable to save.";
        }
        String lower = msg.toLowerCase();
        if (lower.contains("recruitment")
                || lower.contains("deadline")
                || lower.contains("application cycle")) {
            return msg + "\n\nOpen jobs need a deadline in the recruitment period set by an administrator, "
                    + "and first-time publishing is only allowed while that period is active.";
        }
        return msg;
    }
}
