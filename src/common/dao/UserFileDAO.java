package common.dao;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import common.entity.AccountStatus;
import common.entity.Admin;
import common.entity.MO;
import common.entity.TA;
import common.entity.User;
import common.entity.UserRole;

/**
 * 用户数据访问对象
 * 负责用户数据的 JSON 文件读写操作
 * 
 * @author Can Chen
 * @version 3.0 - 移除 TA 冗余字段的序列化
 */
public class UserFileDAO {

    private final JsonPersistenceManager persistenceManager;

    public UserFileDAO() {
        this.persistenceManager = new JsonPersistenceManager();
        this.persistenceManager.initializeBaseFiles();
    }

    /**
     * 保存所有用户到文件
     * 
     * @param users 用户 Map
     */
    public void saveAll(Map<String, User> users) {
        if (users == null) {
            return;
        }

        List<PersistedUser> userList = new ArrayList<>();
        for (User user : users.values()) {
            userList.add(PersistedUser.fromEntity(user));
        }
        persistenceManager.writeList(JsonPersistenceManager.USERS_FILE, userList);
    }

    /**
     * 从文件加载所有用户
     * 
     * @return 用户列表
     */
    public List<User> loadAll() {
        try {
            Type listType = new TypeToken<List<PersistedUser>>() {}.getType();
            List<PersistedUser> rows = persistenceManager.readList(JsonPersistenceManager.USERS_FILE, listType);
            List<User> users = new ArrayList<>();
            for (PersistedUser row : rows) {
                users.add(row.toEntity());
            }
            return users;
        } catch (Exception e) {
            System.err.println("读取用户数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 用于 JSON 存储的中间结构
     * 修改：移除 TA 的冗余个人资料字段
     * 个人资料已移至 ta.entity.TAProfile 独立存储
     */
    private static class PersistedUser {
        private Long userId;
        private String email;
        private String passwordHash;
        private UserRole role;
        private AccountStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime lastLogin;
        private boolean mustChangePassword;
        private int failedLoginCount;
        private LocalDateTime lockedUntil;

        // 已移除所有 TA profile 相关字段：
        // name, major, grade, skillTags, availableWorkingHours, 
        // profileSaved, profileLastUpdated
        // 个人资料由 TAProfileService 通过 ta_profiles.json 独立管理

        public PersistedUser() {
        }

        static PersistedUser fromEntity(User user) {
            PersistedUser row = new PersistedUser();
            row.userId = user.getUserId();
            row.email = user.getEmail();
            row.passwordHash = user.getPasswordHash();
            row.role = user.getRole();
            row.status = user.getStatus();
            row.createdAt = user.getCreatedAt();
            row.lastLogin = user.getLastLogin();
            row.mustChangePassword = user.isMustChangePassword();
            row.failedLoginCount = user.getFailedLoginCount();
            row.lockedUntil = user.getLockedUntil();

            // 移除 TA profile 字段的序列化
            // 个人资料由 TAProfileService 独立管理

            return row;
        }

        User toEntity() {
            if (role == null) {
                throw new IllegalStateException("Invalid user data: role is missing.");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("Invalid user data: email is missing.");
            }
            if (passwordHash == null || passwordHash.isBlank()) {
                throw new IllegalStateException("Invalid user data: password hash is missing.");
            }

            User user = switch (role) {
                case TA -> new TA(email, "temp_password");
                case MO -> new MO(email, "temp_password");
                case ADMIN -> new Admin(email, "temp_password");
            };

            user.setPasswordHash(passwordHash);
            if (userId != null) {
                user.setUserId(userId);
            }
            if (status != null) {
                user.setStatus(status);
            }
            if (createdAt != null) {
                user.setCreatedAt(createdAt);
            }
            if (lastLogin != null) {
                user.setLastLogin(lastLogin);
            }
            user.setMustChangePassword(mustChangePassword);
            user.setFailedLoginCount(failedLoginCount);
            user.setLockedUntil(lockedUntil);
            
            // 移除 TA profile 字段的恢复代码
            // 个人资料由 TAProfileService 独立加载

            return user;
        }
    }
}