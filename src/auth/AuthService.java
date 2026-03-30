package auth;

import common.entity.AccountStatus;
import common.entity.User;
import common.entity.UserRole;
import common.service.UserService;



public class AuthService {
    private static final UserService USER_SERVICE = new UserService();
    public User register(String email, String password, UserRole role) {
        validateEmail(email);
        validatePassword(password);
        return USER_SERVICE.register(email, password, role);
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
    }
}
