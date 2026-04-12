package ta.ui;

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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import common.domain.ApplicationStatus;
import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.TA;
import ta.controller.TAApplicationController;
import ta.controller.TAOfferController;
import ta.entity.TAApplication;
import ta.service.TAApplicationService;
import ta.ui.components.ActionButtonRenderer;
import ta.ui.components.StatusCellRenderer;

/**
 * TA 我的申请面板
 * 显示所有申请记录，支持取消申请和查看详情，以及处理 Offer
 */
public class TAApplicationsPanel extends JPanel {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    private final TAOfferController offerController;
    private final TAApplicationService applicationService;
    
    private static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    
    private JTable applicationsTable;
    private DefaultTableModel tableModel;
    private List<TAApplication> applications;
    private List<MOOffer> offers;
    
    public TAApplicationsPanel(TA ta) {
        this.ta = ta;
        this.applicationController = new TAApplicationController();
        this.offerController = new TAOfferController();
        this.applicationService = new TAApplicationService();
        
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
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JScrollPane createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(0, 30, 30, 30));
        
        panel.add(createApplicationsTable());
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setEnabled(false);
        
        return scrollPane;
    }
    
    private JScrollPane createApplicationsTable() {
        String[] columns = {"Course", "Status", "Applied Date", "", "", ""};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshTable();
        
        applicationsTable = new JTable(tableModel);
        applicationsTable.setRowHeight(50);
        applicationsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        applicationsTable.setShowGrid(false);
        applicationsTable.setIntercellSpacing(new Dimension(0, 0));
        applicationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        applicationsTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        applicationsTable.getColumnModel().getColumn(3).setCellRenderer(new ActionButtonRenderer());
        applicationsTable.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
        applicationsTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        
        applicationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = applicationsTable.rowAtPoint(e.getPoint());
                int col = applicationsTable.columnAtPoint(e.getPoint());
                
                if (row < applications.size()) {
                    TAApplication app = applications.get(row);
                    
                    if (col == 3) {
                        showApplicationDetailDialog(app);
                    } else if (col == 4) {
                        String action = (String) applicationsTable.getValueAt(row, 4);
                        if ("Cancel".equals(action)) {
                            handleCancelApplication(app);
                        } else if ("Accept".equals(action)) {
                            handleAcceptOffer(app);
                        }
                    } else if (col == 5) {
                        String action = (String) applicationsTable.getValueAt(row, 5);
                        if ("Reject".equals(action)) {
                            handleRejectOffer(app);
                        }
                    }
                }
            }
        });
        
        JTableHeader header = applicationsTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(107, 114, 128));
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        applicationsTable.getColumnModel().getColumn(0).setPreferredWidth(350);
        applicationsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        applicationsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        applicationsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        applicationsTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        applicationsTable.getColumnModel().getColumn(5).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(800, 400));
        scrollPane.getHorizontalScrollBar().setEnabled(false);

        return scrollPane;
    }
    
    private void handleCancelApplication(TAApplication app) {
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
    
    private void handleAcceptOffer(TAApplication app) {
        MOOffer offer = findOfferByApplicationId(app.getApplicationId());
        if (offer == null) {
            JOptionPane.showMessageDialog(this, 
                "Offer not found. Please contact the MO.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Do you want to ACCEPT this offer?\n\n" +
            "Course: " + applicationController.getCourseName(app.getJobId()) + "\n" +
            "Weekly Hours: " + offer.getOfferedHours() + " hours",
            "Confirm Accept Offer",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = offerController.acceptOfferWithFeedback(offer.getOfferId(), null);
            if (success) {
                app.setStatus(ApplicationStatus.HIRED);
                applicationService.createOrUpdate(app);
                
                JOptionPane.showMessageDialog(this, 
                    "You have accepted the offer! Your application status has been updated to HIRED.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                refresh();
                TAMainFrame mainFrame = (TAMainFrame) getTopLevelAncestor();
                if (mainFrame != null) {
                    mainFrame.refreshAllPanels();
                }
            }
        }
    }
    
    private void handleRejectOffer(TAApplication app) {
        MOOffer offer = findOfferByApplicationId(app.getApplicationId());
        if (offer == null) {
            JOptionPane.showMessageDialog(this, 
                "Offer not found. Please contact the MO.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to REJECT this offer?\n\n" +
            "Course: " + applicationController.getCourseName(app.getJobId()) + "\n" +
            "This action cannot be undone.",
            "Confirm Reject Offer",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = offerController.rejectOfferWithFeedback(offer.getOfferId(), null);
            if (success) {
                app.setStatus(ApplicationStatus.REJECTED);
                applicationService.createOrUpdate(app);
                
                JOptionPane.showMessageDialog(this, 
                    "You have rejected the offer.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                refresh();
                TAMainFrame mainFrame = (TAMainFrame) getTopLevelAncestor();
                if (mainFrame != null) {
                    mainFrame.refreshAllPanels();
                }
            }
        }
    }
    
    private MOOffer findOfferByApplicationId(Long applicationId) {
        List<MOOffer> allOffers = offerController.getMyOffers(ta.getUserId());
        for (MOOffer offer : allOffers) {
            if (applicationId.equals(offer.getApplicationId()) && "SENT".equals(offer.getStatus())) {
                return offer;
            }
        }
        return null;
    }
    
    private void showApplicationDetailDialog(TAApplication app) {
        MOJob job = applicationController.getJobById(app.getJobId());
        if (job == null) {
            JOptionPane.showMessageDialog(this, "Course information not found.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(job.getModuleCode() + " - " + job.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
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
        statusTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        String statusText = applicationController.getDisplayStatus(app);
        JLabel statusValueLabel = new JLabel(statusText);
        statusValueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        if (ApplicationStatus.isAccepted(app.getStatus()) || ApplicationStatus.isHired(app.getStatus())) {
            statusValueLabel.setForeground(new Color(34, 197, 94));
        } else if (ApplicationStatus.isAwaitingReview(app.getStatus())) {
            statusValueLabel.setForeground(new Color(234, 179, 8));
        } else if (ApplicationStatus.isRejected(app.getStatus())) {
            statusValueLabel.setForeground(new Color(239, 68, 68));
        } else if (ApplicationStatus.WAITLISTED.equals(app.getStatus())) {
            statusValueLabel.setForeground(new Color(59, 130, 246));
        } else {
            statusValueLabel.setForeground(new Color(107, 114, 128));
        }
        
        JPanel statusInnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusInnerPanel.setBackground(new Color(248, 250, 252));
        statusInnerPanel.add(statusTitleLabel);
        statusInnerPanel.add(statusValueLabel);
        statusPanel.add(statusInnerPanel, BorderLayout.WEST);
        
        panel.add(statusPanel);
        panel.add(Box.createVerticalStrut(15));
        
        JPanel infoPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        infoPanel.add(createInfoLabel("Module Code:"));
        infoPanel.add(createValueLabel(job.getModuleCode()));
        
        infoPanel.add(createInfoLabel("Weekly Hours:"));
        infoPanel.add(createValueLabel(job.getWeeklyHours() + " hours/week"));
        
        infoPanel.add(createInfoLabel("Applied Date:"));
        String appliedDate = app.getAppliedAt() != null ? 
            app.getAppliedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
        infoPanel.add(createValueLabel(appliedDate));
        
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(15));
        
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        JLabel statementTitle = new JLabel("Application Statement");
        statementTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        statementTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(statementTitle);
        panel.add(Box.createVerticalStrut(8));
        
        JTextArea statementArea = new JTextArea(5, 50);
        statementArea.setText(app.getStatement() != null ? app.getStatement() : "No statement provided.");
        statementArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);
        statementArea.setEditable(false);
        statementArea.setBackground(Color.WHITE);
        statementArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        
        JScrollPane statementScroll = new JScrollPane(statementArea);
        statementScroll.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(statementScroll);
        
        JOptionPane.showConfirmDialog(null, new JScrollPane(panel), 
            "Application Details", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(30, 35, 45));
        return label;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        applications = applicationController.getMyApplications(ta.getUserId());
        offers = offerController.getMyOffers(ta.getUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TAApplication app : applications) {
            String status = applicationController.getDisplayStatus(app);
            String appliedAt = app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "";
            String courseName = applicationController.getCourseName(app.getJobId());
            
            if (courseName.length() > 45) {
                courseName = courseName.substring(0, 42) + "...";
            }
            
            String action1 = "—";
            String action2 = "—";
            
            if (ApplicationStatus.isCancellable(app.getStatus())) {
                action1 = "Cancel";
                action2 = "—";
            } else {
                boolean hasSentOffer = offers.stream().anyMatch(o -> 
                    app.getApplicationId().equals(o.getApplicationId()) && "SENT".equals(o.getStatus()));
                if (hasSentOffer) {
                    action1 = "Accept";
                    action2 = "Reject";
                }
            }
            
            tableModel.addRow(new Object[]{courseName, status, appliedAt, "Detail", action1, action2});
        }

        if (applications.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "—", "—", "—", "—", "—"});
        }
    }
    
    public void refresh() {
        refreshTable();
        revalidate();
        repaint();
    }
}