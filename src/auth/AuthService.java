package auth;

import common.entity.AccountStatus;
import common.entity.User;
import common.entity.UserRole;
import common.service.UserService;

/**
 * Authentication service for user login, registration, and account status management.
 * Handles input validation and delegates business logic to UserService.
 */
public class AuthService {
    // Singleton instance of UserService for data operations
    private static final UserService USER_SERVICE = new UserService();

    /**
     * Register a new user with email, password, and role
     * @param email user email address
     * @param password user login password
     * @param role user role (TA, MO, ADMIN)
     * @return registered User object or null if failed
     */
    public User register(String email, String password, UserRole role) {
        validateEmail(email);
        validatePassword(password);
        return USER_SERVICE.register(email, password, role);
    }

    /**
     * Authenticate user login with credentials
     * @param email user email
     * @param password user password
     * @return logged-in User object or null if authentication fails
     */
    public User login(String email, String password) {
        validateEmail(email);
        validatePassword(password);
        return USER_SERVICE.login(email, password);
    }

    /**
     * Check if the user account is active and valid for use
     * @param user target user to check
     * @return true if valid, false otherwise
     */
    public boolean isAccountValid(User user) {
        return user != null && user.getStatus() == AccountStatus.ACTIVE;
    }

    /**
     * Get human-readable status message for user interface display
     * @param user target user
     * @return status description string
     */
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

    /**
     * Check if an email is already registered in the system
     * @param email email to verify
     * @return true if exists, false otherwise
     */
    public boolean checkEmailExists(String email) {
        validateEmail(email);
        return USER_SERVICE.emailExists(email);
    }

    /**
     * Reset password for an existing user account
     * @param email user email
     * @param newPassword new password to set
     */
    public void resetPassword(String email, String newPassword) {
        validateEmail(email);
        validatePassword(newPassword);
        USER_SERVICE.updatePassword(email, newPassword);
    }

    /**
     * Validate email format and non-empty value
     * @param email input email to validate
     * @throws IllegalArgumentException if email is invalid
     */
    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty.");
        }
        String normalized = email.trim();
        if (!normalized.contains("@") || normalized.startsWith("@") || normalized.endsWith("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    /**
     * Validate password is not null or blank
     * @param password input password to validate
     * @throws IllegalArgumentException if password is invalid
     */
    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty.");
        }
    }
}
