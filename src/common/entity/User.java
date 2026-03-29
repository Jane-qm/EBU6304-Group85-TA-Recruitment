package common.entity;

import common.service.PasswordService;

import java.time.LocalDateTime;

/**
 * 用户抽象类
 * 所有用户类型（TA、MO、Admin）的基类
 * 
 * @author Can Chen
 * @version 2.0
 */
public abstract class User {
    private Long userId;
    private final String email;
    private String passwordHash;
    private final UserRole role;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    protected User(String email, String password, UserRole role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }

        this.email = email.trim();
        this.passwordHash = PasswordService.hash(password);
        this.role = role;
        // MO 需要管理员激活，TA 和 ADMIN 直接激活
        this.status = (role == UserRole.MO) ? AccountStatus.PENDING : AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.lastLogin = null;
    }

    /**
     * 验证密码是否正确
     */
    public boolean checkPassword(String password) {
        if (password == null) {
            return false;
        }
        return PasswordService.verify(password, this.passwordHash);
    }

    /**
     * 设置新密码
     */
    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }
        this.passwordHash = PasswordService.hash(password);
    }

    // ==================== Getters and Setters ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 仅用于从持久化数据恢复哈希值，不应在业务层直接调用。
     */
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank.");
        }
        this.passwordHash = passwordHash;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Created time must not be null.");
        }
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}