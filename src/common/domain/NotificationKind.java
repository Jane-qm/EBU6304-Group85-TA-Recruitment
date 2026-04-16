package common.domain;

/**
 * Values for {@link common.entity.NotificationMessage#setType(String)} — Iteration 2 hooks.
 */
public final class NotificationKind {

    private NotificationKind() {
    }

    public static final String WAITLISTED    = "APPLICATION_WAITLISTED";
    public static final String RESULT        = "APPLICATION_RESULT";
    public static final String OFFER_RESPONSE = "OFFER_RESPONSE";
    public static final String OFFER_REJECTED = "OFFER_REJECTED";
}
