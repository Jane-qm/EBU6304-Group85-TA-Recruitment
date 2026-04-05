package ta.ui;

import common.entity.MOJob;
import common.entity.TA;
import common.service.TAApplicationService;
import common.ui.BaseFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Iteration 2 shell: position detail (requirements, MO info, deadline, quota).
 * Teammates: enrich MO identity from {@link common.service.UserService}, add deadline validation.
 */
public class TaJobDetailFrame extends BaseFrame {

    private static final DateTimeFormatter DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final TA ta;
    private final MOJob job;
    private final TAApplicationService applicationService = new TAApplicationService();

    public TaJobDetailFrame(TA ta, MOJob job) {
        super("Position — " + (job.getModuleCode() != null ? job.getModuleCode() : ""), 560, 520);
        this.ta = ta;
        this.job = job;
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(245, 247, 251));

        JTextArea body = new JTextArea(buildDetailText());
        body.setEditable(false);
        body.setFont(new Font("Monospaced", Font.PLAIN, 13));
        body.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JTextField statement = new JTextField("Statement for this application…", 28);
        JButton applyBtn = new JButton("Apply for this position");
        applyBtn.addActionListener(e -> {
            try {
                TAApplicationService svc = applicationService;
                svc.submitApplication(ta.getUserId(), job.getJobId(), statement.getText().trim());
                showInfo("Application submitted (Pending Review).");
                dispose();
            } catch (Exception ex) {
                showWarning(ex.getMessage());
            }
        });

        actions.add(statement);
        actions.add(applyBtn);

        root.add(new JScrollPane(body), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private String buildDetailText() {
        String skills = job.getRequiredSkills() == null ? ""
                : job.getRequiredSkills().stream().filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
        String deadline = job.getApplicationDeadline() == null
                ? "(not set)"
                : DT.format(job.getApplicationDeadline());
        String quota = job.getRecruitmentQuota() == null ? "(not set)" : String.valueOf(job.getRecruitmentQuota());
        String mo = job.getMoDisplayName() == null || job.getMoDisplayName().isBlank()
                ? "MO user ID: " + job.getMoUserId()
                : job.getMoDisplayName();

        return "Module: " + nullSafe(job.getModuleCode())
                + "\nTitle: " + nullSafe(job.getTitle())
                + "\nStatus: " + nullSafe(job.getStatus())
                + "\nWeekly hours: " + job.getWeeklyHours()
                + "\n\nMO: " + mo
                + "\n\nRequired skills: " + (skills.isEmpty() ? "(none listed)" : skills)
                + "\n\nApplication deadline: " + deadline
                + "\nRecruitment quota: " + quota
                + "\n\nDescription:\n" + nullSafe(job.getDescription());
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
