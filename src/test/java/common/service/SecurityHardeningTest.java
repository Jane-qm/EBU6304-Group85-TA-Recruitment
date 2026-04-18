package common.service;

import common.entity.User;
import common.entity.UserRole;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Minimal security regression tests for Iteration 3 hardening.
 */
class SecurityHardeningTest {

    @Test
    void passwordService_legacySha256_isStillAcceptedAndFlaggedForUpgrade() {
        String raw = "LegacyPass123";
        String legacy = sha256(raw);

        assertTrue(PasswordService.verify(raw, legacy), "Legacy SHA-256 hash should still verify.");
        assertTrue(PasswordService.needsUpgrade(legacy), "Legacy hash should be marked for upgrade.");
    }

    @Test
    void login_withLegacyHash_upgradesToPbkdf2() {
        UserService service = new UserService();
        String email = "sec.upgrade." + UUID.randomUUID() + "@qmul.ac.uk";
        String raw = "StrongPass123";

        service.register(email, raw, UserRole.TA);
        User user = service.findByEmail(email);
        assertNotNull(user);

        // Force legacy hash to simulate old persisted accounts.
        user.setPasswordHash(sha256(raw));
        service.saveUser(user);

        User loggedIn = service.login(email, raw);
        assertNotNull(loggedIn, "Login should succeed for legacy account.");
        assertFalse(PasswordService.needsUpgrade(loggedIn.getPasswordHash()),
                "Hash should be transparently upgraded to PBKDF2 after successful login.");
        assertTrue(loggedIn.getPasswordHash().startsWith("PBKDF2$"),
                "Stored hash should now be PBKDF2 formatted.");
    }

    @Test
    void login_fiveFailures_locksAccountFor15Minutes() {
        UserService service = new UserService();
        String email = "sec.lock." + UUID.randomUUID() + "@qmul.ac.uk";
        String raw = "LockPass123";

        service.register(email, raw, UserRole.TA);

        for (int i = 0; i < 5; i++) {
            assertNull(service.login(email, "wrong-password-" + i));
        }
        User user = service.findByEmail(email);
        assertNotNull(user);
        assertNotNull(user.getLockedUntil(), "Account should be locked after 5 failed attempts.");
        assertNull(service.login(email, raw), "Correct password should still be blocked while locked.");
    }

    @Test
    void ensureDefaultAdmin_withoutEnvVar_doesNotAutoCreateAdmin() throws Exception {
        assumeTrue(System.getenv("TA_SYSTEM_ADMIN_BOOTSTRAP_PASSWORD") == null
                        || System.getenv("TA_SYSTEM_ADMIN_BOOTSTRAP_PASSWORD").isBlank(),
                "Env var set in runner, skip negative bootstrap test.");

        UserService service = new UserService();

        // Remove admin@test.com from in-memory map via reflection for this test.
        Field mapField = UserService.class.getDeclaredField("usersByEmail");
        mapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, User> map = (Map<String, User>) mapField.get(service);
        User backup = map.remove("admin@test.com");

        try {
            service.ensureDefaultAdmin();
            assertNull(service.findByEmail("admin@test.com"),
                    "Without env var, ensureDefaultAdmin must not auto-create admin.");
        } finally {
            // Restore in-memory state to avoid impacting subsequent tests in same JVM.
            if (backup != null) {
                map.put("admin@test.com", backup);
            }
        }
    }

    private static String sha256(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashBytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

