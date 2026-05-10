package modules.user;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import infrastructure.persistence.JsonPersistenceManager;

/**
 * 用户数据访问对象
 * 负责用户数据的 JSON 文件读写操作
 *
 * @author Can Chen
 * @version 3.0 - 移除 TA 冗余字段的序列化
 * @version 3.1 - 添加 MO 姓名字段支持
 */
public class UserDAO {

    private final JsonPersistenceManager persistenceManager;

    public UserDAO() {
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
     * 支持 MO 姓名字段
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

        // 新增：MO 姓名（仅对 MO 角色有效）
        private String name;

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

            // 新增：保存 MO 姓名
            if (user instanceof MO) {
                row.name = ((MO) user).getName();
            }

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
                case MO -> {
                    MO mo = new MO(email, "temp_password");
                    // 新增：恢复 MO 姓名
                    if (name != null && !name.isBlank()) {
                        mo.setName(name);
                    }
                    yield mo;
                }
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

            return user;
        }
    }
}