package mo.ui;

import common.entity.*;
import common.service.*;
import ta.entity.TAApplication;
import ta.service.TAApplicationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MOApplicantReviewPanel extends JPanel {
    private final User currentUser;
    private final UserService userService = new UserService();
    private final MOJobService jobService = new MOJobService();
    private final MOOfferService offerService = new MOOfferService();
    private final TAApplicationService appService = new TAApplicationService();
    
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
        JLabel titleLabel = new JLabel("Applicant Review & Offer Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
    }

    private void initTable() {
        // 表头：App ID, 课程, TA邮箱, 申请陈述, 当前状态, 申请时间
        String[] columns = {"App ID", "Module", "TA Email", "Statement", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        appTable = new JTable(tableModel);
        appTable.setRowHeight(35);
        appTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(appTable), BorderLayout.CENTER);
    }

    private void initActionButtons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);

        JButton viewBtn = new JButton("📄 View TA Profile");
        viewBtn.addActionListener(e -> viewProfile());

        // 任务4：一键标记（内部初筛）
        JButton rejectBtn = new JButton("Reject");
        rejectBtn.addActionListener(e -> updateAppStatus("REJECTED"));

        JButton waitlistBtn = new JButton("Waitlist");
        waitlistBtn.addActionListener(e -> updateAppStatus("WAITLISTED"));

        JButton acceptBtn = new JButton("Pass Screen (Accept)");
        acceptBtn.addActionListener(e -> updateAppStatus("ACCEPTED"));

        // 任务5：正式发送 Offer（进入 TA 最终确认阶段）
        JButton offerBtn = new JButton("Send Official Offer");
        offerBtn.setBackground(new Color(16, 185, 129));
        offerBtn.setForeground(Color.WHITE);
        offerBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        offerBtn.addActionListener(e -> sendOfficialOffer());

        // MO-007.2：从候补名单选人并补发 Offer
        JButton waitlistOfferBtn = new JButton("From Waitlist");
        waitlistOfferBtn.setBackground(new Color(59, 130, 246));
        waitlistOfferBtn.setForeground(Color.WHITE);
        waitlistOfferBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        waitlistOfferBtn.setToolTipText("Select a waitlisted candidate for the same job and send them an offer");
        waitlistOfferBtn.addActionListener(e -> {
            int row = appTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                        "Select any application row to identify the job, then click 'From Waitlist'.",
                        "No Row Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showWaitlistDialog(currentApplications.get(row));
        });

        btnPanel.add(viewBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(rejectBtn);
        btnPanel.add(waitlistBtn);
        btnPanel.add(acceptBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(offerBtn);
        btnPanel.add(waitlistOfferBtn);

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
            User taUser = userService.getUserById(app.getTaUserId());
            MOJob job = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(app.getJobId()))
                    .findFirst().orElse(null);

            tableModel.addRow(new Object[]{
                    app.getApplicationId(),
                    job != null ? job.getModuleCode() : "N/A",
                    taUser != null ? taUser.getEmail() : "Unknown",
                    app.getStatement(),
                    app.getStatus(),
                    app.getAppliedAt() != null ? app.getAppliedAt().toLocalDate() : "N/A"
            });
        }
    }

    private void viewProfile() {
        int row = appTable.getSelectedRow();
        if (row == -1) return;
        TAApplication app = currentApplications.get(row);
        JOptionPane.showMessageDialog(this, "TA Statement:\n" + app.getStatement(), "Applicant Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    // 内部初筛状态更新
    private void updateAppStatus(String status) {
        int[] rows = appTable.getSelectedRows();
        if (rows.length == 0) return;
        
        for (int row : rows) {
            TAApplication app = currentApplications.get(row);
            app.setStatus(status);
            jobService.updateApplication(app);
        }
        loadApplications();
        JOptionPane.showMessageDialog(this, "Internal status updated to: " + status);
    }

    /**
     * 实现任务 5：发送 Offer（关键逻辑整改）
     * 只有发送了 Offer，TA 端才会收到反馈，并拥有最终“接受/拒绝”的权力。
     */
    // -----------------------------------------------------------------------
    // MO-007.2: Waitlist -> Offer
    // -----------------------------------------------------------------------

    private void showWaitlistDialog(TAApplication referenceApp) {
        Long jobId = referenceApp.getJobId();
        MOJob job = jobService.listAll().stream()
                .filter(j -> j.getJobId().equals(jobId))
                .findFirst().orElse(null);
        if (job == null) {
            JOptionPane.showMessageDialog(this, "Job record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<TAApplication> waitlist = appService.listWaitlistedByJobId(jobId);
        if (waitlist.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No waitlisted candidates for " + job.getModuleCode() + ".",
                    "Empty Waitlist", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(
                ancestor instanceof Frame ? (Frame) ancestor : null,
                "Waitlist - " + job.getModuleCode() + " - " + job.getTitle(),
                true);
        dialog.setSize(750, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel header = new JLabel("<html><b>" + waitlist.size()
                + "</b> candidate(s) on waitlist - sorted by earliest application date</html>");
        header.setBorder(BorderFactory.createEmptyBorder(12, 14, 0, 14));
        dialog.add(header, BorderLayout.NORTH);

        String[] cols = {"#", "TA Email", "Applied Date", "Statement"};
        DefaultTableModel wModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int i = 0; i < waitlist.size(); i++) {
            TAApplication wa = waitlist.get(i);
            User taUser = userService.getUserById(wa.getTaUserId());
            wModel.addRow(new Object[]{
                    i + 1,
                    taUser != null ? taUser.getEmail() : "TA #" + wa.getTaUserId(),
                    wa.getAppliedAt() != null ? wa.getAppliedAt().toLocalDate() : "N/A",
                    wa.getStatement()
            });
        }
        JTable wTable = new JTable(wModel);
        wTable.setRowHeight(30);
        wTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wTable.getColumnModel().getColumn(0).setMaxWidth(40);
        wTable.getColumnModel().getColumn(2).setMaxWidth(110);
        dialog.add(new JScrollPane(wTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton cancelBtn = new JButton("Cancel");
        JButton sendBtn   = new JButton("Send Offer to Selected");
        sendBtn.setBackground(new Color(16, 185, 129));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font("SansSerif", Font.BOLD, 13));

        cancelBtn.addActionListener(e -> dialog.dispose());
        sendBtn.addActionListener(e -> {
            int sel = wTable.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select a candidate first.");
                return;
            }
            TAApplication chosen = waitlist.get(sel);
            dialog.dispose();
            sendOfferFromWaitlist(chosen, job);
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(sendBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Transitions the application from WAITLISTED to OFFER_SENT:
     * 1. Creates and persists a new MOOffer (also notifies the TA via NotificationService).
     * 2. Updates the application status to OFFER_SENT and persists it.
     * 3. Refreshes the main table.
     */
    private void sendOfferFromWaitlist(TAApplication app, MOJob job) {
        MOOffer offer = new MOOffer();
        offer.setApplicationId(app.getApplicationId());
        offer.setMoUserId(currentUser.getUserId());
        offer.setTaUserId(app.getTaUserId());
        offer.setModuleCode(job.getModuleCode());
        offer.setOfferedHours(job.getWeeklyHours());

        offerService.sendOffer(offer);
        app.setStatus("OFFER_SENT");
        jobService.updateApplication(app);

        loadApplications();

        User taUser = userService.getUserById(app.getTaUserId());
        String taEmail = taUser != null ? taUser.getEmail() : "TA #" + app.getTaUserId();
        JOptionPane.showMessageDialog(this,
                "Offer sent to " + taEmail + " (promoted from waitlist).\n"
                        + "Module: " + job.getModuleCode() + " - " + job.getTitle() + "\n"
                        + "Application status updated to OFFER_SENT.",
                "Offer Sent", JOptionPane.INFORMATION_MESSAGE);
    }

    // -----------------------------------------------------------------------

    private void sendOfficialOffer() {
        int[] rows = appTable.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select applicants to send offers.");
            return;
        }

        int count = 0;
        for (int row : rows) {
            TAApplication app = currentApplications.get(row);
            
            // 逻辑检查：建议只有初筛 ACCEPTED 的人才能发 Offer
            if (!"ACCEPTED".equals(app.getStatus())) {
                continue;
            }

            MOJob job = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(app.getJobId()))
                    .findFirst().orElse(null);

            if (job != null) {
                // 1. 创建 Offer 实体
                MOOffer offer = new MOOffer();
                offer.setApplicationId(app.getApplicationId());
                offer.setMoUserId(currentUser.getUserId());
                offer.setTaUserId(app.getTaUserId());
                offer.setModuleCode(job.getModuleCode());
                offer.setOfferedHours(job.getWeeklyHours());
                
                // 2. 调用 offerService 发送 Offer (同步 mo_offer.json 并通知 TA)
                // 这一步之后，TA 的界面会显示这个 Offer，等待 TA 点击 Accept 或 Decline
                offerService.sendOffer(offer);

                // 3. 更新申请状态为 OFFER_SENT，表示 MO 已给出反馈
                app.setStatus("OFFER_SENT");
                jobService.updateApplication(app);
                count++;
            }
        }
        loadApplications();
        if (count > 0) {
            JOptionPane.showMessageDialog(this, "Successfully sent " + count + " offers.\nWaiting for TA's final decision.");
        } else {
            JOptionPane.showMessageDialog(this, "Offers can only be sent to applicants marked as 'ACCEPTED'.");
        }
    }
}