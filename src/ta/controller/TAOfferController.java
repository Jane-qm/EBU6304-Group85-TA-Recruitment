package ta.controller;

import java.awt.Color;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import common.entity.MOJob;
import common.entity.MOOffer;
import common.service.MOJobService;
import common.service.MOOfferService;


public class TAOfferController {
    
    private final MOOfferService offerService;
    private final MOJobService jobService;
    
    public TAOfferController() {
        this.offerService = new MOOfferService();
        this.jobService = new MOJobService();
    }
    
    /**
     * 获取 TA 的所有 Offer
     */
    public List<MOOffer> getMyOffers(Long taUserId) {
        return offerService.listByTaUserId(taUserId);
    }
    
    /**
     * 获取待处理的 Offer（SENT 状态）
     */
    public List<MOOffer> getPendingOffers(Long taUserId) {
        return offerService.listByTaUserId(taUserId).stream()
                .filter(o -> "SENT".equals(o.getStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否有待处理的 Offer
     */
    public boolean hasPendingOffers(Long taUserId) {
        return !getPendingOffers(taUserId).isEmpty();
    }
    
    /**
     * 获取最新的 Offer
     */
    public MOOffer getLatestOffer(Long taUserId) {
        List<MOOffer> offers = offerService.listByTaUserId(taUserId);
        if (offers.isEmpty()) {
            return null;
        }
        return offers.get(offers.size() - 1);
    }
    
    /**
     * 拒绝 Offer（带用户反馈）
     */
    public boolean rejectOfferWithFeedback(Long offerId, JFrame parent) {
        try {
            offerService.rejectOffer(offerId);
            JOptionPane.showMessageDialog(parent, "Offer rejected. The MO has been notified.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "System error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * 接受 Offer（带用户反馈）
     */
    public boolean acceptOfferWithFeedback(Long offerId, JFrame parent) {
        try {
            offerService.acceptOffer(offerId);
            JOptionPane.showMessageDialog(parent, "Offer accepted! Congratulations!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "System error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * 显示 Offer 处理对话框（让 TA 选择接受或拒绝）
     */
    public void handleOfferWithDialog(MOOffer offer, JFrame parent, Runnable onSuccess) {
        if (offer == null) {
            return;
        }
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("🎉 Offer Received!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(59, 130, 246));
        titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        // Offer 详情
        String courseName = getCourseName(offer.getModuleCode());
        JLabel courseLabel = new JLabel("Course: " + courseName);
        courseLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        courseLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(courseLabel);
        
        JLabel hoursLabel = new JLabel("Weekly Hours: " + offer.getOfferedHours());
        hoursLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        hoursLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(hoursLabel);
        
        // 添加 offer 发送时间信息（使用 offeredAt 替代不存在的 expiryDate）
        if (offer.getOfferedAt() != null) {
            JLabel offeredLabel = new JLabel("Offered Date: " + 
                offer.getOfferedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            offeredLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            offeredLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            panel.add(offeredLabel);
        }
        
        panel.add(Box.createVerticalStrut(20));
        
        JLabel questionLabel = new JLabel("Do you accept this offer?");
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        questionLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(questionLabel);
        
        int result = JOptionPane.showConfirmDialog(parent, panel, 
            "Offer Decision", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        boolean success;
        if (result == JOptionPane.YES_OPTION) {
            success = acceptOfferWithFeedback(offer.getOfferId(), parent);
            if (success && onSuccess != null) {
                onSuccess.run();
            }
        } else if (result == JOptionPane.NO_OPTION) {
            success = rejectOfferWithFeedback(offer.getOfferId(), parent);
            if (success && onSuccess != null) {
                onSuccess.run();
            }
        }
    }
    
    /**
     * 处理所有待处理的 Offers
     */
    public void handlePendingOffers(Long taUserId, JFrame parent, Runnable onSuccess) {
        List<MOOffer> pendingOffers = getPendingOffers(taUserId);
        for (MOOffer offer : pendingOffers) {
            handleOfferWithDialog(offer, parent, onSuccess);
        }
    }
    
    /**
     * 拒绝最新的 Offer
     */
    public boolean rejectLatestOffer(Long taUserId, JFrame parent) {
        MOOffer latest = getLatestOffer(taUserId);
        if (latest == null) {
            JOptionPane.showMessageDialog(parent, "No offers available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return rejectOfferWithFeedback(latest.getOfferId(), parent);
    }
    
    /**
     * 获取 Offer 统计
     */
    public OfferStats getOfferStats(Long taUserId) {
        List<MOOffer> offers = offerService.listByTaUserId(taUserId);
        
        long sent = offers.stream()
                .filter(o -> "SENT".equals(o.getStatus()))
                .count();
        long accepted = offers.stream()
                .filter(o -> "ACCEPTED".equals(o.getStatus()))
                .count();
        long rejected = offers.stream()
                .filter(o -> "REJECTED".equals(o.getStatus()))
                .count();
        
        return new OfferStats(sent, accepted, rejected);
    }
    
    /**
     * 根据课程代码获取课程名称
     */
    private String getCourseName(String moduleCode) {
        List<MOJob> jobs = jobService.listPublishedJobs();
        for (MOJob job : jobs) {
            if (job.getModuleCode().equals(moduleCode)) {
                return job.getModuleCode() + " - " + job.getTitle();
            }
        }
        return moduleCode;
    }
    
    /**
     * Offer 统计内部类
     */
    public static class OfferStats {
        public final long sent;
        public final long accepted;
        public final long rejected;
        
        public OfferStats(long sent, long accepted, long rejected) {
            this.sent = sent;
            this.accepted = accepted;
            this.rejected = rejected;
        }
        
        public long getTotal() {
            return sent + accepted + rejected;
        }
    }
}