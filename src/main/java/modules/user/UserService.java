package modules.user;

import infrastructure.time.TimeProvider;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import infrastructure.audit.AuthAuditLogger;
import infrastructure.security.PasswordService;

/**
 * 用户业务服务
 * 负责用户注册、登录等核心业务逻辑
 *
 * @author Can Chen
 * @version 2.0
 *
 * @version 2.1
 * @contributor Jiaze Wang
 * @update
 * - Added strict super-admin validation support
 * - Improved email normalization during user updates
 *
 * @version 2.2
 * @contributor Jiaze Wang
 * @update
 * - Added admin-oriented account lifecycle management methods
 * - Added user listing support for admin operations
 * - Added account status update, approval, disable, and password reset methods
 *
 * @version 2.3
 * @contributor Jiaze Wang
 * @update
 * - Fixed duplicated methods and broken class structure after merge
 * - Kept legacy helper methods used by existing panels
 * - Restored a compilable and compatible UserService implementation
 *
 * @version 2.4
 * @contributor Jiaze Wang
 * @update
 * - Defined the dual seeded admin access policy for Admin Portal access
 * - Restricted strict admin access to active ADMIN users with approved seeded admin emails
 * - Added safe seeded admin repair without promoting non-admin users automatically
 */
public class UserService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_MINUTES = 15;

    /** Approved seeded system-admin emails for Admin Portal access. */
    private static final Set<String> APPROVED_ADMIN_EMAILS = Set.of(
            "admin@qmul.ac.uk",
            "admin@bupt.edu.cn"
    );

    /** Temporary constructor password is overwritten immediately by a persisted password hash. */
    private static final String REPAIR_PLACEHOLDER_PASSWORD = "temporary_seed_repair_password";

    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100000L);
    private final UserDAO fileDAO;

    private static volatile UserService instance;

    /** Shared in-memory user store for the whole app (single load from disk). */
    public static UserService getInstance() {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    /**
     * Fresh service with its own map loaded from disk. For unit tests only;
     * does not replace {@link #getInstance()}.
     */
    public static UserService newInstanceForTesting() {
        return new UserService();
    }

    private UserService() {
        this(new UserDAO());
    }

    UserService(UserDAO fileDAO) {
        this.fileDAO = fileDAO;
        loadFromFile();
        ensureSeededAdminAccounts();
    }

    /**
     * 从文件加载用户数据
     */
    private void loadFromFile() {
        try {
            List<User> users = fileDAO.loadAll();

            // First pass: establish the true maximum ID
            for (User user : users) {
                Long uid = user.getUserId();
                if (uid != null && uid > idGenerator.get()) {
                    idGenerator.set(uid);
                }
            }

            // Second pass: load into map, reassigning any duplicate ID
            Set<Long> seenIds = new HashSet<>();
            boolean needsResave = false;
            for (User user : users) {
                String normalizedEmail = normalizeEmail(user.getEmail());
                Long uid = user.getUserId();
                if (uid != null && !seenIds.add(uid)) {
                    Long newId = idGenerator.incrementAndGet();
                    System.err.println("[UserService] WARNING: duplicate userId " + uid
                            + " for email " + user.getEmail()
                            + " — reassigned to " + newId);
                    user.setUserId(newId);
                    seenIds.add(newId);
                    needsResave = true;
                }
                usersByEmail.put(normalizedEmail, user);
            }

            if (needsResave) {
                saveToFile();
            }
        } catch (Exception e) {
            System.err.println("加载用户数据失败: " + e.getMessage());
        }
    }

    /**
     * Ensures approved seeded admin accounts are aligned with the access policy.
     * Missing approved admins may be created only by reusing an existing seeded
     * ADMIN password hash. Existing non-admin users with approved admin emails
     * are never promoted automatically.
     */
    private void ensureSeededAdminAccounts() {
        boolean needsResave = false;
        User passwordSource = findSeededAdminPasswordSource();

        for (String adminEmail : APPROVED_ADMIN_EMAILS) {
            User existing = usersByEmail.get(adminEmail);

            if (existing == null) {
                if (passwordSource == null) {
                    System.err.println("[UserService] WARNING: seeded admin account " + adminEmail
                            + " is missing and cannot be auto-created because no existing seeded admin password hash is available.");
                    continue;
                }
                Admin seededAdmin = buildSeededAdminFromSource(adminEmail, passwordSource);
                usersByEmail.put(adminEmail, seededAdmin);
                needsResave = true;
                continue;
            }

            if (existing.getRole() != UserRole.ADMIN) {
                System.err.println("[UserService] WARNING: approved seeded admin email exists with non-admin role and will not be promoted automatically: "
                        + adminEmail);
                continue;
            }

            if (existing.getStatus() != AccountStatus.ACTIVE) {
                existing.setStatus(AccountStatus.ACTIVE);
                needsResave = true;
            }
        }

        if (needsResave) {
            saveToFile();
        }
    }

    /**
     * Finds an existing approved ADMIN password hash to keep bootstrap
     * compatibility without hard-coding a new raw password.
     */
    private User findSeededAdminPasswordSource() {
        for (String adminEmail : APPROVED_ADMIN_EMAILS) {
            User user = usersByEmail.get(adminEmail);
            if (user != null
                    && user.getRole() == UserRole.ADMIN
                    && user.getPasswordHash() != null
                    && !user.getPasswordHash().isBlank()) {
                return user;
            }
        }
        return null;
    }

    /**
     * Builds a missing seeded admin while preserving the password policy from
     * another approved seeded admin account.
     */
    private Admin buildSeededAdminFromSource(String adminEmail, User source) {
        Admin admin = new Admin(adminEmail, REPAIR_PLACEHOLDER_PASSWORD);
        admin.setUserId(idGenerator.incrementAndGet());
        admin.setPasswordHash(source.getPasswordHash());
        admin.setStatus(AccountStatus.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setMustChangePassword(source.isMustChangePassword());
        admin.setFailedLoginCount(0);
        admin.setLockedUntil(null);
        return admin;
    }

    /**
     * 保存用户数据到文件
     */
    private void saveToFile() {
        try {
            fileDAO.saveAll(usersByEmail);
        } catch (Exception e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    public User register(String email, String password, UserRole role) {
        String normalizedEmail = normalizeEmail(email);
        if (usersByEmail.containsKey(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User user = createUser(normalizedEmail, password, role);
        user.setUserId(idGenerator.incrementAndGet());
        user.setStatus(AccountStatus.ACTIVE);

        usersByEmail.put(normalizedEmail, user);
        saveToFile();
        return user;
    }

    /**
     * 用户登录
     */
    public User login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        User user = usersByEmail.get(normalizedEmail);
        if (user == null) {
            AuthAuditLogger.logFailure(normalizedEmail, "email not found");
            return null;
        }

        if (user.getLockedUntil() != null && TimeProvider.now().isBefore(user.getLockedUntil())) {
            AuthAuditLogger.logFailure(normalizedEmail,
                    "account locked until " + user.getLockedUntil());
            return null;
        }

        if (!user.checkPassword(password)) {
            int failCount = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failCount);
            if (failCount >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLockedUntil(TimeProvider.now().plusMinutes(ACCOUNT_LOCK_MINUTES));
                user.setFailedLoginCount(0);
                AuthAuditLogger.logFailure(normalizedEmail,
                        "too many failures; locked for " + ACCOUNT_LOCK_MINUTES + " minutes");
            } else {
                AuthAuditLogger.logFailure(normalizedEmail, "invalid password");
            }
            saveToFile();
            return null;
        }

        // Successful login: clear lock counters and upgrade legacy hash
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (PasswordService.needsUpgrade(user.getPasswordHash())) {
            user.setPassword(password);
        }
        user.setLastLogin(TimeProvider.now());
        saveToFile();
        AuthAuditLogger.logSuccess(normalizedEmail, String.valueOf(user.getRole()));
        return user;
    }

    /**
     * 根据邮箱查找用户
     */
    public User findByEmail(String email) {
        return usersByEmail.get(normalizeEmail(email));
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean emailExists(String email) {
        return usersByEmail.containsKey(normalizeEmail(email));
    }

    /**
     * 更新密码
     */
    public void updatePassword(String email, String newPassword) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Email is not registered.");
        }
        user.setPassword(newPassword);
        user.setMustChangePassword(false);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        saveToFile();
    }

    public void saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }
        String normalizedEmail = normalizeEmail(user.getEmail());
        usersByEmail.put(normalizedEmail, user);
        saveToFile();
    }

    /**
     * 创建用户实例
     */
    private User createUser(String email, String password, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }
        return switch (role) {
            case TA -> new TA(email, password);
            case MO -> new MO(email, password);
            case ADMIN -> new Admin(email, password);
        };
    }

    /**
     * 标准化邮箱
     */
    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank.");
        }
        return email.trim().toLowerCase();
    }

    /**
     * 根据 ID 查找用户
     */
    public User findById(Long userId) {
        for (User user : usersByEmail.values()) {
            if (user.getUserId() != null && user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    /**
     * 更新用户信息
     */
    public void updateUser(User user) {
        if (user == null || user.getEmail() == null) {
            return;
        }
        usersByEmail.put(normalizeEmail(user.getEmail()), user);
        saveToFile();
    }

    /**
     * 返回所有用户列表
     */
    public List<User> listAll() {
        return new java.util.ArrayList<>(usersByEmail.values());
    }

    /**
     * 根据用户 ID 获取用户对象
     */
    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        for (User user : usersByEmail.values()) {
            if (userId.equals(user.getUserId())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Validates Admin Portal access through the centralized dual seeded admin policy.
     */
    public boolean isStrictAdmin(User user) {
        return user != null
                && user.getRole() == UserRole.ADMIN
                && user.getStatus() == AccountStatus.ACTIVE
                && APPROVED_ADMIN_EMAILS.contains(normalizeEmail(user.getEmail()));
    }

    /**
     * Returns all users sorted by user ID.
     */
    public List<User> listAllUsers() {
        return usersByEmail.values().stream()
                .sorted((a, b) -> {
                    // 修复空值警告
                    Long aId = a.getUserId() != null ? a.getUserId() : 0L;
                    Long bId = b.getUserId() != null ? b.getUserId() : 0L;
                    return aId.compareTo(bId);
                })
                .toList();
    }

    /**
     * Updates account status.
     */
    public void updateAccountStatus(String email, AccountStatus newStatus) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        user.setStatus(newStatus);
        saveUser(user);
    }

    /**
     * Approves an MO account.
     */
    public void approveMoAccount(String email) {
        User user = findByEmail(email);
        if (user == null || user.getRole() != UserRole.MO) {
            throw new IllegalArgumentException("MO account not found: " + email);
        }
        user.setStatus(AccountStatus.ACTIVE);
        saveUser(user);
    }

    /**
     * Disables an account.
     */
    public void disableAccount(String email) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        user.setStatus(AccountStatus.DISABLED);
        saveUser(user);
    }

    /**
     * Resets a user's password as an administrator action.
     */
    public void resetPasswordByAdmin(String email, String newPassword) {
        updatePassword(email, newPassword);
    }

    public boolean isPasswordChangeRequired(String email) {
        User user = findByEmail(email);
        return user != null && user.isMustChangePassword();
    }
    // modules/user/UserService.java

    /**
     * 批量导入 MO 账号的结果
     */
    public static class MOImportResult {
        public final int successCount;
        public final int failCount;
        public final List<String> errors;

        public MOImportResult(int successCount, int failCount, List<String> errors) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.errors = errors;
        }
    }

    /**
     * 从 CSV 文件路径批量导入 MO 账号
     * CSV 格式: email,password,name
     */
// modules/user/UserService.java

    public MOImportResult importMOFromCSV(String filePath) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // 跳过表头
                if (isFirstLine && line.toLowerCase().contains("email")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    failCount++;
                    errors.add("格式错误: " + line);
                    continue;
                }

                String email = parts[0].trim();
                String password = parts.length > 1 ? parts[1].trim() : "123456";
                String name = parts.length > 2 ? parts[2].trim() : "";

                if (!email.contains("@")) {
                    failCount++;
                    errors.add("邮箱格式错误: " + email);
                    continue;
                }

                try {
                    if (findByEmail(email) != null) {
                        failCount++;
                        errors.add("MO已存在: " + email);
                        continue;
                    }

                    // 注册MO账号
                    User user = register(email, password, UserRole.MO);

                    // 设置姓名
                    if (user instanceof MO && !name.isEmpty()) {
                        ((MO) user).setName(name);
                        saveUser(user);  // 保存更新后的用户
                    }

                    successCount++;

                } catch (Exception ex) {
                    failCount++;
                    errors.add(email + " - " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            return new MOImportResult(0, 1, List.of("读取文件失败: " + e.getMessage()));
        }

        return new MOImportResult(successCount, failCount, errors);
    }
}
