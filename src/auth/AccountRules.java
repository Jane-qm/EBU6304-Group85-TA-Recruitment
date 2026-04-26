package auth;

import common.entity.UserRole;

/**
 * Centralized account normalization, role detection, and validation rules.
 */
public final class AccountRules {

    public enum DetectedRole {
        TA, MO, ADMIN, UNKNOWN, INVALID
    }

    private AccountRules() {
    }

    public static String buildAccount(String input, String suffix) {
        String value = input == null ? "" : input.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.contains("@")) {
            return normalizeAccount(value);
        }
        return normalizeAccount(value + (suffix == null ? "" : suffix));
    }

    public static String normalizeAccount(String account) {
        return account == null ? "" : account.trim().toLowerCase();
    }

    public static DetectedRole detectRole(String account) {
        String normalized = normalizeAccount(account);
        if (normalized.isEmpty()) {
            return DetectedRole.UNKNOWN;
        }
        if ("admin@test.com".equals(normalized)) {
            return DetectedRole.ADMIN;
        }
        if (isValidTaEmail(normalized)) {
            return DetectedRole.TA;
        }
        if (isValidEmail(normalized)) {
            return DetectedRole.MO;
        }
        return DetectedRole.INVALID;
    }

    public static UserRole toUserRole(DetectedRole detectedRole) {
        return switch (detectedRole) {
            case TA -> UserRole.TA;
            case MO -> UserRole.MO;
            case ADMIN -> UserRole.ADMIN;
            default -> null;
        };
    }

    public static boolean isValidLoginAccount(String account) {
        return isValidEmail(normalizeAccount(account));
    }

    public static boolean isValidEmail(String value) {
        return value != null
                && value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public static boolean isValidTaEmail(String value) {
        return value != null
                && (value.matches("^\\d{10}@bupt\\.edu\\.cn$")
                || value.matches("^\\d{9}@qmul\\.ac\\.uk$"));
    }
}
