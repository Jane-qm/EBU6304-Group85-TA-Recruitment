package modules.auth;

import modules.user.AccountStatus;
import modules.user.TA;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceAccountStatusTest {

    private final AuthService auth = new AuthService();

    @Test
    void getAccountStatusMessage_nullUser() {
        assertEquals("User not found.", auth.getAccountStatusMessage(null));
    }

    @Test
    void getAccountStatusMessage_reflectsStatus() {
        TA ta = new TA("status.msg@qmul.ac.uk", "Password123");
        ta.setStatus(AccountStatus.ACTIVE);
        assertTrue(auth.getAccountStatusMessage(ta).toLowerCase().contains("active"));
        ta.setStatus(AccountStatus.PENDING);
        assertTrue(auth.getAccountStatusMessage(ta).toLowerCase().contains("pending"));
        ta.setStatus(AccountStatus.DISABLED);
        assertTrue(auth.getAccountStatusMessage(ta).toLowerCase().contains("disabled"));
    }

    @Test
    void isAccountValid_requiresActive() {
        TA ta = new TA("status.valid@qmul.ac.uk", "Password123");
        assertTrue(auth.isAccountValid(ta));
        ta.setStatus(AccountStatus.PENDING);
        assertFalse(auth.isAccountValid(ta));
        assertFalse(auth.isAccountValid(null));
    }
}
