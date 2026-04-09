package ta.controller;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import common.entity.MOOffer;
import common.service.MOOfferService;
import common.service.NotificationService;


public class TAOfferController {
    
    private final MOOfferService offerService;
    private final NotificationService notificationService;
    
    public TAOfferController() {
        this.offerService = new MOOfferService();
        this.notificationService = new NotificationService();
    }
    
    /**
     * 获取 TA 的所有 Offer
     */
    public List<MOOffer> getMyOffers(Long taUserId) {
        return offerService.listByTaUserId(taUserId);
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