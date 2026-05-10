package modules.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ApplicationStatus} string-based helpers and UI copy.
 */
class ApplicationStatusTest {

    // ── isActive ──────────────────────────────────────────────────────────────

    @Test
    void submitted_waitlisted_offerSent_hired_areActive() {
        assertTrue(ApplicationStatus.isActive(ApplicationStatus.SUBMITTED));
        assertTrue(ApplicationStatus.isActive(ApplicationStatus.WAITLISTED));
        assertTrue(ApplicationStatus.isActive(ApplicationStatus.OFFER_SENT));
        assertTrue(ApplicationStatus.isActive(ApplicationStatus.HIRED));
    }

    @Test
    void rejected_cancelled_expired_areNotActive() {
        assertFalse(ApplicationStatus.isActive(ApplicationStatus.REJECTED));
        assertFalse(ApplicationStatus.isActive(ApplicationStatus.CANCELLED));
        assertFalse(ApplicationStatus.isActive(ApplicationStatus.EXPIRED));
        assertFalse(ApplicationStatus.isActive(null));
    }

    // ── isCancellable ─────────────────────────────────────────────────────────

    @Test
    void submitted_and_waitlisted_areCancellable() {
        assertTrue(ApplicationStatus.isCancellable(ApplicationStatus.SUBMITTED));
        assertTrue(ApplicationStatus.isCancellable(ApplicationStatus.WAITLISTED));
    }

    @Test
    void offerSent_isNotCancellable() {
        assertFalse(ApplicationStatus.isCancellable(ApplicationStatus.OFFER_SENT));
    }

    @Test
    void terminalStates_areNotCancellable() {
        assertFalse(ApplicationStatus.isCancellable(ApplicationStatus.HIRED));
        assertFalse(ApplicationStatus.isCancellable(ApplicationStatus.REJECTED));
        assertFalse(ApplicationStatus.isCancellable(ApplicationStatus.CANCELLED));
        assertFalse(ApplicationStatus.isCancellable(null));
    }

    // ── isRespondable ───────────────────────────────────────────────────────────

    @Test
    void offerSent_isRespondable() {
        assertTrue(ApplicationStatus.isRespondable(ApplicationStatus.OFFER_SENT));
    }

    @Test
    void otherStatuses_areNotRespondable() {
        assertFalse(ApplicationStatus.isRespondable(ApplicationStatus.SUBMITTED));
        assertFalse(ApplicationStatus.isRespondable(ApplicationStatus.WAITLISTED));
        assertFalse(ApplicationStatus.isRespondable(null));
    }

    // ── isTerminal ────────────────────────────────────────────────────────────

    @Test
    void hired_rejected_cancelled_expired_areTerminal() {
        assertTrue(ApplicationStatus.isTerminal(ApplicationStatus.HIRED));
        assertTrue(ApplicationStatus.isTerminal(ApplicationStatus.REJECTED));
        assertTrue(ApplicationStatus.isTerminal(ApplicationStatus.CANCELLED));
        assertTrue(ApplicationStatus.isTerminal(ApplicationStatus.EXPIRED));
    }

    @Test
    void inProgressStates_areNotTerminal() {
        assertFalse(ApplicationStatus.isTerminal(ApplicationStatus.SUBMITTED));
        assertFalse(ApplicationStatus.isTerminal(ApplicationStatus.WAITLISTED));
        assertFalse(ApplicationStatus.isTerminal(ApplicationStatus.OFFER_SENT));
        assertFalse(ApplicationStatus.isTerminal(null));
    }

    // ── individual predicates ─────────────────────────────────────────────────

    @Test
    void isHired_isRejected_isCancelled_matchConstants() {
        assertTrue(ApplicationStatus.isHired(ApplicationStatus.HIRED));
        assertFalse(ApplicationStatus.isHired(ApplicationStatus.SUBMITTED));

        assertTrue(ApplicationStatus.isRejected(ApplicationStatus.REJECTED));
        assertFalse(ApplicationStatus.isRejected(ApplicationStatus.HIRED));

        assertTrue(ApplicationStatus.isCancelled(ApplicationStatus.CANCELLED));
        assertFalse(ApplicationStatus.isCancelled(ApplicationStatus.SUBMITTED));

        assertTrue(ApplicationStatus.isExpired(ApplicationStatus.EXPIRED));
        assertFalse(ApplicationStatus.isExpired(ApplicationStatus.HIRED));

        assertTrue(ApplicationStatus.isOfferSent(ApplicationStatus.OFFER_SENT));
        assertTrue(ApplicationStatus.isWaitlisted(ApplicationStatus.WAITLISTED));
        assertTrue(ApplicationStatus.isSubmitted(ApplicationStatus.SUBMITTED));
    }

    // ── getDisplayText ─────────────────────────────────────────────────────────

    @Test
    void displayText_knownStatuses() {
        assertEquals("Submitted", ApplicationStatus.getDisplayText(ApplicationStatus.SUBMITTED));
        assertEquals("Waitlisted", ApplicationStatus.getDisplayText(ApplicationStatus.WAITLISTED));
        assertEquals("Offer Sent - Awaiting Response",
                ApplicationStatus.getDisplayText(ApplicationStatus.OFFER_SENT));
        assertEquals("Hired", ApplicationStatus.getDisplayText(ApplicationStatus.HIRED));
        assertEquals("Rejected", ApplicationStatus.getDisplayText(ApplicationStatus.REJECTED));
        assertEquals("Cancelled", ApplicationStatus.getDisplayText(ApplicationStatus.CANCELLED));
        assertEquals("Expired", ApplicationStatus.getDisplayText(ApplicationStatus.EXPIRED));
    }

    @Test
    void displayText_null_returnsUnknown() {
        assertEquals("Unknown", ApplicationStatus.getDisplayText(null));
    }

    @Test
    void displayText_unknownRole_lowercased() {
        assertEquals("custom", ApplicationStatus.getDisplayText("CUSTOM"));
    }

    // ── getShortDisplayText ────────────────────────────────────────────────────

    @Test
    void shortDisplayText_submitted_isPending() {
        assertEquals("Pending", ApplicationStatus.getShortDisplayText(ApplicationStatus.SUBMITTED));
        assertEquals("Offer Received",
                ApplicationStatus.getShortDisplayText(ApplicationStatus.OFFER_SENT));
    }

    @Test
    void shortDisplayText_null_returnsUnknown() {
        assertEquals("Unknown", ApplicationStatus.getShortDisplayText(null));
    }

    // ── getFeedbackMessage ─────────────────────────────────────────────────────

    @Test
    void feedbackMessage_eachKnownStatus_nonEmpty() {
        assertFalse(ApplicationStatus.getFeedbackMessage(ApplicationStatus.SUBMITTED).isBlank());
        assertFalse(ApplicationStatus.getFeedbackMessage(ApplicationStatus.HIRED).isBlank());
        assertFalse(ApplicationStatus.getFeedbackMessage(ApplicationStatus.OFFER_SENT).isBlank());
    }

    @Test
    void feedbackMessage_null_isEmpty() {
        assertEquals("", ApplicationStatus.getFeedbackMessage(null));
    }

    @Test
    void feedbackMessage_unknown_isEmpty() {
        assertEquals("", ApplicationStatus.getFeedbackMessage("UNKNOWN_STATUS"));
    }
}
