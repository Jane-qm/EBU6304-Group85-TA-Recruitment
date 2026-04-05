package ta.ui;

import common.entity.MOJob;
import common.entity.TA;
import common.service.MOJobService;
import common.service.TAApplicationService;
import common.ui.BaseFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * Iteration 2 shell: browse published positions with simple filters.
 * Teammates: add time-based filter using {@link MOJob#getApplicationDeadline()}.
 */
public class TaJobDirectoryFrame extends BaseFrame {

    private final TA ta;
    private final MOJobService jobService = new MOJobService();
    private final TAApplicationService applicationService = new TAApplicationService();

    private JTextField moduleFilter;
    private JTextField skillFilter;
    private DefaultTableModel tableModel;

    public TaJobDirectoryFrame(TA ta) {
        super("Published positions", 900, 420);
        this.ta = ta;
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(245, 247, 251));

        JLabel hint = new JLabel("<html>Active applications: "
                + applicationService.countActiveApplications(ta.getUserId())
                + " / " + TAApplicationService.MAX_CONCURRENT_APPLICATIONS + "</html>");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        moduleFilter = new JTextField(12);
        moduleFilter.setToolTipText("Module code prefix");
        skillFilter = new JTextField(12);
        skillFilter.setToolTipText("Keyword in required skills");
        JButton applyFilter = new JButton("Apply filters");
        applyFilter.addActionListener(e -> refreshTable());
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            moduleFilter.setText("");
            skillFilter.setText("");
            refreshTable();
        });
        filters.add(new JLabel("Module:"));
        filters.add(moduleFilter);
        filters.add(new JLabel("Skill:"));
        filters.add(skillFilter);
        filters.add(applyFilter);
        filters.add(clearBtn);

        String[] columns = {"Job ID", "Module", "Title", "Hours", "MO", "Deadline", "Quota"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        refreshTable();
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(860, 220));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        south.setOpaque(false);
        JButton detailBtn = new JButton("Open detail / apply…");
        detailBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showWarning("Select a job row first.");
                return;
            }
            long jobId = ((Number) tableModel.getValueAt(row, 0)).longValue();
            MOJob job = null;
            for (MOJob j : jobService.listPublishedJobs()) {
                if (j.getJobId() != null && j.getJobId() == jobId) {
                    job = j;
                    break;
                }
            }
            if (job == null) {
                showWarning("Job not found.");
                return;
            }
            new TaJobDetailFrame(ta, job).setVisible(true);
        });
        south.add(detailBtn);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        JPanel northStack = new JPanel();
        northStack.setLayout(new javax.swing.BoxLayout(northStack, javax.swing.BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.add(hint);
        northStack.add(javax.swing.Box.createVerticalStrut(6));
        northStack.add(filters);
        north.add(northStack, BorderLayout.NORTH);

        root.add(north, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String mod = moduleFilter != null ? moduleFilter.getText() : "";
        String sk = skillFilter != null ? skillFilter.getText() : "";
        List<MOJob> jobs = jobService.filterPublishedJobs(mod, sk);
        for (MOJob job : jobs) {
            String mo = job.getMoDisplayName() != null && !job.getMoDisplayName().isBlank()
                    ? job.getMoDisplayName()
                    : String.valueOf(job.getMoUserId());
            String deadline = job.getApplicationDeadline() == null ? "—"
                    : job.getApplicationDeadline().toLocalDate().toString();
            String quota = job.getRecruitmentQuota() == null ? "—" : String.valueOf(job.getRecruitmentQuota());
            tableModel.addRow(new Object[]{
                    job.getJobId(),
                    job.getModuleCode(),
                    job.getTitle(),
                    job.getWeeklyHours(),
                    mo,
                    deadline,
                    quota
            });
        }
    }
}
