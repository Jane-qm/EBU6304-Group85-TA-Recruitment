package modules.auth;

import modules.user.AccountStatus;
import modules.user.User;
import modules.user.UserRole;
import modules.user.UserService;
import modules.profile.TAProfileService;

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
 *
 * @version 2.1
 * @contributor Jiaze Wang
 * @update
 * - Initialized a blank TA profile immediately after TA registration
 * - Reduced the chance of stale profile data being shown for newly registered TA accounts
 */
public class AuthService {

    private static final UserService USER_SERVICE = UserService.getInstance();
    private static final TAProfileService TA_PROFILE_SERVICE = new TAProfileService();

    /** Accepted school email domains enforced on registration. */
    private static final String[] ALLOWED_DOMAINS = {"@qmul.ac.uk", "@bupt.edu.cn"};

    public User register(String email, String password, UserRole role) {
        validateEmail(email);
        validatePassword(password);
        String normalized = email.trim().toLowerCase();
        boolean domainOk = false;
        for (String domain : ALLOWED_DOMAINS) {
            if (normalized.endsWith(domain)) {
                domainOk = true;
                break;
            }
        }
        if (!domainOk) {
            throw new IllegalArgumentException(
                    "Only university emails are allowed (e.g. user@qmul.ac.uk or user@bupt.edu.cn).");
        }

        if (role != UserRole.TA) {
            throw new IllegalArgumentException("Only TA (Teaching Assistant) self-registration is allowed.");
        }

        User user = USER_SERVICE.register(email.trim(), password, role);

        // Create a blank TA profile as soon as a TA account is created.
        if (user != null && user.getRole() == UserRole.TA) {
            TA_PROFILE_SERVICE.initializeProfile(user.getUserId(), user.getEmail());
        }

        return user;
    }

    public User login(String email, String password) {
        validateEmail(email);
        validatePassword(password);
        return USER_SERVICE.login(email, password);
    }

    public boolean isAccountValid(User user) {
        return user != null && user.getStatus() == AccountStatus.ACTIVE;
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

    public boolean isPasswordChangeRequired(String email) {
        validateEmail(email);
        return USER_SERVICE.isPasswordChangeRequired(email);
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