package ta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.TA;
import common.entity.User;
import common.ui.BaseFrame;
import ta.controller.TAApplicationController;
import ta.controller.TAAuthController;
import ta.controller.TAOfferController;

/**
 * TA 工作量追踪界面
 * 
 * @author System
 * @version 1.0
 */
public class TAWorkloadFrame extends BaseFrame {
    
    private final TA ta;
    private final TAApplicationController applicationController;
    private final TAOfferController offerController;
    private final TAAuthController authController;
    
    public static final Color PRIMARY_BLUE = new Color(59, 130, 246);

    public TAWorkloadFrame(User user) {
        super("TA Recruitment System - Workload Tracking", 1000, 700);
        this.ta = (TA) user;
        this.applicationController = new TAApplicationController();
        this.offerController = new TAOfferController();
        this.authController = new TAAuthController();
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));
        
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createContent(), BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("Workload Tracking");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 35, 45));
        
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        backBtn.setForeground(PRIMARY_BLUE);
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new TAMainFrame(ta).setVisible(true);
            dispose();
        });
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(backBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    private JScrollPane createContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(0, 30, 30, 30));
        
        // 工作量统计卡片
        panel.add(createWorkloadSummary());
        panel.add(Box.createVerticalStrut(25));
        
        // 接受的 Offer 列表
        panel.add(createAcceptedOffersSection());
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        return scrollPane;
    }
    
    private JPanel createWorkloadSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setOpaque(false);
        
        List<MOOffer> offers = offerController.getMyOffers(ta.getUserId());
        int totalHours = offers.stream()
                .filter(o -> "ACCEPTED".equals(o.getStatus()))
                .mapToInt(MOOffer::getOfferedHours)
                .sum();
        int acceptedCount = (int) offers.stream().filter(o -> "ACCEPTED".equals(o.getStatus())).count();
        int pendingOffers = (int) offers.stream().filter(o -> "SENT".equals(o.getStatus())).count();
        
        panel.add(createSummaryCard("Total Hours", String.valueOf(totalHours), "per week", PRIMARY_BLUE));
        panel.add(createSummaryCard("Active Positions", String.valueOf(acceptedCount), "TA roles", new Color(34, 197, 94)));
        panel.add(createSummaryCard("Pending Offers", String.valueOf(pendingOffers), "awaiting response", new Color(234, 179, 8)));
        
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
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        List<MOOffer> offers = offerController.getMyOffers(ta.getUserId());
        List<MOOffer> acceptedOffers = offers.stream()
                .filter(o -> "ACCEPTED".equals(o.getStatus()))
                .toList();
        
        if (acceptedOffers.isEmpty()) {
            JLabel emptyLabel = new JLabel("No active positions yet. Apply for courses to get started!");
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            emptyLabel.setForeground(new Color(107, 114, 128));
            emptyLabel.setAlignmentX(CENTER_ALIGNMENT);
            contentPanel.add(emptyLabel);
        } else {
            for (MOOffer offer : acceptedOffers) {
                contentPanel.add(createOfferCard(offer));
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOfferCard(MOOffer offer) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(12, 15, 12, 15)
        ));
        
        String courseName = getCourseName(offer.getModuleCode());
        
        JLabel courseLabel = new JLabel(courseName);
        courseLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel hoursLabel = new JLabel(offer.getOfferedHours() + " hours/week");
        hoursLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        hoursLabel.setForeground(PRIMARY_BLUE);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(hoursLabel, BorderLayout.EAST);
        
        card.add(courseLabel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private String getCourseName(String moduleCode) {
        List<MOJob> jobs = applicationController.getPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getModuleCode().equals(moduleCode)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return moduleCode;
    }
}