package common.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Security audit logger for authentication events.
 *
 * <p>Writes append-only entries to {@code data/auth_audit.log}.
 * No plaintext password is ever written.
 */
public final class AuthAuditLogger {
    private static final Path LOG_FILE = Path.of("data", "auth_audit.log");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuthAuditLogger() {
    }

    public static void logSuccess(String email, String role) {
        write("LOGIN_SUCCESS", email, "role=" + safe(role));
    }

    public static void logFailure(String email, String reason) {
        write("LOGIN_FAILURE", email, safe(reason));
    }

    private static void write(String action, String email, String details) {
        String line = String.format("[%s] ACTION=%s EMAIL=%s DETAILS=%s%n",
                LocalDateTime.now().format(FMT), safe(action), safe(email), safe(details));
        try {
            Files.createDirectories(LOG_FILE.getParent());
            Files.writeString(LOG_FILE, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[AuthAuditLogger] Failed to write auth log: " + e.getMessage());
        }
    }

    private static String safe(String value) {
        return value == null ? "unknown" : value.replaceAll("[\\r\\n\\t]", " ").trim();
    }
}

