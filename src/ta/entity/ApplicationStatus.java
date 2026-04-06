// ta/entity/ApplicationStatus.java
package ta.entity;

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

    // 待审核状态集合
    private static final Set<String> AWAITING_REVIEW = Set.of(PENDING_REVIEW, SUBMITTED);
    
    // 可取消的状态集合
    private static final Set<String> CANCELLABLE = Set.of(PENDING_REVIEW, SUBMITTED, WAITLISTED);

    // 判断是否为待审核状态
    public static boolean isAwaitingReview(String status) {
        return status != null && AWAITING_REVIEW.contains(status);
    }

    // 判断是否可以取消
    public static boolean isCancellable(String status) {
        return status != null && CANCELLABLE.contains(status);
    }

    // 判断是否为已取消
    public static boolean isCancelled(String status) {
        return CANCELLED.equals(status);
    }

    // 判断是否为已拒绝
    public static boolean isRejected(String status) {
        return REJECTED.equals(status);
    }

    // 判断是否为已录用
    public static boolean isHired(String status) {
        return HIRED.equals(status);
    }

    // 判断是否为已接受（发Offer前）
    public static boolean isAccepted(String status) {
        return ACCEPTED.equals(status);
    }

    // 判断是否为候补
    public static boolean isWaitlisted(String status) {
        return WAITLISTED.equals(status);
    }

    // 获取UI显示文本
    public static String getDisplayText(String status) {
        if (status == null) return "unknown";
        
        if (isHired(status) || isAccepted(status)) {
            return "accepted";
        }
        if (isAwaitingReview(status)) {
            return "pending";
        }
        if (isWaitlisted(status)) {
            return "waitlisted";
        }
        if (isRejected(status)) {
            return "rejected";
        }
        if (isCancelled(status)) {
            return "cancelled";
        }
        return status.toLowerCase();
    }

    // 获取反馈信息
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