package mo.ui;

import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.User;
import common.service.MOJobService;
import common.service.MOOfferService;
import common.service.UserService;
import common.util.CsvExportUtil;
import ta.dao.TAProfileDAO;
import ta.entity.TAApplication;
import ta.entity.TAProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MO-008.1: Displays every TA who accepted an offer for the current MO's modules.
 *
 * Data sources (all file-backed, no database):
 *   ta_applications.json  — find applications with status HIRED belonging to this MO's jobs
 *   mo_offers.json        — find the matching ACCEPTED offer for the hired-at timestamp
 *   ta_profiles.json      — TA name, major, year, phone
 *   users.json            — TA email (fallback when profile is incomplete)
 *   mo_jobs.json          — module code + job title for the "Course" column
 */
public class MOHiredTAsPanel extends JPanel {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final User currentUser;
    private final MOJobService     jobService     = new MOJobService();
    private final MOOfferService   offerService   = new MOOfferService();
    private final UserService      userService    = new UserService();
    private final TAProfileDAO     profileDAO     = new TAProfileDAO();

    private JTable             table;
    private DefaultTableModel  tableModel;
    private JLabel             countLabel;
    private List<TAApplication> hiredApps;   // parallel to table rows

    public MOHiredTAsPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(),  BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadData();
    }

    // -----------------------------------------------------------------------
    // UI construction
    // -----------------------------------------------------------------------

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Hired TAs");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(30, 35, 45));

        countLabel = new JLabel("Loading…");
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        countLabel.setForeground(new Color(100, 116, 139));

        JPanel labels = new JPanel();
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.setOpaque(false);
        labels.add(title);
        labels.add(Box.createRigidArea(new Dimension(0, 4)));
        labels.add(countLabel);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> loadData());

        panel.add(labels,     BorderLayout.WEST);
        panel.add(refreshBtn, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane createTable() {
        String[] cols = {"TA Name", "Major", "Year", "Phone", "Email", "Course", "Hired At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setGridColor(new Color(226, 232, 240));
        table.setShowGrid(true);

        int[] widths = {150, 110, 75, 130, 195, 200, 130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        return new JScrollPane(table);
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setOpaque(false);

        JButton viewBtn = new JButton("View Full Profile");
        viewBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        viewBtn.addActionListener(e -> viewSelectedProfile());

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        exportBtn.setBackground(new Color(16, 185, 129));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setOpaque(true);
        exportBtn.setContentAreaFilled(true);
        exportBtn.setFocusPainted(false);
        exportBtn.setToolTipText("Export the Hired TAs table to a CSV file (Excel-compatible)");
        exportBtn.addActionListener(e -> exportToCsv());

        panel.add(viewBtn);
        panel.add(exportBtn);
        return panel;
    }

    // -----------------------------------------------------------------------
    // Data loading
    // -----------------------------------------------------------------------

    private void loadData() {
        tableModel.setRowCount(0);
        hiredApps = new ArrayList<>();

        // 1. Load all jobs once; build a map for O(1) look-up inside the loop
        Map<Long, MOJob> jobMap = jobService.listAll().stream()
                .collect(Collectors.toMap(MOJob::getJobId, j -> j, (a, b) -> a));

        List<Long> myJobIds = jobMap.values().stream()
                .filter(j -> currentUser.getUserId().equals(j.getMoUserId()))
                .map(MOJob::getJobId)
                .collect(Collectors.toList());

        // 2. Find applications with status HIRED for those jobs
        List<TAApplication> hired = jobService.listAllApplications().stream()
                .filter(app -> myJobIds.contains(app.getJobId())
                        && "HIRED".equals(app.getStatus()))
                .collect(Collectors.toList());

        // 3. Load all offers once for O(n) look-up
        List<MOOffer> allOffers = offerService.listAll();

        for (TAApplication app : hired) {
            // --- job label (O(1) map look-up, no extra listAll() call) ---
            MOJob job = jobMap.get(app.getJobId());
            String courseLabel = job != null
                    ? job.getModuleCode() + " \u2013 " + job.getTitle()
                    : "Job #" + app.getJobId();

            // --- profile (two-step lookup: by taId first, then by email) ---
            TAProfile profile = profileDAO.findByTaId(app.getTaUserId());
            if (profile == null) {
                User u = userService.getUserById(app.getTaUserId());
                if (u != null) profile = profileDAO.findByEmail(u.getEmail());
            }

            String taName = resolveName(profile, app.getTaUserId());
            String major  = blankToNA(profile != null ? profile.getMajor() : null);
            String year   = profile != null && profile.getCurrentYear() != null
                    ? profile.getCurrentYearDisplay() : "N/A";
            String phone  = blankToNA(profile != null ? profile.getPhone() : null);
            String email  = resolveEmail(profile, app.getTaUserId());

            // --- hired-at time: respondedAt of the matching ACCEPTED offer ---
            String hiredAt = allOffers.stream()
                    .filter(o -> app.getApplicationId().equals(o.getApplicationId())
                            && "ACCEPTED".equals(o.getStatus()))
                    .findFirst()
                    .map(o -> o.getRespondedAt() != null
                            ? o.getRespondedAt().format(DISPLAY_FMT) : "N/A")
                    .orElse("N/A");

            tableModel.addRow(new Object[]{taName, major, year, phone, email, courseLabel, hiredAt});
            hiredApps.add(app);
        }

        countLabel.setText(hiredApps.size() + " TA(s) hired across your module(s)");
    }

    // -----------------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------------

    /**
     * MO-008.2: Export the current hired-TA table to a CSV file.
     *
     * Export fields: Course Name | TA Name | Student ID / Email | Phone | Hired Date
     *
     * A JFileChooser lets the MO choose the save directory and filename.
     * The file is written UTF-8 with BOM so Excel opens it correctly.
     */
    private void exportToCsv() {
        if (hiredApps == null || hiredApps.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "There are no hired TAs to export.",
                    "Nothing to Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Default filename: hired_tas_YYYY-MM-DD.csv
        String defaultName = "hired_tas_" + LocalDate.now() + ".csv";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Hired TAs as CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        chooser.setSelectedFile(new File(defaultName));

        // Pre-navigate to the exports/ directory if it exists, otherwise stay in project root
        File exportsDir = new File("exports");
        if (exportsDir.isDirectory()) {
            chooser.setCurrentDirectory(exportsDir);
        } else {
            chooser.setCurrentDirectory(new File("."));
        }

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File chosen = chooser.getSelectedFile();
        if (!chosen.getName().toLowerCase().endsWith(".csv")) {
            chosen = new File(chosen.getAbsolutePath() + ".csv");
        }

        // Build CSV headers and rows
        String[] headers = {
            "Course Name", "TA Name", "Student ID / Email", "Phone", "Hired Date"
        };

        List<String[]> rows = new ArrayList<>();
        // Load jobs and offers once before the loop to avoid O(n²) file reads
        Map<Long, MOJob> jobMap = jobService.listAll().stream()
                .collect(Collectors.toMap(MOJob::getJobId, j -> j, (a, b) -> a));
        List<MOOffer> allOffers = offerService.listAll();

        for (TAApplication app : hiredApps) {
            // Course — O(1) map look-up instead of calling listAll() per iteration
            MOJob job = jobMap.get(app.getJobId());
            String course = job != null
                    ? job.getModuleCode() + " - " + job.getTitle()
                    : "Job #" + app.getJobId();

            // Profile
            TAProfile profile = profileDAO.findByTaId(app.getTaUserId());
            if (profile == null) {
                User u = userService.getUserById(app.getTaUserId());
                if (u != null) profile = profileDAO.findByEmail(u.getEmail());
            }

            String taName   = resolveName(profile, app.getTaUserId());
            String studentId = (profile != null && notBlank(profile.getStudentId()))
                    ? profile.getStudentId()
                    : resolveEmail(profile, app.getTaUserId());
            String phone    = blankToNA(profile != null ? profile.getPhone() : null);

            // Hired date from ACCEPTED offer
            String hiredDate = allOffers.stream()
                    .filter(o -> app.getApplicationId().equals(o.getApplicationId())
                            && "ACCEPTED".equals(o.getStatus()))
                    .findFirst()
                    .map(o -> o.getRespondedAt() != null
                            ? o.getRespondedAt().toLocalDate().toString() : "N/A")
                    .orElse("N/A");

            rows.add(new String[]{course, taName, studentId, phone, hiredDate});
        }

        try {
            Path saved = CsvExportUtil.exportRows(chosen.toPath(), headers, rows);
            JOptionPane.showMessageDialog(this,
                    "<html>Export successful!<br><b>" + rows.size() + "</b> record(s) written to:<br>"
                            + "<tt>" + saved + "</tt></html>",
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedProfile() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a TA row first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        TAApplication app = hiredApps.get(row);
        TAProfile profile = profileDAO.findByTaId(app.getTaUserId());
        if (profile == null) {
            User u = userService.getUserById(app.getTaUserId());
            if (u != null) profile = profileDAO.findByEmail(u.getEmail());
        }

        // --- find matching offer for additional details ---
        MOOffer offer = offerService.listAll().stream()
                .filter(o -> app.getApplicationId().equals(o.getApplicationId())
                        && "ACCEPTED".equals(o.getStatus()))
                .findFirst().orElse(null);

        showProfileDialog(profile, app, offer);
    }

    private void showProfileDialog(TAProfile p, TAApplication app, MOOffer offer) {
        StringBuilder sb = new StringBuilder();

        if (p == null) {
            sb.append("Profile not yet completed.\n");
            sb.append("TA User ID : ").append(app.getTaUserId()).append("\n");
        } else {
            appendLine(sb, "Name",         resolveName(p, app.getTaUserId()));
            if (p.getChineseName() != null && !p.getChineseName().isBlank())
                appendLine(sb, "Chinese Name", p.getChineseName());
            appendLine(sb, "Email",        resolveEmail(p, app.getTaUserId()));
            appendLine(sb, "Phone",        blankToNA(p.getPhone()));
            appendLine(sb, "Student ID",   blankToNA(p.getStudentId()));
            appendLine(sb, "Major",        blankToNA(p.getMajor()));
            appendLine(sb, "School",       blankToNA(p.getSchool()));
            appendLine(sb, "Student Type", p.getStudentType() != null
                    ? p.getStudentType().toString() : "N/A");
            appendLine(sb, "Year",         p.getCurrentYear() != null
                    ? p.getCurrentYearDisplay() : "N/A");
            appendLine(sb, "Campus",       p.getCampus() != null ? p.getCampus().toString() : "N/A");
            if (p.getPreviousExperience() != null && !p.getPreviousExperience().isBlank())
                appendLine(sb, "Experience",   p.getPreviousExperience());
            if (p.getSkillTags() != null && !p.getSkillTags().isEmpty())
                appendLine(sb, "Skills",       String.join(", ", p.getSkillTags()));
            if (p.getAvailableWorkingHours() > 0)
                appendLine(sb, "Avail. Hours/Week", p.getAvailableWorkingHours() + " h");
        }

        sb.append("\n");
        appendLine(sb, "Application ID", String.valueOf(app.getApplicationId()));
        if (offer != null) {
            appendLine(sb, "Offered Hours",
                    offer.getOfferedHours() + " h/week");
            appendLine(sb, "Hired At",
                    offer.getRespondedAt() != null
                            ? offer.getRespondedAt().format(DISPLAY_FMT) : "N/A");
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 340));

        JOptionPane.showMessageDialog(this, scroll,
                "Hired TA — " + resolveName(p, app.getTaUserId()),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String resolveName(TAProfile p, Long taUserId) {
        if (p != null) {
            String f = p.getForename(), s = p.getSurname();
            if (notBlank(f) && notBlank(s)) return f + " " + s;
            if (notBlank(p.getChineseName())) return p.getChineseName();
            if (notBlank(p.getEmail()))       return p.getEmail();
        }
        User u = userService.getUserById(taUserId);
        return u != null ? u.getEmail() : "TA #" + taUserId;
    }

    private String resolveEmail(TAProfile p, Long taUserId) {
        if (p != null && notBlank(p.getEmail())) return p.getEmail();
        User u = userService.getUserById(taUserId);
        return u != null ? u.getEmail() : "N/A";
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String blankToNA(String s) {
        return (s != null && !s.isBlank()) ? s : "N/A";
    }

    private static void appendLine(StringBuilder sb, String label, String value) {
        sb.append(String.format("%-17s: %s%n", label, value));
    }
}
