package common.entity;

import common.service.PasswordService;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户抽象类
 * 所有用户类型（TA、MO、Admin）的基类
 *
 * @author Can Chen
 * @author Zhixuan Guo
 * @version 2.0
 */
public abstract class User implements Serializable {
    
    private static final long serialVersionUID = 2L;
    
    /** 用户唯一标识ID */
    private Long userId;
    
    /** 用户邮箱（登录账号） */
    private String email;

    /**
     * 用户显示姓名（各角色共用）。
     * 注册时为空，可在完善个人资料时填写。
     *
     * @author Zhixuan Guo
     */
    private String name;
    
    /** 加密后的密码哈希值 */
    private String passwordHash;
    
    /** 用户角色（TA/MO/ADMIN） */
    private UserRole role;
    
    /** 账号状态（ACTIVE/PENDING/DISABLED） */
    private AccountStatus status;
    
    /** 账号创建时间 */
    private LocalDateTime createdAt;
    
    /** 最后登录时间 */
    private LocalDateTime lastLogin;
    
    /**
     * 构造函数 - 创建新用户
     * 
     * @param email 用户邮箱
     * @param password 明文密码（会自动加密）
     * @param role 用户角色
     */
    public User(String email, String password, UserRole role) {
        this.email = email;
        this.passwordHash = PasswordService.hash(password);
        this.role = role;
        // MO注册后需要管理员激活，TA和ADMIN直接激活
        this.status = (role == UserRole.MO) ? AccountStatus.PENDING : AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.lastLogin = null;
    }
    
    /**
     * 验证密码是否正确
     * 
     * @param password 待验证的明文密码
     * @return true=密码正确，false=密码错误
     */
    public boolean checkPassword(String password) {
        return PasswordService.verify(password, this.passwordHash);
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
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public AccountStatus getStatus() {
        return status;
    }
    
    public void setStatus(AccountStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * @author Zhixuan Guo
     */
    public String getName() {
        return name;
    }

    /**
     * @author Zhixuan Guo
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                '}';
    }
}