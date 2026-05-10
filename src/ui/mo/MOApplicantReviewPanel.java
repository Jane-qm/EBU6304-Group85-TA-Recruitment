package ui.mo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import modules.application.Application;
import modules.application.ApplicationService;
import modules.application.ApplicationStatus;
import modules.cv.CVInfo;
import modules.cv.CVService;
import modules.job.Job;
import modules.job.JobService;
import modules.profile.TAProfile;
import modules.profile.TAProfileService;
import modules.user.User;
import modules.user.UserService;
import ui.common.TableListActionStyle;
import ui.common.TableScrollUtil;
import ui.common.TaProfileViewer;

/**
 * MO applicant review: filter by course and status; per-row profile, CV, accept, reject, shortlist.
 */
public class MOApplicantReviewPanel extends JPanel {

    private static final String ALL_STATUSES = "All statuses";
    private static final String ST_PENDING = "Pending review (not seen)";
    private static final String ST_SHORTLIST = "Shortlisted";
    private static final String ST_REJECTED = "Rejected";
    private static final String ST_OFFER_SENT = "Offer sent";
    private static final String ST_HIRED = "Hired";

    /** Inactive action cells (same as TA tables): not editable, rendered as plain em dash. */
    private static final String DISABLED_ACTION = "—";

    private final User currentUser;
    private final UserService userService = new UserService();
    private final JobService jobService = new JobService();
    private final ApplicationService appService = new ApplicationService();
    private final TAProfileService profileService = new TAProfileService();
    private final CVService cvService = new CVService();

    private JTable appTable;
    private DefaultTableModel tableModel;
    /** All applications for this MO's jobs (unfiltered). */
    private List<Application> allMoApplications = new ArrayList<>();
    /** Rows currently shown after course + status filters. */
    private List<Application> filteredApplications = new ArrayList<>();
    private List<Job> myJobs = new ArrayList<>();

    private JComboBox<String> courseCombo;
    private JComboBox<String> statusCombo;
    private JLabel statusLabel;
    private Timer refreshTimer;

    /** Parallel to {@link #courseCombo} items: module code for the selected row. */
    private final List<String> courseComboModuleCodes = new ArrayList<>();

    public MOApplicantReviewPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(24, 32, 24, 32));

        initNorth();
        initTable();
        initSouth();

        loadApplications();

        refreshTimer = new Timer(10000, e -> loadApplications());
        refreshTimer.start();
    }

    private void initNorth() {
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        north.setOpaque(false);

        north.add(new JLabel("Course:"));
        courseCombo = new JComboBox<>();
        courseCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        courseCombo.addActionListener(e -> applyFiltersAndRefreshTable());
        north.add(courseCombo);

        north.add(Box.createHorizontalStrut(16));
        north.add(new JLabel("Application status:"));
        statusCombo = new JComboBox<>(new String[]{
                ALL_STATUSES, ST_PENDING, ST_SHORTLIST, ST_REJECTED, ST_OFFER_SENT, ST_HIRED
        });
        statusCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusCombo.addActionListener(e -> applyFiltersAndRefreshTable());
        north.add(statusCombo);

        north.add(Box.createHorizontalStrut(24));
        JButton exportCsvBtn = new JButton("Export CSV");
        exportCsvBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        MoUiStyles.applyTextButton(exportCsvBtn);
        exportCsvBtn.addActionListener(e -> exportFilteredApplicationsToCsv());
        north.add(exportCsvBtn);

        north.add(Box.createHorizontalStrut(12));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        MoUiStyles.applyTextButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadApplications());
        north.add(refreshBtn);

        add(north, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {
                "TA name", "Email", "Course", "Application status",
                "Profile", "CV", "Accept (offer)", "Reject", "Shortlist"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column < 4) {
                    return false;
                }
                Object v = getValueAt(row, column);
                if (v == null) {
                    return false;
                }
                String s = String.valueOf(v).trim();
                return !s.isEmpty() && !DISABLED_ACTION.equals(s);
            }
        };
        appTable = new JTable(tableModel);
        appTable.setRowHeight(40);
        appTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        appTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        ActionButtonRenderer renderer = new ActionButtonRenderer();
        ActionButtonEditor editor = new ActionButtonEditor();
        for (int c = 4; c < columns.length; c++) {
            appTable.getColumnModel().getColumn(c).setCellRenderer(renderer);
            appTable.getColumnModel().getColumn(c).setCellEditor(editor);
        }

        TableScrollUtil.ColumnSpec[] appCols = {
                TableScrollUtil.ColumnSpec.flex(88, 120),
                TableScrollUtil.ColumnSpec.flex(120, 220),
                TableScrollUtil.ColumnSpec.flex(140, 320),
                TableScrollUtil.ColumnSpec.flex(88, 140),
                TableScrollUtil.ColumnSpec.fixed(76),
                TableScrollUtil.ColumnSpec.fixed(58),
                TableScrollUtil.ColumnSpec.fixed(118),
                TableScrollUtil.ColumnSpec.fixed(78),
                TableScrollUtil.ColumnSpec.fixed(88),
        };

        JScrollPane appScroll = TableScrollUtil.wrapTable(appTable);
        TableScrollUtil.installResponsiveColumns(appTable, appScroll, appCols);
        add(appScroll, BorderLayout.CENTER);
    }

    private void initSouth() {
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusLabel.setBorder(new EmptyBorder(8, 4, 0, 4));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadApplications() {
        myJobs = jobService.listAll().stream()
                .filter(j -> j.getMoUserId() != null && j.getMoUserId().equals(currentUser.getUserId()))
                .collect(Collectors.toList());

        Set<Long> myJobIds = myJobs.stream().map(Job::getJobId).filter(Objects::nonNull).collect(Collectors.toSet());

        List<Application> allApps = jobService.listAllApplications();
        allMoApplications = allApps.stream()
                .filter(app -> myJobIds.contains(app.getJobId()))
                .collect(Collectors.toList());

        rebuildCourseCombo();
        applyFiltersAndRefreshTable();
    }

    private void rebuildCourseCombo() {
        String previousCode = getSelectedModuleCodeFromCombo();
        courseCombo.removeAllItems();
        courseComboModuleCodes.clear();

        Map<String, String> codeToTitle = new LinkedHashMap<>();
        myJobs.stream()
                .filter(j -> j.getModuleCode() != null && !j.getModuleCode().isBlank())
                .sorted(Comparator.comparing(j -> j.getModuleCode().trim(), String.CASE_INSENSITIVE_ORDER))
                .forEach(j -> {
                    String code = j.getModuleCode().trim();
                    codeToTitle.putIfAbsent(code,
                            j.getTitle() != null && !j.getTitle().isBlank() ? j.getTitle().trim() : "—");
                });

        for (Map.Entry<String, String> e : codeToTitle.entrySet()) {
            courseComboModuleCodes.add(e.getKey());
            courseCombo.addItem(e.getKey() + " — " + e.getValue());
        }

        if (courseComboModuleCodes.isEmpty()) {
            return;
        }
        if (previousCode != null) {
            int idx = courseComboModuleCodes.indexOf(previousCode);
            if (idx >= 0) {
                courseCombo.setSelectedIndex(idx);
                return;
            }
        }
        courseCombo.setSelectedIndex(0);
    }

    private String getSelectedModuleCodeFromCombo() {
        int i = courseCombo.getSelectedIndex();
        if (i < 0 || i >= courseComboModuleCodes.size()) {
            return null;
        }
        return courseComboModuleCodes.get(i);
    }

    private void applyFiltersAndRefreshTable() {
        if (courseCombo.getItemCount() == 0) {
            filteredApplications = new ArrayList<>();
            tableModel.setRowCount(0);
            statusLabel.setText("No courses (jobs) for your account. Post a job first.");
            return;
        }
        String c = getSelectedModuleCodeFromCombo();
        if (c == null && !courseComboModuleCodes.isEmpty()) {
            courseCombo.setSelectedIndex(0);
            c = getSelectedModuleCodeFromCombo();
        }
        if (c == null) {
            filteredApplications = new ArrayList<>();
            tableModel.setRowCount(0);
            statusLabel.setText("Select a course.");
            return;
        }
        final String courseSel = c;
        String statusRaw = (String) statusCombo.getSelectedItem();
        final String statusSel = (statusRaw == null) ? ALL_STATUSES : statusRaw;

        Set<Long> jobIdsForCourse = myJobs.stream()
                .filter(j -> courseSel.equals(j.getModuleCode()))
                .map(Job::getJobId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        filteredApplications = allMoApplications.stream()
                .filter(app -> jobIdsForCourse.contains(app.getJobId()))
                .filter(app -> matchesStatusFilter(app, statusSel))
                .sorted(Comparator.comparing(Application::getAppliedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (Application app : filteredApplications) {
            User taUser = userService.getUserById(app.getTaUserId());
            String email = taUser != null ? taUser.getEmail() : ("TA #" + app.getTaUserId());
            String name = displayTaName(taUser);
            String courseDisplay = courseDisplayForApplication(app);
            String statusText = ApplicationStatus.getDisplayText(app.getStatus());
            String acceptLabel = canSendOffer(app) ? "Accept" : DISABLED_ACTION;
            String rejectLabel = canReject(app) ? "Reject" : DISABLED_ACTION;
            String shortlistLabel = canShortlist(app) ? "Shortlist" : DISABLED_ACTION;
            tableModel.addRow(new Object[]{
                    name, email, courseDisplay, statusText,
                    "View profile", "View CV", acceptLabel, rejectLabel, shortlistLabel
            });
        }

        statusLabel.setText(String.format(
                "%d application(s) shown (of %d for your jobs) | Courses: %d | Last refresh: %s",
                filteredApplications.size(),
                allMoApplications.size(),
                myJobs.stream().map(Job::getModuleCode).filter(Objects::nonNull).distinct().count(),
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))));
    }

    private void exportFilteredApplicationsToCsv() {
        if (courseCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No course to export (no jobs for your account).",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (filteredApplications.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No applications match the current filters.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String courseCode = getSelectedModuleCodeFromCombo();
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("applicants_"
                + (courseCode != null ? courseCode.replaceAll("[^a-zA-Z0-9_-]", "_") : "export")
                + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = fc.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".csv")) {
            target = new File(target.getParentFile(), target.getName() + ".csv");
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(csvEscape("TA name")).append(',')
                    .append(csvEscape("Email")).append(',')
                    .append(csvEscape("Course")).append(',')
                    .append(csvEscape("Application status")).append(',')
                    .append(csvEscape("Application ID")).append(',')
                    .append(csvEscape("Job ID")).append(',')
                    .append(csvEscape("Applied at")).append(',')
                    .append(csvEscape("Statement")).append('\n');
            for (Application app : filteredApplications) {
                User taUser = userService.getUserById(app.getTaUserId());
                String email = taUser != null ? taUser.getEmail() : ("TA #" + app.getTaUserId());
                String name = displayTaName(taUser);
                String courseDisplay = courseDisplayForApplication(app);
                String statusText = ApplicationStatus.getDisplayText(app.getStatus());
                String applied = app.getAppliedAt() != null ? app.getAppliedAt().toString() : "";
                sb.append(csvEscape(name)).append(',')
                        .append(csvEscape(email)).append(',')
                        .append(csvEscape(courseDisplay)).append(',')
                        .append(csvEscape(statusText)).append(',')
                        .append(app.getApplicationId() != null ? app.getApplicationId() : "").append(',')
                        .append(app.getJobId() != null ? app.getJobId() : "").append(',')
                        .append(csvEscape(applied)).append(',')
                        .append(csvEscape(app.getStatement())).append('\n');
            }
            Files.writeString(target.toPath(), sb.toString(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Exported to:\n" + target.getAbsolutePath(),
                    "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private boolean matchesStatusFilter(Application app, String selected) {
        if (ALL_STATUSES.equals(selected)) {
            return true;
        }
        String st = app.getStatus();
        if (ST_PENDING.equals(selected)) {
            return ApplicationStatus.SUBMITTED.equals(st);
        }
        if (ST_SHORTLIST.equals(selected)) {
            return ApplicationStatus.WAITLISTED.equals(st);
        }
        if (ST_REJECTED.equals(selected)) {
            return ApplicationStatus.REJECTED.equals(st);
        }
        if (ST_OFFER_SENT.equals(selected)) {
            return ApplicationStatus.OFFER_SENT.equals(st);
        }
        if (ST_HIRED.equals(selected)) {
            return ApplicationStatus.HIRED.equals(st);
        }
        return true;
    }

    private String courseDisplayForApplication(Application app) {
        Job j = findJob(app.getJobId());
        if (j == null) {
            return "—";
        }
        String code = j.getModuleCode() != null && !j.getModuleCode().isBlank()
                ? j.getModuleCode().trim() : "—";
        String title = j.getTitle() != null && !j.getTitle().isBlank()
                ? j.getTitle().trim() : "—";
        return code + " — " + title;
    }

    private String displayTaName(User taUser) {
        if (taUser == null) {
            return "—";
        }
        TAProfile profile = profileService.getProfileByTaId(taUser.getUserId());
        if (profile != null) {
            String full = profile.getFullName();
            if (full != null && !full.isBlank()) {
                return full.trim();
            }
        }
        String email = taUser.getEmail();
        if (email == null) {
            return "—";
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private void showTaProfileDialog(Application app) {
        User user = userService.getUserById(app.getTaUserId());
        if (user == null) {
            JOptionPane.showMessageDialog(this, "TA user not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TAProfile profile = profileService.getProfileByTaId(user.getUserId());
        if (profile == null) {
            JOptionPane.showMessageDialog(this,
                    "Profile not completed yet.\nEmail: " + user.getEmail(),
                    "TA Profile", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        TaProfileViewer.show(this, user, profile, app.getStatement());
    }

    private void viewTaCv(Application app) {
        User user = userService.getUserById(app.getTaUserId());
        if (user == null) {
            JOptionPane.showMessageDialog(this, "TA user not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        cvService.refreshCVs(user.getUserId());
        CVInfo defaultCV = cvService.getDefaultCV(user.getUserId());
        if (defaultCV == null) {
            JOptionPane.showMessageDialog(this,
                    "No CV uploaded yet.\nEmail: " + user.getEmail(),
                    "CV Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            byte[] fileData = cvService.downloadCV(user.getUserId(), defaultCV.getCvId());
            if (fileData == null || fileData.length == 0) {
                JOptionPane.showMessageDialog(this, "Failed to read CV file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String extension = defaultCV.getOriginalFileName() != null && defaultCV.getOriginalFileName().contains(".")
                    ? defaultCV.getOriginalFileName().substring(defaultCV.getOriginalFileName().lastIndexOf('.') + 1)
                    : "pdf";
            java.io.File tempFile = java.io.File.createTempFile("cv_", "." + extension);
            tempFile.deleteOnExit();
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(fileData);
            }
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                JOptionPane.showMessageDialog(this, "CV saved to: " + tempFile.getAbsolutePath(),
                        "CV File", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to open CV: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Job findJob(Long jobId) {
        if (jobId == null) {
            return null;
        }
        return myJobs.stream().filter(j -> jobId.equals(j.getJobId())).findFirst().orElse(null);
    }

    private static boolean canSendOffer(Application app) {
        String st = app.getStatus();
        return ApplicationStatus.SUBMITTED.equals(st) || ApplicationStatus.WAITLISTED.equals(st);
    }

    private static boolean canReject(Application app) {
        if (ApplicationStatus.isTerminal(app.getStatus())) {
            return false;
        }
        String st = app.getStatus();
        return ApplicationStatus.SUBMITTED.equals(st) || ApplicationStatus.WAITLISTED.equals(st);
    }

    private static boolean canShortlist(Application app) {
        return ApplicationStatus.SUBMITTED.equals(app.getStatus());
    }

    private void acceptApplication(Application app) {
        if (!ApplicationStatus.SUBMITTED.equals(app.getStatus())
                && !ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Offers can only be sent for applications in Pending review or Shortlisted status.",
                    "Cannot send offer", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Job job = findJob(app.getJobId());
        if (job == null) {
            JOptionPane.showMessageDialog(this, "Job not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            appService.sendOffer(app.getApplicationId(), job.getWeeklyHours(), 7);
            loadApplications();
            JOptionPane.showMessageDialog(this,
                    "Offer sent. Waiting for the TA to respond.",
                    "Offer sent", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectApplication(Application app) {
        if (ApplicationStatus.isTerminal(app.getStatus())) {
            JOptionPane.showMessageDialog(this, "This application is already in a final state.",
                    "No change", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!ApplicationStatus.SUBMITTED.equals(app.getStatus())
                && !ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Reject is only available for Pending review or Shortlisted applications.",
                    "Cannot reject", JOptionPane.WARNING_MESSAGE);
            return;
        }
        app.setStatus(ApplicationStatus.REJECTED);
        jobService.updateApplication(app);
        loadApplications();
        JOptionPane.showMessageDialog(this, "Application marked as rejected.", "Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void shortlistApplication(Application app) {
        if (!ApplicationStatus.SUBMITTED.equals(app.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Shortlist is only available for applications that are still pending review.",
                    "Cannot shortlist", JOptionPane.WARNING_MESSAGE);
            return;
        }
        app.setStatus(ApplicationStatus.WAITLISTED);
        jobService.updateApplication(app);
        loadApplications();
        JOptionPane.showMessageDialog(this, "Candidate moved to shortlist.", "Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private class ActionButtonRenderer extends JButton implements TableCellRenderer {
        ActionButtonRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value == null ? "" : String.valueOf(value).trim();
            boolean disabled = text.isEmpty() || DISABLED_ACTION.equals(text);
            setText(disabled ? DISABLED_ACTION : text);
            TableListActionStyle.applyToButton(this, getText());
            return this;
        }
    }

    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
        private int row;
        private int col;

        ActionButtonEditor() {
            button.addActionListener(evt -> handleButtonClick());
        }

        private void handleButtonClick() {
            if (row < 0 || row >= filteredApplications.size()) {
                fireEditingStopped();
                return;
            }
            Object cell = tableModel.getValueAt(row, col);
            if (cell == null || DISABLED_ACTION.equals(String.valueOf(cell).trim())) {
                fireEditingStopped();
                return;
            }
            Application app = filteredApplications.get(row);
            switch (col) {
                case 4 -> showTaProfileDialog(app);
                case 5 -> viewTaCv(app);
                case 6 -> acceptApplication(app);
                case 7 -> rejectApplication(app);
                case 8 -> shortlistApplication(app);
                default -> { }
            }
            fireEditingStopped();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                   boolean isSelected, int row, int column) {
            this.row = row;
            this.col = column;
            String text = value == null ? "" : String.valueOf(value).trim();
            button.setText(text.isEmpty() ? DISABLED_ACTION : text);
            TableListActionStyle.applyToButton(button, button.getText());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }
}
