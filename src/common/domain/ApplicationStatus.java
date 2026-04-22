package common.domain;

import java.util.Set;

/**
 * 申请状态常量
 * 
 * @version 3.0 - 简化状态，合并 Offer 到 Application
 */
public final class ApplicationStatus {

    private ApplicationStatus() {}

    // ==================== 状态常量 ====================
    
    /** 已提交 - TA 完成申请，等待 MO 处理 */
    public static final String SUBMITTED = "SUBMITTED";
    
    /** 候选 - MO 放入候选池 */
    public static final String WAITLISTED = "WAITLISTED";
    
    /** Offer 已发送 - MO 接受并发 Offer，等待 TA 响应 */
    public static final String OFFER_SENT = "OFFER_SENT";
    
    /** 已录用 - TA 接受 Offer */
    public static final String HIRED = "HIRED";
    
    /** 已拒绝 - MO 拒绝 或 TA 拒绝 Offer */
    public static final String REJECTED = "REJECTED";
    
    /** 已取消 - TA 主动取消（截止日前） */
    public static final String CANCELLED = "CANCELLED";
    
    /** 已过期 - 职位关闭后未处理 */
    public static final String EXPIRED = "EXPIRED";

    // ==================== 状态集合 ====================
    
    /** 活跃申请状态（计入 3 个上限） */
    private static final Set<String> ACTIVE = Set.of(
        SUBMITTED, WAITLISTED, OFFER_SENT
    );
    
    /** 可取消状态（截止日期前） */
    private static final Set<String> CANCELLABLE = Set.of(
        SUBMITTED, WAITLISTED
    );
    
    /** 可响应 Offer 的状态 */
    private static final Set<String> RESPONDABLE = Set.of(OFFER_SENT);
    
    /** 最终状态（不可再变更） */
    private static final Set<String> TERMINAL = Set.of(
        HIRED, REJECTED, CANCELLED, EXPIRED
    );

    // ==================== 判断方法 ====================
    
    public static boolean isActive(String status) {
        return status != null && ACTIVE.contains(status);
    }
    
    public static boolean isCancellable(String status) {
        return status != null && CANCELLABLE.contains(status);
    }
    
    public static boolean isRespondable(String status) {
        return status != null && RESPONDABLE.contains(status);
    }
    
    public static boolean isTerminal(String status) {
        return status != null && TERMINAL.contains(status);
    }
    
    public static boolean isHired(String status) {
        return HIRED.equals(status);
    }
    
    public static boolean isRejected(String status) {
        return REJECTED.equals(status);
    }
    
    public static boolean isCancelled(String status) {
        return CANCELLED.equals(status);
    }
    
    public static boolean isExpired(String status) {
        return EXPIRED.equals(status);
    }
    
    public static boolean isOfferSent(String status) {
        return OFFER_SENT.equals(status);
    }
    
    public static boolean isWaitlisted(String status) {
        return WAITLISTED.equals(status);
    }
    
    public static boolean isSubmitted(String status) {
        return SUBMITTED.equals(status);
    }

    // ==================== UI 显示方法 ====================
    
    public static String getDisplayText(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case SUBMITTED: return "Submitted";
            case WAITLISTED: return "Waitlisted";
            case OFFER_SENT: return "Offer Sent - Awaiting Response";
            case HIRED: return "Hired";
            case REJECTED: return "Rejected";
            case CANCELLED: return "Cancelled";
            case EXPIRED: return "Expired";
            default: return status.toLowerCase();
        }
    }
    
    public static String getShortDisplayText(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case SUBMITTED: return "Pending";
            case WAITLISTED: return "Waitlisted";
            case OFFER_SENT: return "Offer Received";
            case HIRED: return "Hired";
            case REJECTED: return "Rejected";
            case CANCELLED: return "Cancelled";
            case EXPIRED: return "Expired";
            default: return status.toLowerCase();
        }
    }

    public static String getFeedbackMessage(String status) {
        if (status == null) return "";
        switch (status) {
            case SUBMITTED:
                return "Your application has been submitted and is pending review.";
            case WAITLISTED:
                return "You have been placed on the waitlist. You may be contacted if a position opens.";
            case OFFER_SENT:
                return "An offer has been sent. Please accept or reject it before the deadline.";
            case HIRED:
                return "Congratulations! You have been hired as a TA.";
            case REJECTED:
                return "Your application was not successful.";
            case CANCELLED:
                return "You cancelled this application.";
            case EXPIRED:
                return "The application period has ended.";
            default:
                return "";
        }
    }
}