package ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

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

/**
 * Course Management Panel for Admin
 * All buttons: white background, black text, black border
 */
public class CourseManagementPanel extends JPanel {
    private final CourseService courseService = new CourseService();
    private final JobService jobService = new JobService();
    private final ApplicationService appService = new ApplicationService();
    private final UserService userService = new UserService();
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Course> courses;

    public CourseManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Top button panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(248, 250, 252));

        JButton importBtn = createButton("📥 Import Courses from CSV");
        importBtn.addActionListener(e -> importCoursesFromCSV());

        JButton addBtn = createButton("➕ Add Single Course");
        addBtn.addActionListener(e -> showAddCourseDialog());

        topPanel.add(importBtn);
        topPanel.add(addBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table - define columns
        String[] columns = {"Course Code", "Course Title", "Published by MO", "View Applicants", "View Hired"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Set custom renderer and editor for button columns
        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor());
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor());

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        courses = courseService.getAllCourses();

        for (Course course : courses) {
            String moName = getMONameForCourse(course.getModuleCode());

            tableModel.addRow(new Object[]{
                    course.getModuleCode(),
                    course.getTitle(),
                    moName,
                    "View Applicants",
                    "View Hired"
            });
        }
    }

    // Button Renderer
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFocusPainted(false);
            setFont(new Font("SansSerif", Font.PLAIN, 11));
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (column == 3) {
                setText("View Applicants");
            } else if (column == 4) {
                setText("View Hired");
            }
            return this;
        }
    }

    // Button Editor
    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JButton button;
        private int currentRow;
        private int currentColumn;

        public ButtonEditor() {
            button = new JButton();
            button.setFont(new Font("SansSerif", Font.PLAIN, 11));
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            button.setFocusPainted(false);
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            currentColumn = column;

            if (column == 3) {
                button.setText("View Applicants");
            } else if (column == 4) {
                button.setText("View Hired");
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Course course = courses.get(currentRow);

            if (currentColumn == 3) {
                viewApplicants(course);
            } else if (currentColumn == 4) {
                viewHiredTAs(course);
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

    private String getMONameForCourse(String moduleCode) {
        if (moduleCode == null) {
            return "—";
        }
        List<Job> jobs = jobService.listAll();
        Optional<Job> published = jobs.stream()
                .filter(j -> moduleCode.equals(j.getModuleCode()))
                .filter(j -> {
                    String s = j.getStatus();
                    return s != null && ("OPEN".equalsIgnoreCase(s) || "PUBLISHED".equalsIgnoreCase(s));
                })
                .min(Comparator.comparing(Job::getJobId));
        Optional<Job> any = jobs.stream()
                .filter(j -> moduleCode.equals(j.getModuleCode()))
                .min(Comparator.comparing(Job::getJobId));
        Job job = published.or(() -> any).orElse(null);
        if (job == null) {
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

    private void viewApplicants(Course course) {
        List<Job> jobs = jobService.listAll().stream()
                .filter(j -> course.getModuleCode().equals(j.getModuleCode()))
                .toList();

        if (jobs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No jobs found for course: " + course.getModuleCode(),
                    "No Applicants", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Application> allApps = appService.listAll();
        List<Application> courseApps = allApps.stream()
                .filter(app -> jobs.stream().anyMatch(j -> j.getJobId().equals(app.getJobId())))
                .filter(app -> !ApplicationStatus.isHired(app.getStatus()))
                .toList();

        if (courseApps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No applicants found for course: " + course.getModuleCode(),
                    "No Applicants", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Applicants for ").append(course.getModuleCode()).append(" - ").append(course.getTitle()).append(" ===\n\n");

        for (Application app : courseApps) {
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

        JOptionPane.showMessageDialog(this, sb.toString(), "Applicants - " + course.getModuleCode(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewHiredTAs(Course course) {
        List<Job> jobs = jobService.listAll().stream()
                .filter(j -> course.getModuleCode().equals(j.getModuleCode()))
                .toList();

        if (jobs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No jobs found for course: " + course.getModuleCode(),
                    "No Hired TAs", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Application> allApps = appService.listAll();
        List<Application> hiredApps = allApps.stream()
                .filter(app -> jobs.stream().anyMatch(j -> j.getJobId().equals(app.getJobId())))
                .filter(app -> ApplicationStatus.isHired(app.getStatus()))
                .toList();

        if (hiredApps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hired TAs found for course: " + course.getModuleCode(),
                    "No Hired TAs", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Hired TAs for ").append(course.getModuleCode()).append(" - ").append(course.getTitle()).append(" ===\n\n");

        for (Application app : hiredApps) {
            User ta = userService.findById(app.getTaUserId());
            String taName = getTAName(ta);
            sb.append("TA: ").append(taName).append("\n");
            sb.append("   Email: ").append(ta != null ? ta.getEmail() : "Unknown").append("\n");
            sb.append("   Offered Hours: ").append(app.getOfferedHours() != null ? app.getOfferedHours() : "N/A").append(" hours/week\n");
            sb.append("   Hired At: ").append(app.getRespondedAt() != null ? app.getRespondedAt() : "N/A").append("\n");
            sb.append("---\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Hired TAs - " + course.getModuleCode(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String getTAName(User ta) {
        if (ta == null) return "Unknown";
        return extractNameFromEmail(ta.getEmail());
    }

    private String extractNameFromEmail(String email) {
        if (email == null) return "Unknown";
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    public void refresh() {
        loadData();
    }
}