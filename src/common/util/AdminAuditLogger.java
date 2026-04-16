package common.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ADM-004.2: Appends every admin operation to {@code data/admin_audit.log}.
 *
 * Log format (one line per action):
 *   [YYYY-MM-DD HH:mm:ss] ADMIN=<email>  ACTION=<action>  TARGET=<target>
 *
 * The file is created automatically under {@code data/} if it does not exist.
 * All writes are append-only so no previous entry is ever lost.
 */
public final class AdminAuditLogger {

    private static final Path LOG_FILE = Path.of("data", "admin_audit.log");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AdminAuditLogger() {}

    /**
     * Records a single admin action.
     *
     * @param adminEmail the email of the admin who performed the action
     * @param action     a short verb or phrase describing what was done
     *                   (e.g. "APPROVE_MO", "DISABLE_ACCOUNT", "RESET_PASSWORD", "SAVE_CYCLE")
     * @param target     the email / ID / label of the affected resource
     */
    public static void log(String adminEmail, String action, String target) {
        String line = String.format("[%s]  ADMIN=%-30s  ACTION=%-22s  TARGET=%s%n",
                LocalDateTime.now().format(FMT),
                adminEmail == null ? "unknown" : adminEmail,
                action == null ? "" : action,
                target == null ? "" : target);
        try {
            Files.createDirectories(LOG_FILE.getParent());
            Files.writeString(LOG_FILE, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[AdminAuditLogger] Failed to write audit log: " + e.getMessage());
        }
    }
}
