package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import common.domain.ApplicationStatus;
import common.entity.MOJob;
import common.entity.TA;
import ta.controller.TAApplicationController;
import ta.entity.TAApplication;

public class TAWorkloadPanel extends JPanel {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color ACCEPTED_COLOR = new Color(34, 197, 94);
    private static final Color PENDING_COLOR = new Color(234, 179, 8);
    
    private JPanel contentPanel;
    
    public TAWorkloadPanel(TA ta) {
        this.ta = ta;
        this.applicationController = new TAApplicationController();
        
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
        
        JLabel titleLabel = new JLabel("Workload Tracking");
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
        
        panel.add(createWorkloadSummary());
        panel.add(Box.createVerticalStrut(25));
        
        panel.add(createAcceptedOffersSection());
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }
    
    private JPanel createWorkloadSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setOpaque(false);
        
        List<TAApplication> applications = applicationController.getMyApplications(ta.getUserId());
        
        // 从 Application 获取已录用的职位信息
        int totalHours = applications.stream()
                .filter(a -> ApplicationStatus.isHired(a.getStatus()))
                .mapToInt(a -> a.getOfferedHours() != null ? a.getOfferedHours() : 0)
                .sum();
        int hiredCount = (int) applications.stream()
                .filter(a -> ApplicationStatus.isHired(a.getStatus()))
                .count();
        int offerSentCount = (int) applications.stream()
                .filter(a -> ApplicationStatus.OFFER_SENT.equals(a.getStatus()))
                .count();
        
        panel.add(createSummaryCard("Total Hours", String.valueOf(totalHours), "per week", PRIMARY_BLUE));
        panel.add(createSummaryCard("Active Positions", String.valueOf(hiredCount), "TA roles", ACCEPTED_COLOR));
        panel.add(createSummaryCard("Pending Offers", String.valueOf(offerSentCount), "awaiting response", PENDING_COLOR));
        
        return panel;
    }
    
    private JPanel createSummaryCard(String title, String value, String unit, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(107, 114, 128));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel unitLabel = new JLabel(unit);
        unitLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        unitLabel.setForeground(new Color(156, 163, 175));
        unitLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(unitLabel);
        
        return card;
    }
    
    private JPanel createAcceptedOffersSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("My Active Positions");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 35, 45));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        refreshContent();
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshContent() {
        contentPanel.removeAll();
        
        List<TAApplication> applications = applicationController.getMyApplications(ta.getUserId());
        List<TAApplication> hiredApplications = applications.stream()
                .filter(a -> ApplicationStatus.isHired(a.getStatus()))
                .collect(Collectors.toList());
        
        if (hiredApplications.isEmpty()) {
            JLabel emptyLabel = new JLabel("No active positions yet. Apply for courses to get started!");
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            emptyLabel.setForeground(new Color(107, 114, 128));
            emptyLabel.setAlignmentX(CENTER_ALIGNMENT);
            contentPanel.add(emptyLabel);
        } else {
            for (TAApplication app : hiredApplications) {
                contentPanel.add(createOfferCard(app));
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private JPanel createOfferCard(TAApplication app) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(12, 15, 12, 15)
        ));
        
        String courseName = getCourseName(app.getJobId());
        
        JLabel courseLabel = new JLabel(courseName);
        courseLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        int hours = app.getOfferedHours() != null ? app.getOfferedHours() : 0;
        JLabel hoursLabel = new JLabel(hours + " hours/week");
        hoursLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        hoursLabel.setForeground(PRIMARY_BLUE);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(hoursLabel, BorderLayout.EAST);
        
        card.add(courseLabel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private String getCourseName(Long jobId) {
        List<MOJob> jobs = applicationController.getPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return "Course #" + jobId;
    }
    
    public void refresh() {
        refreshContent();
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
}