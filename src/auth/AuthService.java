package auth;

import common.entity.User;
import common.entity.UserRole;
import common.service.UserService;
import ta.service.TAProfileService;

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

    private static final UserService USER_SERVICE = new UserService();
    private static final TAProfileService TA_PROFILE_SERVICE = new TAProfileService();

    public User register(String account, String password, UserRole role) {
        validateAccountForRegistration(account, role);
        validatePassword(password);

        String normalized = AccountRules.normalizeAccount(account);

        // Keep role-based status logic inside UserService.
        User user = USER_SERVICE.register(normalized, password, role);

        // Create a blank TA profile as soon as a TA account is created.
        if (user != null && user.getRole() == UserRole.TA) {
            TA_PROFILE_SERVICE.initializeProfile(user.getUserId(), user.getEmail());
        }

        return user;
    }

    public User login(String account, String password) {
        validateAccountForLogin(account);
        validatePassword(password);
        return USER_SERVICE.login(AccountRules.normalizeAccount(account), password);
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

    public boolean sendVerificationCode(String account) {
        if (!USER_SERVICE.emailExists(account)) {
            return false;
        }
        System.out.println("Verification code sent to: " + account);
        return true;
    }

    public boolean checkEmailExists(String account) {
        validateAccountForLogin(account);
        return USER_SERVICE.emailExists(account);
    }

    public void resetPassword(String account, String newPassword) {
        validateAccountForLogin(account);
        validatePassword(newPassword);
        USER_SERVICE.updatePassword(account, newPassword);
    }

    public boolean isPasswordChangeRequired(String account) {
        validateAccountForLogin(account);
        return USER_SERVICE.isPasswordChangeRequired(account);
    }

    private static void validateAccountForLogin(String account) {
        if (account == null || account.isBlank()) {
            throw new IllegalArgumentException("Account must not be empty.");
        }
        if (!AccountRules.isValidLoginAccount(account)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
    }

    private static void validateAccountForRegistration(String account, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }
        if (role == UserRole.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be registered here.");
        }

        validateAccountForLogin(account);
        String normalized = AccountRules.normalizeAccount(account);

        if (role == UserRole.TA) {
            if (!AccountRules.isValidTaEmail(normalized)) {
                throw new IllegalArgumentException(
                        "TA registration requires a 10-digit @bupt.edu.cn or 9-digit @qmul.ac.uk email."
                );
            }
            return;
        }

        if (!AccountRules.isValidEmail(normalized)) {
            throw new IllegalArgumentException("MO registration requires a valid email address.");
        }
        if ("admin@test.com".equalsIgnoreCase(normalized)) {
            throw new IllegalArgumentException("Admin accounts cannot be registered here.");
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
