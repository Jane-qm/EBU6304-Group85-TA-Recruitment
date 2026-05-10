package modules.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests formatting helpers on {@link ApplicationService} that do not depend on external persistence for the code path used.
 */
class ApplicationServiceSummaryTest {

    private final ApplicationService service = new ApplicationService();

    @Test
    void buildApplicationSummary_null_returnsPlaceholder() {
        assertEquals("No application selected.", service.buildApplicationSummary(null));
    }

    @Test
    void buildApplicationSummary_includesIdStatusStatementAndOfferFields() {
        Application app = new Application();
        app.setApplicationId(9001L);
        app.setStatus(ApplicationStatus.OFFER_SENT);
        app.setStatement("I want to teach.");
        app.setOfferedHours(10);
        app.setOfferExpiryAt(LocalDateTime.of(2026, 6, 1, 23, 59));

        String summary = service.buildApplicationSummary(app);

        assertTrue(summary.contains("9001"));
        assertTrue(summary.contains("Offer Sent"));
        assertTrue(summary.contains("I want to teach."));
        assertTrue(summary.contains("10"));
        assertTrue(summary.contains("hours/week"));
        assertTrue(summary.contains("2026-06-01"));
    }

    @Test
    void buildApplicationSummary_omitsOptionalOfferLinesWhenUnset() {
        Application app = new Application();
        app.setApplicationId(42L);
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setStatement("Hello");

        String summary = service.buildApplicationSummary(app);

        assertTrue(summary.contains("42"));
        assertTrue(summary.contains("Submitted"));
        assertTrue(summary.contains("Hello"));
        assertFalse(summary.contains("Offered Hours"));
        assertFalse(summary.contains("Offer Expires"));
    }
}
