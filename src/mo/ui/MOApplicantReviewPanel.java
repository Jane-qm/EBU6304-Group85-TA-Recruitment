package mo.ui;

import common.entity.*;
import common.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MOApplicantReviewPanel extends JPanel {
    private final User currentUser;

    // 引入需要的 Service
    private final UserService userService = new UserService(); // 【新增】：用于抓取 TA 的邮箱和姓名
    private final TAApplicationService appService = new TAApplicationService();
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
        JLabel titleLabel = new JLabel("Review Applicants & Send Offers");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"App ID", "Job ID", "TA User ID", "Statement", "Status", "Applied At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        appTable = new JTable(tableModel);
        appTable.setRowHeight(30);
        appTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        // 【核心修改】：开启多选模式，支持按住 Ctrl 进行批量操作
        appTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        add(new JScrollPane(appTable), BorderLayout.CENTER);
    }

    private void initActionButtons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);

        // 【新增】：查看简历/资料按钮
        JButton viewCvBtn = new JButton("📄 View Profile/CV");
        viewCvBtn.addActionListener(e -> viewTAProfile());

        JButton rejectBtn = new JButton("Reject");
        rejectBtn.addActionListener(e -> processApplications("REJECTED"));

        JButton waitlistBtn = new JButton("Waitlist");
        waitlistBtn.addActionListener(e -> processApplications("WAITLISTED"));

        JButton acceptBtn = new JButton("Accept");
        acceptBtn.addActionListener(e -> processApplications("ACCEPTED"));

        // 核心任务5：一键标记Accept并发送Offer
        JButton offerBtn = new JButton("🚀 Batch Send Offers");
        offerBtn.setBackground(new Color(16, 185, 129)); // 绿色突出
        offerBtn.setForeground(Color.WHITE);
        offerBtn.addActionListener(e -> sendOffer());

        //新增
        btnPanel.add(viewCvBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(rejectBtn);
        btnPanel.add(waitlistBtn);
        //新增
        btnPanel.add(acceptBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(offerBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadApplications() {
        tableModel.setRowCount(0);
        // 获取该 MO 发布的所有职位 ID
        List<Long> myJobIds = jobService.listAll().stream()
                .filter(j -> j.getMoUserId().equals(currentUser.getUserId()))
                .map(MOJob::getJobId)
                .collect(Collectors.toList());

        // 筛选出投递给这些职位的申请
        currentApplications = appService.listAll().stream()
                .filter(app -> myJobIds.contains(app.getJobId()))
                .collect(Collectors.toList());

        for (TAApplication app : currentApplications) {
            //新增
            // 【新增联表逻辑】：利用 UserID 自动去查找 TA 的姓名和邮箱
            User taUser = userService.listAll().stream()
                    .filter(u -> u.getUserId().equals(app.getTaUserId()))
                    .findFirst().orElse(null);
            
            MOJob job = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(app.getJobId()))
                    .findFirst().orElse(null);

            tableModel.addRow(new Object[]{
                    //改了
                    app.getApplicationId(), 
                    job != null ? job.getModuleCode() : app.getJobId(), 
                    taUser != null ? taUser.getEmail() :"Unknown",
                    taUser != null ? taUser.getEmail() : "N/A",  // 自动展示邮箱
                    app.getStatus(), 
                    app.getAppliedAt() != null ? app.getAppliedAt().toLocalDate() : "N/A" 
            });
        }
    }

   /**
     * 【新增方法】：查看 TA 资料 (任务 1)
     */
    private void viewTAProfile() {
        int selectedRow = appTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an applicant to view their profile.");
            return;
        }
        TAApplication app = currentApplications.get(selectedRow);
        
        // 此处展示 TA 的个人陈述，若有 CV 关联模块可在此进一步拓展
        JOptionPane.showMessageDialog(this, 
            "Applicant Statement:\n\n" + app.getStatement(), 
            "TA Profile Details", JOptionPane.INFORMATION_MESSAGE);
    }

/**
     * 【修改方法】：支持批量修改状态 (任务 1)
     */
    private void processApplications(String newStatus) {
        int[] selectedRows = appTable.getSelectedRows(); // 获取所有选中的行
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one applicant.");
            return;
        }
        
        for (int row : selectedRows) {
            TAApplication app = currentApplications.get(row);
            app.setStatus(newStatus);
            appService.createOrUpdate(app); // 保存更新
        }
        loadApplications();
        JOptionPane.showMessageDialog(this, "Successfully marked " + selectedRows.length + " applicant(s) as " + newStatus);
    }

    // 任务5：Offer 发送逻辑
    private void sendOffer() {
        int[] selectedRows = appTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select applicants to send offers.");
            return;
        }

        int count = 0;
        for (int row : selectedRows) {
            TAApplication app = currentApplications.get(row);
            
            // 获取对应的职位信息
            MOJob job = jobService.listAll().stream()
                    .filter(j -> j.getJobId().equals(app.getJobId()))
                    .findFirst().orElse(null);

            if (job != null) {
                // 生成 Offer 写入 mo_offer.json
                MOOffer offer = new MOOffer();
                offer.setApplicationId(app.getApplicationId());
                offer.setMoUserId(currentUser.getUserId());
                offer.setTaUserId(app.getTaUserId());
                offer.setModuleCode(job.getModuleCode());
                offer.setOfferedHours(job.getWeeklyHours());
                offer.setStatus("SENT");
                offerService.createOrUpdate(offer); 

                // 更新申请表状态为 OFFER_SENT (作为通知 TA 的一种状态标记)
                app.setStatus("OFFER_SENT");
                appService.createOrUpdate(app);
                
                count++;
            }
        }
        
        loadApplications(); // 刷新界面
        JOptionPane.showMessageDialog(this, 
            "Successfully sent " + count + " offer(s)!\nTA(s) will see the offer in their system without needing manual email input.", 
            "Offers Sent", JOptionPane.INFORMATION_MESSAGE);
    }
}