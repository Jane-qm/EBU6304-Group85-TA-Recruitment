package ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import modules.application.Application;
import modules.application.ApplicationService;
import modules.application.ApplicationStatus;
import modules.course.Course;
import modules.course.CourseService;
import modules.job.Job;
import modules.job.JobService;
import modules.user.MO;
import modules.user.User;
import modules.user.UserService;
import ui.common.JobDetailDialog;
import ui.common.TableListActionStyle;
import ui.common.TableScrollUtil;

/**
 * Course Management Panel for Admin — one row per job posting (same module code, different MOs → multiple rows).
 */
public class CourseManagementPanel extends JPanel {
    private static final int COL_DETAIL = 5;
    private static final int COL_APPLICANTS = 6;
    private static final int COL_HIRED = 7;

    private final CourseService courseService = new CourseService();
    private final JobService jobService = new JobService();
    private final ApplicationService appService = new ApplicationService();
    private final UserService userService = UserService.getInstance();

    private JTable table;
    private DefaultTableModel tableModel;
    /** Full list before search filter */
    private List<CourseJobRow> allRows = new ArrayList<>();
    private List<CourseJobRow> displayRows = new ArrayList<>();

    private JTextField searchField;
    private JComboBox<String> searchAttrCombo;

    public CourseManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private static final class CourseJobRow {
        final Course course;
        final Job job;

        CourseJobRow(Course course, Job job) {
            this.course = course;
            this.job = job;
        }
    }

    private void initUI() {
        JPanel north = new JPanel();
        north.setLayout(new javax.swing.BoxLayout(north, javax.swing.BoxLayout.Y_AXIS));
        north.setOpaque(false);

        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(titleLabel);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(new JLabel("Search:"));
        searchField = new JTextField(22);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchAttrCombo = new JComboBox<>(new String[]{
                "All fields", "Course code", "Course title", "MO name", "Job status", "Job ID"
        });
        searchAttrCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchRow.add(searchField);
        searchRow.add(searchAttrCombo);
        north.add(searchRow);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setOpaque(false);
        topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton importBtn = createButton("📥 Import Courses from CSV");
        importBtn.addActionListener(e -> importCoursesFromCSV());

        JButton addBtn = createButton("➕ Add Single Course");
        addBtn.addActionListener(e -> showAddCourseDialog());

        topPanel.add(importBtn);
        topPanel.add(addBtn);
        north.add(topPanel);

        add(north, BorderLayout.NORTH);

        String[] columns = {
                "Course Code", "Course Title", "Job ID", "Job status", "Published by MO",
                "View Detail", "View Applicants", "View Hired"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (row < 0 || row >= displayRows.size()) {
                    return false;
                }
                if (displayRows.get(row).job == null) {
                    return false;
                }
                return column == COL_DETAIL || column == COL_APPLICANTS || column == COL_HIRED;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));

        TextButtonOrDashRenderer dashRenderer = new TextButtonOrDashRenderer();
        table.getColumnModel().getColumn(COL_DETAIL).setCellRenderer(dashRenderer);
        table.getColumnModel().getColumn(COL_APPLICANTS).setCellRenderer(dashRenderer);
        table.getColumnModel().getColumn(COL_HIRED).setCellRenderer(dashRenderer);
        table.getColumnModel().getColumn(COL_DETAIL).setCellEditor(new JobActionEditor());
        table.getColumnModel().getColumn(COL_APPLICANTS).setCellEditor(new JobActionEditor());
        table.getColumnModel().getColumn(COL_HIRED).setCellEditor(new JobActionEditor());

        TableScrollUtil.ColumnSpec[] courseCols = {
                TableScrollUtil.ColumnSpec.fixed(100),
                TableScrollUtil.ColumnSpec.flex(120, 260),
                TableScrollUtil.ColumnSpec.fixed(72),
                TableScrollUtil.ColumnSpec.fixed(88),
                TableScrollUtil.ColumnSpec.flex(100, 200),
                TableScrollUtil.ColumnSpec.fixed(96),
                TableScrollUtil.ColumnSpec.fixed(128),
                TableScrollUtil.ColumnSpec.fixed(108),
        };

        JScrollPane courseScroll = TableScrollUtil.wrapTable(table);
        TableScrollUtil.installResponsiveColumns(table, courseScroll, courseCols);
        add(courseScroll, BorderLayout.CENTER);

        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        };
        searchField.getDocument().addDocumentListener(dl);
        searchAttrCombo.addActionListener(e -> applySearchFilter());
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        TableListActionStyle.applyToButton(button, text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        return button;
    }

    private void loadData() {
        allRows.clear();
        List<Course> courses = courseService.getAllCourses();
        List<Job> allJobs = jobService.listAll();

        for (Course course : courses) {
            List<Job> forModule = allJobs.stream()
                    .filter(j -> course.getModuleCode() != null
                            && course.getModuleCode().equals(j.getModuleCode()))
                    .sorted(Comparator.comparing(Job::getJobId, Comparator.nullsLast(Long::compareTo)))
                    .collect(Collectors.toList());
            if (forModule.isEmpty()) {
                allRows.add(new CourseJobRow(course, null));
            } else {
                for (Job j : forModule) {
                    allRows.add(new CourseJobRow(course, j));
                }
            }
        }
        applySearchFilter();
    }

    private void applySearchFilter() {
        String q = searchField.getText().trim().toLowerCase();
        String attrRaw = (String) searchAttrCombo.getSelectedItem();
        final String attr = attrRaw == null ? "All fields" : attrRaw;

        displayRows = allRows.stream()
                .filter(row -> rowMatchesSearch(row, q, attr))
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (CourseJobRow row : displayRows) {
            Course c = row.course;
            Job j = row.job;
            String jobIdStr = j != null && j.getJobId() != null ? String.valueOf(j.getJobId()) : "—";
            String statusStr = j != null ? jobStatusLabel(j) : "—";
            String moName = j != null ? moDisplayNameForJob(j) : "—";
            String detailLbl = j != null ? "View Detail" : "—";
            String appLbl = j != null ? "View Applicants" : "—";
            String hiredLbl = j != null ? "View Hired" : "—";
            tableModel.addRow(new Object[]{
                    c.getModuleCode(),
                    c.getTitle(),
                    jobIdStr,
                    statusStr,
                    moName,
                    detailLbl,
                    appLbl,
                    hiredLbl
            });
        }
    }

    private boolean rowMatchesSearch(CourseJobRow row, String q, String attr) {
        if (q.isEmpty()) {
            return true;
        }
        Course c = row.course;
        Job j = row.job;
        String code = safeLower(c.getModuleCode());
        String title = safeLower(c.getTitle());
        String mo = j != null ? safeLower(moDisplayNameForJob(j)) : "";
        String st = j != null ? safeLower(jobStatusLabel(j)) : "";
        String jid = j != null && j.getJobId() != null ? String.valueOf(j.getJobId()) : "";

        return switch (attr) {
            case "Course code" -> code.contains(q);
            case "Course title" -> title.contains(q);
            case "MO name" -> mo.contains(q);
            case "Job status" -> st.contains(q);
            case "Job ID" -> jid.contains(q);
            default -> code.contains(q) || title.contains(q) || mo.contains(q) || st.contains(q) || jid.contains(q);
        };
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String jobStatusLabel(Job job) {
        if (job == null) {
            return "—";
        }
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

    private String moDisplayNameForJob(Job job) {
        if (job == null || job.getMoUserId() == null) {
            return "—";
        }
        User mo = userService.findById(job.getMoUserId());
        if (mo == null) {
            return "MO #" + job.getMoUserId();
        }
        if (mo instanceof MO) {
            String n = ((MO) mo).getName();
            if (n != null && !n.isBlank()) {
                return n.trim();
            }
        }
        return extractNameFromEmail(mo.getEmail());
    }

    private class TextButtonOrDashRenderer extends DefaultTableCellRenderer {
        TextButtonOrDashRenderer() {
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
            String s = value == null ? "" : value.toString();
            if ("—".equals(s)) {
                setForeground(new Color(107, 114, 128));
                setFont(new Font("SansSerif", Font.PLAIN, 14));
            } else {
                setFont(new Font("SansSerif", Font.BOLD, 13));
                TableListActionStyle.applyToLabel(this, s, true);
            }
            return this;
        }
    }

    private class JobActionEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button = new JButton();
        private int row;
        private int actionColumn;

        JobActionEditor() {
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int col) {
            this.row = row;
            this.actionColumn = col;
            String s = value == null ? "" : value.toString();
            button.setText(s);
            if (!"—".equals(s)) {
                TableListActionStyle.applyToButton(button, s);
            }
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (row < 0 || row >= displayRows.size()) {
                fireEditingStopped();
                return;
            }
            CourseJobRow r = displayRows.get(row);
            if (r.job == null) {
                fireEditingStopped();
                return;
            }
            if (actionColumn == COL_DETAIL) {
                JobDetailDialog.show(CourseManagementPanel.this, r.job);
            } else if (actionColumn == COL_APPLICANTS) {
                viewApplicants(r.job, r.course);
            } else if (actionColumn == COL_HIRED) {
                viewHiredTAs(r.job, r.course);
            }
            fireEditingStopped();
        }
    }

    private void importCoursesFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Courses CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        CourseService.CourseImportResult result = courseService.importCoursesFromCSV(fileChooser.getSelectedFile().getAbsolutePath());

        String message = String.format("Import completed!\nSuccess: %d\nFailed: %d",
                result.successCount, result.failCount);
        if (!result.errors.isEmpty()) {
            message += "\n\nErrors:\n" + String.join("\n", result.errors);
        }
        JOptionPane.showMessageDialog(this, message, "Import Result",
                result.failCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);

        loadData();
    }

    private void showAddCourseDialog() {
        JTextField codeField = new JTextField(15);
        JTextField titleField = new JTextField(20);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        panel.add(codeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Course Title:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Course",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();

            if (code.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both course code and title are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                courseService.addCourse(code, title);
                JOptionPane.showMessageDialog(this, "Course added successfully!\n" + code + " - " + title, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Failed to add course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewApplicants(Job job, Course course) {
        List<Application> allApps = appService.listAll();
        List<Application> jobApps = allApps.stream()
                .filter(app -> job.getJobId().equals(app.getJobId()))
                .filter(app -> !ApplicationStatus.isHired(app.getStatus()))
                .toList();

        if (jobApps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No applicants for this job (ID " + job.getJobId() + ").",
                    "No Applicants", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Applicants: ").append(course.getModuleCode()).append(" — ").append(course.getTitle())
                .append(" (Job ID ").append(job.getJobId()).append(", MO: ")
                .append(moDisplayNameForJob(job)).append(") ===\n\n");

        for (Application app : jobApps) {
            User ta = userService.findById(app.getTaUserId());
            String taName = getTAName(ta);
            sb.append("TA: ").append(taName).append("\n");
            sb.append("   Status: ").append(ApplicationStatus.getDisplayText(app.getStatus())).append("\n");
            sb.append("   Applied: ").append(app.getAppliedAt()).append("\n");
            if (app.getStatement() != null && !app.getStatement().isEmpty()) {
                sb.append("   Statement: ").append(app.getStatement()).append("\n");
            }
            sb.append("---\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Applicants — " + course.getModuleCode(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewHiredTAs(Job job, Course course) {
        List<Application> allApps = appService.listAll();
        List<Application> hiredApps = allApps.stream()
                .filter(app -> job.getJobId().equals(app.getJobId()))
                .filter(app -> ApplicationStatus.isHired(app.getStatus()))
                .toList();

        if (hiredApps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hired TAs for this job (ID " + job.getJobId() + ").",
                    "No Hired TAs", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Hired TAs: ").append(course.getModuleCode()).append(" — ").append(course.getTitle())
                .append(" (Job ID ").append(job.getJobId()).append(") ===\n\n");

        for (Application app : hiredApps) {
            User ta = userService.findById(app.getTaUserId());
            String taName = getTAName(ta);
            sb.append("TA: ").append(taName).append("\n");
            sb.append("   Email: ").append(ta != null ? ta.getEmail() : "Unknown").append("\n");
            sb.append("   Offered Hours: ").append(app.getOfferedHours() != null ? app.getOfferedHours() : "N/A").append(" hours/week\n");
            sb.append("   Hired At: ").append(app.getRespondedAt() != null ? app.getRespondedAt() : "N/A").append("\n");
            sb.append("---\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Hired TAs — " + course.getModuleCode(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String getTAName(User ta) {
        if (ta == null) {
            return "Unknown";
        }
        return extractNameFromEmail(ta.getEmail());
    }

    private String extractNameFromEmail(String email) {
        if (email == null) {
            return "Unknown";
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    public void refresh() {
        loadData();
    }
}
