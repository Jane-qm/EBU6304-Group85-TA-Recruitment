package mo.ui;

import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.User;
import common.service.MOJobService;
import common.service.MOOfferService;
import common.service.UserService;
import ta.entity.TAApplication;
import ta.service.TAApplicationService;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.stream.Collectors;

public class MOApplicantReviewPanel extends JPanel {
    private final User currentUser;
    private final UserService userService = new UserService();
    private final MOJobService jobService = new MOJobService();
    private final MOOfferService offerService = new MOOfferService();
    private final TAApplicationService applicationService = new TAApplicationService();

    private JTable appTable;
    private DefaultTableModel tableModel;
    private List<TAApplication> currentApplications;

    public MOApplicantReviewPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initHeader();
        initTable();
        initActionButtons();
        loadApplications();
    }

    private void initHeader() {
        JLabel titleLabel = new JLabel("Review Applicants & Send Offers");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"App ID", "Job ID", "TA User ID", "Statement", "Status", "Applied At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appTable = new JTable(tableModel);
        appTable.setRowHeight(30);
        appTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        appTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        add(new JScrollPane(appTable), BorderLayout.CENTER);
    }

    private void initActionButtons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);

        JButton viewCvBtn = new JButton("View Profile/CV");
        viewCvBtn.addActionListener(e -> viewTAProfile());

        JButton rejectBtn = new JButton("Reject");
        rejectBtn.addActionListener(e -> processApplications("REJECTED"));

        JButton waitlistBtn = new JButton("Waitlist");
        waitlistBtn.addActionListener(e -> processApplications("WAITLISTED"));

        JButton acceptBtn = new JButton("Accept");
        acceptBtn.addActionListener(e -> processApplications("ACCEPTED"));

        JButton offerBtn = new JButton("Batch Send Offers");
        offerBtn.setBackground(new Color(16, 185, 129));
        offerBtn.setForeground(Color.WHITE);
        offerBtn.addActionListener(e -> sendOffer());

        btnPanel.add(viewCvBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(rejectBtn);
        btnPanel.add(waitlistBtn);
        btnPanel.add(acceptBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(offerBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadApplications() {
        tableModel.setRowCount(0);

        List<Long> myJobIds = jobService.listAll().stream()
                .filter(j -> j.getMoUserId().equals(currentUser.getUserId()))
                .map(MOJob::getJobId)
                .collect(Collectors.toList());

        currentApplications = jobService.listAllApplications().stream()
                .filter(app -> myJobIds.contains(app.getJobId()))
                .collect(Collectors.toList());

        for (TAApplication app : currentApplications) {
            User taUser = userService.listAll().stream()
                    .filter(u -> u.getUserId().equals(app.getTaUserId()))
                    .findFirst()
                    .orElse(null);

            MOJob job = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(app.getJobId()))
                    .findFirst()
                    .orElse(null);

            tableModel.addRow(new Object[]{
                    app.getApplicationId(),
                    job != null ? job.getModuleCode() : app.getJobId(),
                    taUser != null ? taUser.getEmail() : "Unknown",
                    app.getStatement(),
                    app.getStatus(),
                    app.getAppliedAt() != null ? app.getAppliedAt().toLocalDate() : "N/A"
            });
        }
    }

    private void viewTAProfile() {
        int selectedRow = appTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an applicant to view their profile.");
            return;
        }

        TAApplication app = currentApplications.get(selectedRow);
        JOptionPane.showMessageDialog(
                this,
                "Applicant Statement:\n\n" + app.getStatement(),
                "TA Profile Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void processApplications(String newStatus) {
        int[] selectedRows = appTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one applicant.");
            return;
        }

        try {
            for (int row : selectedRows) {
                TAApplication app = currentApplications.get(row);
                if ("REJECTED".equals(newStatus)) {
                    applicationService.rejectApplication(app.getApplicationId());
                } else if ("WAITLISTED".equals(newStatus)) {
                    applicationService.markAsWaitlisted(app.getApplicationId());
                } else if ("ACCEPTED".equals(newStatus)) {
                    applicationService.markAsHired(app.getApplicationId());
                }
            }

            loadApplications();
            JOptionPane.showMessageDialog(
                    this,
                    "Successfully updated " + selectedRows.length + " applicant(s) and sent notification(s)."
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendOffer() {
        int[] selectedRows = appTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select applicants to send offers.");
            return;
        }

        int count = 0;
        for (int row : selectedRows) {
            TAApplication app = currentApplications.get(row);

            MOJob job = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(app.getJobId()))
                    .findFirst()
                    .orElse(null);

            if (job != null) {
                MOOffer offer = new MOOffer();
                offer.setApplicationId(app.getApplicationId());
                offer.setMoUserId(currentUser.getUserId());
                offer.setTaUserId(app.getTaUserId());
                offer.setModuleCode(job.getModuleCode());
                offer.setOfferedHours(job.getWeeklyHours());
                offer.setStatus("SENT");

                offerService.sendOffer(offer);

                app.setStatus("OFFER_SENT");
                jobService.updateApplication(app);
                count++;
            }
        }

        loadApplications();
        JOptionPane.showMessageDialog(
                this,
                "Successfully sent " + count + " offer(s).\nTA users can now view the offer in their notification panel.",
                "Offers Sent",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
