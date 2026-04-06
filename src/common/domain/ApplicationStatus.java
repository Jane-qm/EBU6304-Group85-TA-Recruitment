package common.domain;

import java.util.Set;

public final class ApplicationStatus {

    private ApplicationStatus() {}

    // 申请状态常量
    public static final String PENDING_REVIEW = "PENDING_REVIEW";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String WAITLISTED = "WAITLISTED";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";
    public static final String HIRED = "HIRED";
    public static final String CANCELLED = "CANCELLED";

    private static final Set<String> AWAITING_REVIEW = Set.of(PENDING_REVIEW, SUBMITTED);
    
    // 可取消的状态
    private static final Set<String> CANCELLABLE = Set.of(PENDING_REVIEW, SUBMITTED, WAITLISTED);

    public static boolean isAwaitingReview(String status) {
        return status != null && AWAITING_REVIEW.contains(status);
    }

    public static boolean isCancellable(String status) {
        return status != null && CANCELLABLE.contains(status);
    }

    public static boolean isCancelled(String status) {
        return CANCELLED.equals(status);
    }

    public static boolean isRejected(String status) {
        return REJECTED.equals(status);
    }

    public static boolean isHired(String status) {
        return HIRED.equals(status);
    }

    // ========== 新增：isAccepted 方法 ==========
    public static boolean isAccepted(String status) {
        return ACCEPTED.equals(status);
    }

    // ========== UI 显示辅助方法 ==========
    
    public static String getDisplayText(String status) {
        if (status == null) return "unknown";
        if (HIRED.equals(status) || ACCEPTED.equals(status)) return "accepted";
        if (isAwaitingReview(status)) return "pending";
        if (WAITLISTED.equals(status)) return "waitlisted";
        if (REJECTED.equals(status)) return "rejected";
        if (CANCELLED.equals(status)) return "cancelled";
        return status.toLowerCase();
    }

    public static String getFeedbackMessage(String status) {
        if (status == null) return "";
        switch (status) {
            case SUBMITTED:
            case PENDING_REVIEW:
                return "Your application is being reviewed.";
            case WAITLISTED:
                return "You have been waitlisted. You may be contacted if a position opens.";
            case ACCEPTED:
                return "Your application has been accepted. Offer incoming.";
            case HIRED:
                return "Congratulations! You have been hired.";
            case REJECTED:
                return "Position filled by another candidate.";
            case CANCELLED:
                return "You cancelled this application.";
            default:
                return "";
        }
    }
}