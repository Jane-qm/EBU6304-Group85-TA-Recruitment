package modules.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression anchor for notification type strings used across services and UI.
 */
class NotificationKindTest {

    @Test
    void constants_areStableStrings() {
        assertFalse(NotificationKind.WAITLISTED.isBlank());
        assertFalse(NotificationKind.RESULT.isBlank());
        assertFalse(NotificationKind.OFFER_RESPONSE.isBlank());
        assertFalse(NotificationKind.OFFER_REJECTED.isBlank());
    }
}
