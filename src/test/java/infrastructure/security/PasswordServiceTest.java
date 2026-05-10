package infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for password hashing and verification (no UserService / file I/O).
 */
class PasswordServiceTest {

    @Test
    void hash_then_verify_succeeds() {
        String raw = "CorrectHorseBatteryStaple9";
        String stored = PasswordService.hash(raw);
        assertTrue(PasswordService.verify(raw, stored));
        assertFalse(PasswordService.verify("wrong", stored));
    }

    @Test
    void hash_startsWithPbkdf2Prefix() {
        String stored = PasswordService.hash("AnyPass123");
        assertTrue(stored.startsWith("PBKDF2$"));
        assertFalse(PasswordService.needsUpgrade(stored));
    }

    @Test
    void hash_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> PasswordService.hash(null));
    }

    @Test
    void verify_nullArguments_returnsFalse() {
        assertFalse(PasswordService.verify(null, "PBKDF2$120000$AAAA$BBBB"));
        assertFalse(PasswordService.verify("x", null));
    }

    @Test
    void verify_malformedPbkdf2_returnsFalse() {
        assertFalse(PasswordService.verify("pass", "PBKDF2$not$valid"));
    }

    @Test
    void needsUpgrade_plainSha256_returnsTrue() {
        assertTrue(PasswordService.needsUpgrade("e7cf3ef4f1c8531cdfc21bbfc891ac54be92e803"));
    }
}
