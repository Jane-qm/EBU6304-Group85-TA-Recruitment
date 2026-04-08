package auth;

import common.entity.User;
import common.entity.UserRole;
import common.service.UserService;

/**
 * Authentication service.
 * Handles registration, login, password reset, and account checks.
 *
 * @version 2.0
 * @contributor Jiaze Wang
 * @update
 * - Refined registration flow to keep role-based account status logic inside UserService
 * - Removed duplicated status override during registration
 * - Kept authentication responsibilities focused on validation and routing support
 */
public class AuthService {

    private static final UserService USER_SERVICE = new UserService();

    /** School email suffix enforced on registration only (demo accounts may use other domains for login). */
    private static final String UNIVERSITY_DOMAIN = "@qmul.ac.uk";

    public User register(String email, String password, UserRole role) {
        validateEmail(email);
        validatePassword(password);
        String normalized = email.trim();
        if (!normalized.toLowerCase().endsWith(UNIVERSITY_DOMAIN.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Only university email is allowed (e.g. user" + UNIVERSITY_DOMAIN + ").");
        }

        // Keep role-based status logic inside UserService.
        return USER_SERVICE.register(normalized, password, role);
    }

    public User login(String email, String password) {
        validateEmail(email);
        validatePassword(password);
        return USER_SERVICE.login(email, password);
    }

    public boolean isAccountValid(User user) {
        return user != null && user.getStatus() == common.entity.AccountStatus.ACTIVE;
    }

    public String getAccountStatusMessage(User user) {
        if (user == null) {
            return "User not found.";
        }
        return switch (user.getStatus()) {
            case ACTIVE -> "Account is active.";
            case PENDING -> "Account is pending approval. Please wait for admin review.";
            case DISABLED -> "Account is disabled. Please contact administrator.";
        };
    }

    public boolean sendVerificationCode(String email) {
        if (!USER_SERVICE.emailExists(email)) {
            return false;
        }
        System.out.println("Verification code sent to: " + email);
        return true;
    }

    public boolean checkEmailExists(String email) {
        validateEmail(email);
        return USER_SERVICE.emailExists(email);
    }

    public void resetPassword(String email, String newPassword) {
        validateEmail(email);
        validatePassword(newPassword);
        USER_SERVICE.updatePassword(email, newPassword);
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty.");
        }

        String normalized = email.trim();
        if (!normalized.contains("@") || normalized.startsWith("@") || normalized.endsWith("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }
}