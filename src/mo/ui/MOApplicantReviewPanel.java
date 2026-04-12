package mo.ui;

import common.entity.*;
import common.service.*;
import ta.entity.TAApplication; // 确保导入正确的实体

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
        JButton offerBtn = new JButton("🚀 Send Official Offer");
        offerBtn.setBackground(new Color(16, 185, 129));
        offerBtn.setForeground(Color.WHITE);
        offerBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        offerBtn.addActionListener(e -> sendOfficialOffer());

        btnPanel.add(viewBtn);
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