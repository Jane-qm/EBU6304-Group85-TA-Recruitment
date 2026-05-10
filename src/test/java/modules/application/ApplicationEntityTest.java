package modules.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure logic on {@link Application} (offer expiry / respond eligibility).
 */
class ApplicationEntityTest {

    @Test
    void isOfferExpired_whenExpiryNull_isFalse() {
        Application app = new Application();
        app.setOfferExpiryAt(null);
        assertFalse(app.isOfferExpired());
    }

    @Test
    void isOfferExpired_whenExpiryInPast_isTrue() {
        Application app = new Application();
        app.setOfferExpiryAt(LocalDateTime.now().minusMinutes(1));
        assertTrue(app.isOfferExpired());
    }

    @Test
    void isOfferExpired_whenExpiryInFuture_isFalse() {
        Application app = new Application();
        app.setOfferExpiryAt(LocalDateTime.now().plusDays(7));
        assertFalse(app.isOfferExpired());
    }

    @Test
    void canRespondToOffer_requiresOfferSent_andNotExpired() {
        Application app = new Application();
        app.setStatus(ApplicationStatus.OFFER_SENT);
        app.setOfferExpiryAt(LocalDateTime.now().plusHours(1));
        assertTrue(app.canRespondToOffer());
    }

    @Test
    void canRespondToOffer_falseWhenWrongStatus() {
        Application app = new Application();
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setOfferExpiryAt(LocalDateTime.now().plusHours(1));
        assertFalse(app.canRespondToOffer());
    }

    @Test
    void canRespondToOffer_falseWhenExpired() {
        Application app = new Application();
        app.setStatus(ApplicationStatus.OFFER_SENT);
        app.setOfferExpiryAt(LocalDateTime.now().minusSeconds(1));
        assertFalse(app.canRespondToOffer());
    }
}
