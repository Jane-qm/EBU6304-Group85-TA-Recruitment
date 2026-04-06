package common.domain;

import java.util.Set;

/**
 * Iteration 2 application lifecycle (TA ↔ MO). Persisted as strings in JSON.
 * <p>
 * Teammates: wire MO review UI to transition e.g. PENDING_REVIEW → WAITLISTED / ACCEPTED / REJECTED.
 */
public final class ApplicationStatus {

    private ApplicationStatus() {
    }

    /** New application awaiting MO review (requirement: “Pending Review”). */
    public static final String PENDING_REVIEW = "PENDING_REVIEW";

    /** Legacy demo value — treated same as {@link #PENDING_REVIEW} for MO queues. */
    public static final String SUBMITTED = "SUBMITTED";

    public static final String WAITLISTED = "WAITLISTED";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";
    public static final String CANCELLED = "CANCELLED";

    /** Post-offer / hiring (existing demo). */
    public static final String HIRED = "HIRED";

    private static final Set<String> AWAITING_REVIEW = Set.of(
            PENDING_REVIEW,
            SUBMITTED
    );

    public static boolean isAwaitingReview(String status) {
        if (status == null) {
            return false;
        }
        return AWAITING_REVIEW.contains(status);
    }

    public static boolean isCancelled(String status) {
        return CANCELLED.equalsIgnoreCase(status);
    }

    public static boolean isRejected(String status) {
        return REJECTED.equalsIgnoreCase(status);
    }
}
