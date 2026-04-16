package auth;

import common.entity.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AuthService registration and login validation.
 *
 * Tests that fail on domain / format checks are pure (no file I/O).
 * Tests that reach UserService require the data directory to exist;
 * Maven Surefire runs from the project root where data/ is already present.
 */
class AuthServiceTest {

    private final AuthService authService = new AuthService();

    // ── email domain validation ───────────────────────────────────────────────

    /**
     * A qmul.ac.uk address should pass domain check.
     * Registration may succeed or fail with "Email already registered" — never with
     * "Only university emails are allowed".
     */
    @Test
    void register_withQmulDomain_doesNotThrowDomainError() {
        try {
            authService.register("test.junit.qmul.x@qmul.ac.uk", "Password123", UserRole.TA);
            // registration succeeded — fine
        } catch (IllegalArgumentException ex) {
            assertFalse(ex.getMessage().contains("Only university"),
                    "Should not fail on domain; got: " + ex.getMessage());
        }
    }

    @Test
    void register_withBuptDomain_doesNotThrowDomainError() {
        try {
            authService.register("test.junit.bupt.y@bupt.edu.cn", "Password123", UserRole.TA);
        } catch (IllegalArgumentException ex) {
            assertFalse(ex.getMessage().contains("Only university"),
                    "Should not fail on domain; got: " + ex.getMessage());
        }
    }

    @Test
    void register_withGmailDomain_throwsDomainError() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("student@gmail.com", "Password1", UserRole.TA));
        assertTrue(ex.getMessage().contains("Only university"),
                "Expected domain error, got: " + ex.getMessage());
    }

    @Test
    void register_withArbitraryDomain_throwsDomainError() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("hacker@evil.io", "Password1", UserRole.TA));
        assertTrue(ex.getMessage().contains("Only university"));
    }

    // ── email format validation ───────────────────────────────────────────────

    @Test
    void register_withEmptyEmail_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register("", "Password1", UserRole.TA));
    }

    @Test
    void register_withNullEmail_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(null, "Password1", UserRole.TA));
    }

    @Test
    void register_withEmailMissingAtSign_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register("nodomain", "Password1", UserRole.TA));
    }

    // ── password validation ───────────────────────────────────────────────────

    @Test
    void register_withShortPassword_throwsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("a@qmul.ac.uk", "abc", UserRole.TA));
        assertTrue(ex.getMessage().toLowerCase().contains("password") ||
                   ex.getMessage().toLowerCase().contains("6"),
                "Expected password-length error; got: " + ex.getMessage());
    }

    @Test
    void register_withNullPassword_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register("a@qmul.ac.uk", null, UserRole.TA));
    }

    @Test
    void register_withBlankPassword_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register("a@qmul.ac.uk", "      ", UserRole.TA));
    }

    // ── login validation ──────────────────────────────────────────────────────

    @Test
    void login_withNullEmail_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.login(null, "Password1"));
    }

    @Test
    void login_withBlankEmail_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.login("", "Password1"));
    }

    @Test
    void login_withUnregisteredEmail_returnsNull() {
        assertNull(authService.login("nobody@qmul.ac.uk", "Password1"),
                "Login with unknown email should return null");
    }

    @Test
    void login_withWrongPassword_returnsNull() {
        // demo@qmul.ac.uk is expected NOT to be in the seed data; result is just null
        assertNull(authService.login("nobody@qmul.ac.uk", "WrongPass"));
    }
}
