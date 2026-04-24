package mo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import common.domain.ApplicationStatus;
import common.entity.MOJob;
import common.entity.User;
import common.service.MOJobService;
import common.service.UserService;
import ta.entity.TAApplication;
import ta.service.TAApplicationService;

/**
 * MO 申请人审核面板 - 增强版
 * 包含自动刷新、调试日志和状态栏
 */
public class MOApplicantReviewPanel extends JPanel {
    private final User currentUser;
    private final UserService userService = new UserService();
    private final MOJobService jobService = new MOJobService();
    private final TAApplicationService appService = new TAApplicationService();
    
    private JTable appTable;
    private DefaultTableModel tableModel;
    private List<TAApplication> currentApplications;
    private JLabel statusLabel;          // 状态栏
    private Timer refreshTimer;          // 自动刷新定时器

    public MOApplicantReviewPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initHeader();
        initStatusBar();          // 新增状态栏
        initTable();
        initActionButtons();
        loadApplications();

        // 每 10 秒自动刷新
        refreshTimer = new Timer(10000, e -> loadApplications());
        refreshTimer.start();
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Applicant Review & Offer Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton refreshBtn = new JButton("⟳ Refresh Now");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        refreshBtn.setBackground(new Color(59, 130, 246));
        //refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadApplications());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }

    private void initStatusBar() {
        statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void initTable() {
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

        JButton rejectBtn = new JButton("Reject");
        rejectBtn.addActionListener(e -> updateAppStatus(ApplicationStatus.REJECTED));

        JButton waitlistBtn = new JButton("Waitlist");
        waitlistBtn.addActionListener(e -> updateAppStatus(ApplicationStatus.WAITLISTED));

        JButton acceptBtn = new JButton("Accept & Send Offer");
        acceptBtn.setBackground(new Color(16, 185, 129));
        //acceptBtn.setForeground(Color.WHITE);
        acceptBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        acceptBtn.addActionListener(e -> sendOfferToSelected());

        JButton waitlistOfferBtn = new JButton("From Waitlist");
        waitlistOfferBtn.setBackground(new Color(59, 130, 246));
        //waitlistOfferBtn.setForeground(Color.WHITE);
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
        btnPanel.add(waitlistOfferBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadApplications() {
        // 调试输出：当前 MO 用户 ID
        System.out.println("========== MOApplicantReviewPanel.loadApplications ==========");
        System.out.println("Current MO User ID: " + currentUser.getUserId());

        // 获取当前 MO 的所有职位 ID
        List<Long> myJobIds = jobService.listAll().stream()
                .filter(j -> j.getMoUserId().equals(currentUser.getUserId()))
                .map(MOJob::getJobId)
                .collect(Collectors.toList());
        System.out.println("MO job IDs: " + myJobIds);

        // 获取系统中所有申请
        List<TAApplication> allApps = jobService.listAllApplications();
        System.out.println("Total applications in system: " + allApps.size());

        // 过滤出属于当前 MO 的申请
        currentApplications = allApps.stream()
                .filter(app -> myJobIds.contains(app.getJobId()))
                .collect(Collectors.toList());
        System.out.println("Filtered applications for MO: " + currentApplications.size());

        // 更新表格
        tableModel.setRowCount(0);
        for (TAApplication app : currentApplications) {
            User taUser = userService.getUserById(app.getTaUserId());
            MOJob job = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(app.getJobId()))
                    .findFirst().orElse(null);

            String statusDisplay = ApplicationStatus.getDisplayText(app.getStatus());
            tableModel.addRow(new Object[]{
                    app.getApplicationId(),
                    job != null ? job.getModuleCode() : "N/A",
                    taUser != null ? taUser.getEmail() : "Unknown",
                    app.getStatement(),
                    statusDisplay,
                    app.getAppliedAt() != null ? app.getAppliedAt().toLocalDate() : "N/A"
            });
        }

        // 更新状态栏
        statusLabel.setText(String.format("✅ %d application(s) for your %d job(s) | Last refresh: %s",
                currentApplications.size(), myJobIds.size(),
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))));

        if (currentApplications.isEmpty() && !allApps.isEmpty()) {
            System.err.println("WARNING: There are " + allApps.size() + " total applications but none belong to your jobs.");
            System.err.println("Your job IDs: " + myJobIds);
            System.err.println("Application job IDs: " + allApps.stream().map(TAApplication::getJobId).collect(Collectors.toList()));
        }
    }

    private void viewProfile() {
        int row = appTable.getSelectedRow();
        if (row == -1) return;
        TAApplication app = currentApplications.get(row);
        JOptionPane.showMessageDialog(this, "TA Statement:\n" + app.getStatement(), 
                "Applicant Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateAppStatus(String status) {
        int[] rows = appTable.getSelectedRows();
        if (rows.length == 0) return;
        
        for (int row : rows) {
            TAApplication app = currentApplications.get(row);
            if (ApplicationStatus.SUBMITTED.equals(app.getStatus()) || 
                ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
                app.setStatus(status);
                jobService.updateApplication(app);
            }
        }
        loadApplications();
        JOptionPane.showMessageDialog(this, "Status updated to: " + status);
    }

    private void sendOfferToSelected() {
        int[] rows = appTable.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select applicants to send offers.");
            return;
        }

        int count = 0;
        for (int row : rows) {
            TAApplication app = currentApplications.get(row);
            
            if (!ApplicationStatus.SUBMITTED.equals(app.getStatus()) && 
                !ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
                continue;
            }

            MOJob job = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(app.getJobId()))
                    .findFirst().orElse(null);

            if (job != null) {
                try {
                    appService.sendOffer(app.getApplicationId(), job.getWeeklyHours(), 7);
                    count++;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error sending offer: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        loadApplications();
        if (count > 0) {
            JOptionPane.showMessageDialog(this, 
                "Successfully sent " + count + " offers.\nWaiting for TA's final decision.");
        } else {
            JOptionPane.showMessageDialog(this, 
                "Offers can only be sent to applicants with SUBMITTED or WAITLISTED status.");
        }
    }

    private void showWaitlistDialog(TAApplication referenceApp) {
        Long jobId = referenceApp.getJobId();
        MOJob job = jobService.listAll().stream()
                .filter(j -> j.getJobId().equals(jobId))
                .findFirst().orElse(null);
        if (job == null) {
            JOptionPane.showMessageDialog(this, "Job record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<TAApplication> waitlist = appService.listAll().stream()
                .filter(app -> jobId.equals(app.getJobId()) && 
                        ApplicationStatus.WAITLISTED.equals(app.getStatus()))
                .collect(Collectors.toList());
        
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

    private void sendOfferFromWaitlist(TAApplication app, MOJob job) {
        try {
            appService.sendOffer(app.getApplicationId(), job.getWeeklyHours(), 7);
            loadApplications();

            User taUser = userService.getUserById(app.getTaUserId());
            String taEmail = taUser != null ? taUser.getEmail() : "TA #" + app.getTaUserId();
            JOptionPane.showMessageDialog(this,
                    "Offer sent to " + taEmail + " (promoted from waitlist).\n"
                            + "Module: " + job.getModuleCode() + " - " + job.getTitle() + "\n"
                            + "Application status updated to OFFER_SENT.",
                    "Offer Sent", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error sending offer: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}