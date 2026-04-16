package common.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApplicationStatus pure utility methods.
 * No file I/O — tests cover all status-classification helpers and display text.
 */
class ApplicationStatusTest {

    // ── isAwaitingReview ──────────────────────────────────────────────────────

    @Test
    void pendingReview_isAwaitingReview() {
        assertTrue(ApplicationStatus.isAwaitingReview(ApplicationStatus.PENDING_REVIEW));
    }

    @Test
    void submitted_isAwaitingReview() {
        assertTrue(ApplicationStatus.isAwaitingReview(ApplicationStatus.SUBMITTED));
    }

    @Test
    void waitlisted_isNotAwaitingReview() {
        assertFalse(ApplicationStatus.isAwaitingReview(ApplicationStatus.WAITLISTED));
    }

    @Test
    void hired_isNotAwaitingReview() {
        assertFalse(ApplicationStatus.isAwaitingReview(ApplicationStatus.HIRED));
    }

    @Test
    void null_isNotAwaitingReview() {
        assertFalse(ApplicationStatus.isAwaitingReview(null));
    }

    // ── isCancellable ─────────────────────────────────────────────────────────

    @Test
    void pendingReview_isCancellable() {
        assertTrue(ApplicationStatus.isCancellable(ApplicationStatus.PENDING_REVIEW));
    }

    @Test
    void submitted_isCancellable() {
        assertTrue(ApplicationStatus.isCancellable(ApplicationStatus.SUBMITTED));
    }

    @Test
    void waitlisted_isCancellable() {
        assertTrue(ApplicationStatus.isCancellable(ApplicationStatus.WAITLISTED));
    }

    @Test
    void hired_isNotCancellable() {
        assertFalse(ApplicationStatus.isCancellable(ApplicationStatus.HIRED));
    }

    @Test
    void rejected_isNotCancellable() {
        assertFalse(ApplicationStatus.isCancellable(ApplicationStatus.REJECTED));
    }

    @Test
    void cancelled_isNotCancellable() {
        assertFalse(ApplicationStatus.isCancellable(ApplicationStatus.CANCELLED));
    }

    // ── terminal states ───────────────────────────────────────────────────────

    @Test
    void isCancelled_matchesCancelledOnly() {
        assertTrue(ApplicationStatus.isCancelled(ApplicationStatus.CANCELLED));
        assertFalse(ApplicationStatus.isCancelled(ApplicationStatus.SUBMITTED));
        assertFalse(ApplicationStatus.isCancelled(null));
    }

    @Test
    void isRejected_matchesRejectedOnly() {
        assertTrue(ApplicationStatus.isRejected(ApplicationStatus.REJECTED));
        assertFalse(ApplicationStatus.isRejected(ApplicationStatus.ACCEPTED));
    }

    @Test
    void isHired_matchesHiredOnly() {
        assertTrue(ApplicationStatus.isHired(ApplicationStatus.HIRED));
        assertFalse(ApplicationStatus.isHired(ApplicationStatus.ACCEPTED));
    }

    @Test
    void isAccepted_matchesAcceptedOnly() {
        assertTrue(ApplicationStatus.isAccepted(ApplicationStatus.ACCEPTED));
        assertFalse(ApplicationStatus.isAccepted(ApplicationStatus.HIRED));
    }

    // ── getDisplayText ────────────────────────────────────────────────────────

    @Test
    void displayText_hired_returnsAccepted() {
        assertEquals("accepted", ApplicationStatus.getDisplayText(ApplicationStatus.HIRED));
    }

    @Test
    void displayText_accepted_returnsAccepted() {
        assertEquals("accepted", ApplicationStatus.getDisplayText(ApplicationStatus.ACCEPTED));
    }

    @Test
    void displayText_submitted_returnsPending() {
        assertEquals("pending", ApplicationStatus.getDisplayText(ApplicationStatus.SUBMITTED));
    }

    @Test
    void displayText_waitlisted_returnsWaitlisted() {
        assertEquals("waitlisted", ApplicationStatus.getDisplayText(ApplicationStatus.WAITLISTED));
    }

    @Test
    void displayText_rejected_returnsRejected() {
        assertEquals("rejected", ApplicationStatus.getDisplayText(ApplicationStatus.REJECTED));
    }

    @Test
    void displayText_cancelled_returnsCancelled() {
        assertEquals("cancelled", ApplicationStatus.getDisplayText(ApplicationStatus.CANCELLED));
    }

    @Test
    void displayText_null_returnsUnknown() {
        assertEquals("unknown", ApplicationStatus.getDisplayText(null));
    }
}
