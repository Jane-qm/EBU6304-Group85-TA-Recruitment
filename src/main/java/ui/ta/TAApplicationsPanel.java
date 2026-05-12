package ui.ta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;          // <-- Added missing import
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import modules.application.ApplicationStatus;
import modules.job.Job;
import modules.user.MO;
import modules.user.TA;
import modules.user.User;
import modules.user.UserService;
import modules.application.ApplicationController;
import modules.application.Application;
import ui.common.ScrollPaneTopHelper;
import ui.common.TableScrollUtil;
import ui.ta.components.ActionButtonRenderer;
import ui.ta.components.StatusCellRenderer;

/**
 * TA 我的申请面板
 * 显示所有申请记录，支持取消申请、查看详情、响应 Offer
 * 
 * @version 3.1 - 修复 Offer 响应按钮显示逻辑，确保刷新后正确更新
 */
public class TAApplicationsPanel extends JPanel {
    
    private final TA ta;
    private final ApplicationController applicationController;
    private final UserService userService = UserService.getInstance();
    
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    private List<Application> applications;
    
    public TAApplicationsPanel(TA ta) {
        this.ta = ta;
        this.applicationController = new ApplicationController();
        
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        
        initUI();
    }
    
    private void initUI() {
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        JScrollPane contentScroll = createContentPanel();
        add(contentScroll, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("My Applications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        // 添加刷新按钮
        JButton refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        applyPrimaryButtonStyle(refreshBtn);
        refreshBtn.addActionListener(e -> refresh());
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(refreshBtn, BorderLayout.EAST);
        
        return panel;
    }

    private void applyPrimaryButtonStyle(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_BLUE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }
    
    private JScrollPane createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(0, 30, 30, 30));
        
        panel.add(createApplicationsTable());
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }
    
    private JScrollPane createApplicationsTable() {
        String[] columns = {"Course", "MO", "Status", "Applied Date", "Detail", "", ""};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshTable();
        
        applicationsTable = new JTable(tableModel);
        applicationsTable.setRowHeight(50);
        applicationsTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        applicationsTable.setShowGrid(false);
        applicationsTable.setIntercellSpacing(new Dimension(0, 0));
        
        applicationsTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        applicationsTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
        applicationsTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        applicationsTable.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        
        applicationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = applicationsTable.rowAtPoint(e.getPoint());
                int col = applicationsTable.columnAtPoint(e.getPoint());
                
                if (row < applications.size()) {
                    Application app = applications.get(row);
                    
                    if (col == 4) {
                        showApplicationDetailDialog(app);
                    } else if (col == 5) {
                        String action = (String) applicationsTable.getValueAt(row, 5);
                        if ("Cancel".equals(action)) {
                            handleCancelApplication(app);
                        } else if ("Accept".equals(action)) {
                            handleAcceptOffer(app);
                        }
                    } else if (col == 6) {
                        String action = (String) applicationsTable.getValueAt(row, 6);
                        if ("Reject".equals(action)) {
                            handleRejectOffer(app);
                        }
                    }
                }
            }
        });
        
        JTableHeader header = applicationsTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        TableScrollUtil.ColumnSpec[] appCols = {
                TableScrollUtil.ColumnSpec.flex(170, 300),
                TableScrollUtil.ColumnSpec.flex(78, 125),
                TableScrollUtil.ColumnSpec.flex(72, 118),
                TableScrollUtil.ColumnSpec.fixed(104),
                TableScrollUtil.ColumnSpec.fixed(70),
                TableScrollUtil.ColumnSpec.fixed(78),
                TableScrollUtil.ColumnSpec.fixed(78),
        };

        JScrollPane scrollPane = TableScrollUtil.wrapTable(applicationsTable);
        TableScrollUtil.installResponsiveColumns(applicationsTable, scrollPane, appCols);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(800, 400));

        return scrollPane;
    }
    
    private void handleCancelApplication(Application app) {
        boolean success = applicationController.cancelApplicationWithFeedback(
            app.getApplicationId(), null);
        if (success) {
            refresh();
            TAMainFrame mainFrame = (TAMainFrame) getTopLevelAncestor();
            if (mainFrame != null) {
                mainFrame.refreshAllPanels();
            }
        }
    }
    
    private void handleAcceptOffer(Application app) {
        java.awt.Window w = SwingUtilities.getWindowAncestor(this);
        JFrame parent = w instanceof JFrame ? (JFrame) w : null;
        boolean success = applicationController.acceptOfferWithFeedback(app.getApplicationId(), parent);
        if (success) {
            refresh();
            TAMainFrame mainFrame = (TAMainFrame) getTopLevelAncestor();
            if (mainFrame != null) {
                mainFrame.refreshAllPanels();
            }
        }
    }

    private void handleRejectOffer(Application app) {
        java.awt.Window w = SwingUtilities.getWindowAncestor(this);
        JFrame parent = w instanceof JFrame ? (JFrame) w : null;
        boolean success = applicationController.rejectOfferWithFeedback(app.getApplicationId(), parent);
        if (success) {
            refresh();
            TAMainFrame mainFrame = (TAMainFrame) getTopLevelAncestor();
            if (mainFrame != null) {
                mainFrame.refreshAllPanels();
            }
        }
    }
    
    private static String moDisplayName(User mo) {
        if (mo == null) {
            return "—";
        }
        if (mo instanceof MO) {
            String n = ((MO) mo).getName();
            if (n != null && !n.isBlank()) {
                return n.trim();
            }
        }
        String email = mo.getEmail();
        if (email == null) {
            return "—";
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private static String moEmail(User mo) {
        if (mo == null || mo.getEmail() == null) {
            return "—";
        }
        return mo.getEmail();
    }

    private void showApplicationDetailDialog(Application app) {
        Job job = applicationController.getJobById(app.getJobId());
        if (job == null) {
            JOptionPane.showMessageDialog(this, "Course information not found.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User mo = job.getMoUserId() != null ? userService.findById(job.getMoUserId()) : null;
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(job.getModuleCode() + " - " + job.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(248, 250, 252));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(12, 15, 12, 15)
        ));
        statusPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel statusTitleLabel = new JLabel("Application Status: ");
        statusTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        String statusText = applicationController.getDisplayStatus(app);
        JLabel statusValueLabel = new JLabel(statusText);
        statusValueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        if (ApplicationStatus.isHired(app.getStatus())) {
            statusValueLabel.setForeground(new Color(34, 197, 94));
        } else if (ApplicationStatus.OFFER_SENT.equals(app.getStatus())) {
            statusValueLabel.setForeground(PRIMARY_BLUE);
        } else if (ApplicationStatus.isRejected(app.getStatus())) {
            statusValueLabel.setForeground(new Color(239, 68, 68));
        } else if (ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
            statusValueLabel.setForeground(new Color(234, 179, 8));
        } else {
            statusValueLabel.setForeground(new Color(107, 114, 128));
        }
        
        JPanel statusInnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusInnerPanel.setBackground(new Color(248, 250, 252));
        statusInnerPanel.add(statusTitleLabel);
        statusInnerPanel.add(statusValueLabel);
        statusPanel.add(statusInnerPanel, BorderLayout.WEST);
        
        if (app.getOfferedHours() != null) {
            JPanel offerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            offerPanel.setBackground(new Color(248, 250, 252));
            JLabel offerLabel = new JLabel("Offered Hours: " + app.getOfferedHours() + " hours/week");
            offerLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
            offerPanel.add(offerLabel);
            if (job.getOfferResponseDeadline() != null) {
                JLabel dl = new JLabel("   · Respond by: "
                        + job.getOfferResponseDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                dl.setFont(new Font("SansSerif", Font.PLAIN, 15));
                offerPanel.add(dl);
            }
            statusPanel.add(offerPanel, BorderLayout.SOUTH);
        }
        
        panel.add(statusPanel);
        panel.add(Box.createVerticalStrut(15));
        
        JPanel infoPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        infoPanel.add(createInfoLabel("Module Code:"));
        infoPanel.add(createValueLabel(job.getModuleCode()));

        infoPanel.add(createInfoLabel("Module Organiser (MO):"));
        infoPanel.add(createValueLabel(moDisplayName(mo)));

        infoPanel.add(createInfoLabel("MO Email:"));
        infoPanel.add(createValueLabel(moEmail(mo)));
        
        infoPanel.add(createInfoLabel("Weekly Hours:"));
        infoPanel.add(createValueLabel(job.getWeeklyHours() + " hours/week"));

        if (job.getOfferResponseDeadline() != null) {
            infoPanel.add(createInfoLabel("Respond to offer by (MO deadline):"));
            infoPanel.add(createValueLabel(
                    job.getOfferResponseDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        }
        
        infoPanel.add(createInfoLabel("Applied Date:"));
        String appliedDate = app.getAppliedAt() != null ? 
            app.getAppliedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
        infoPanel.add(createValueLabel(appliedDate));
        
        if (app.getOfferExpiryAt() != null) {
            infoPanel.add(createInfoLabel("Offer Expires:"));
            infoPanel.add(createValueLabel(app.getOfferExpiryAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        }
        
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(15));
        
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        JLabel statementTitle = new JLabel("Application Statement");
        statementTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        statementTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(statementTitle);
        panel.add(Box.createVerticalStrut(8));
        
        JTextArea statementArea = new JTextArea(5, 50);
        statementArea.setText(app.getStatement() != null ? app.getStatement() : "No statement provided.");
        statementArea.setFont(new Font("SansSerif", Font.PLAIN, 15));
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);
        statementArea.setEditable(false);
        statementArea.setBackground(Color.WHITE);
        statementArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        
        JScrollPane statementScroll = new JScrollPane(statementArea);
        statementScroll.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(statementScroll);
        
        JScrollPane outer = new JScrollPane(panel);
        ScrollPaneTopHelper.installScrollStartsAtTop(outer);
        JOptionPane.showConfirmDialog(null, outer, 
            "Application Details", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(new Color(30, 35, 45));
        return label;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        applications = applicationController.getMyApplications(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Application app : applications) {
            String status = applicationController.getShortDisplayStatus(app);
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String courseName = applicationController.getCourseName(app.getJobId());

            Job job = applicationController.getJobById(app.getJobId());
            User mo = job != null && job.getMoUserId() != null
                    ? userService.findById(job.getMoUserId()) : null;
            String moName = moDisplayName(mo);
            
            if (courseName.length() > 45) {
                courseName = courseName.substring(0, 42) + "...";
            }
            if (moName.length() > 28) {
                moName = moName.substring(0, 25) + "...";
            }
            
            String action1 = "—";
            String action2 = "—";
            
            if (ApplicationStatus.isCancellable(app.getStatus())) {
                action1 = "Cancel";
                action2 = "—";
            } else if (ApplicationStatus.OFFER_SENT.equals(app.getStatus())) {
                if (app.isOfferExpired()) {
                    action1 = "Expired";
                    action2 = "—";
                } else {
                    action1 = "Accept";
                    action2 = "Reject";
                }
            }
            
            tableModel.addRow(new Object[]{courseName, moName, status, appliedAt, "Detail", action1, action2});
        }

        if (applications.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "—", "—", "—", "—", "—", "—"});
        }
    }
    
    public void refresh() {
        refreshTable();
        revalidate();
        repaint();
    }
}
