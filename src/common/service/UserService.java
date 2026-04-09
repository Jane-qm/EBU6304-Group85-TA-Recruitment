package common.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import common.dao.UserFileDAO;
import common.entity.AccountStatus;
import common.entity.Admin;
import common.entity.MO;
import common.entity.TA;
import common.entity.User;
import common.entity.UserRole;

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
 */
public class UserService {
    
    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100000L);
    private final UserFileDAO fileDAO = new UserFileDAO();

    public UserService() {
        loadFromFile();
        if (usersByEmail.isEmpty()) {
            seedDemoUsers();
            saveToFile();
        }
    }

    /**
     * 从文件加载用户数据
     */
    private void loadFromFile() {
        try {
            List<User> users = fileDAO.loadAll();
            for (User user : users) {
                String normalizedEmail = normalizeEmail(user.getEmail());
                usersByEmail.put(normalizedEmail, user);
                // 更新 ID 生成器，确保新用户 ID 不重复
                if (user.getUserId() != null && user.getUserId() > idGenerator.get()) {
                    idGenerator.set(user.getUserId());
                }
            }
            System.out.println("加载用户数据成功，共 " + users.size() + " 个用户");
        } catch (Exception e) {
            System.err.println("加载用户数据失败: " + e.getMessage());
        }
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
        // MO 需要管理员激活，TA 和 ADMIN 直接激活
        if (role == UserRole.MO) {
            user.setStatus(AccountStatus.PENDING);
        } else {
            user.setStatus(AccountStatus.ACTIVE);
        }
        usersByEmail.put(normalizedEmail, user);
        saveToFile();  // 保存到文件
        return user;
    }

    /**
     * 用户登录
     */
    public User login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        User user = usersByEmail.get(normalizedEmail);
        if (user == null || !user.checkPassword(password)) {
            return null;
        }
        user.setLastLogin(LocalDateTime.now());
        saveToFile();  // 更新最后登录时间后保存
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
        saveToFile();  // 保存到文件
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
     * 添加演示用户
     */
    private void seedDemoUsers() {
        // 演示 TA 用户（状态 ACTIVE）
        User activeTa = new TA("ta@test.com", "123456");
        activeTa.setUserId(idGenerator.incrementAndGet());
        activeTa.setStatus(AccountStatus.ACTIVE);
        usersByEmail.put(normalizeEmail(activeTa.getEmail()), activeTa);

        // 演示 MO 用户（状态 PENDING，需管理员激活）
        User pendingMo = new MO("mo@test.com", "123456");
        pendingMo.setUserId(idGenerator.incrementAndGet());
        pendingMo.setStatus(AccountStatus.PENDING);
        usersByEmail.put(normalizeEmail(pendingMo.getEmail()), pendingMo);
        
        // 演示管理员用户
        User admin = new Admin("admin@test.com", "admin123");
        admin.setUserId(idGenerator.incrementAndGet());
        admin.setStatus(AccountStatus.ACTIVE);
        usersByEmail.put(normalizeEmail(admin.getEmail()), admin);
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
        usersByEmail.put(user.getEmail(), user);
        saveToFile();
    }
    /**
    * 【新增】：返回所有用户列表
    * 解决 MOApplicantReviewPanel 报 listAll() 找不到的问题
    */
    public List<User> listAll() {
        // 将 Map 中的所有 User 对象转换为 List 返回
        return new java.util.ArrayList<>(usersByEmail.values());
    }

/**
 * 【新增】：根据用户 ID 获取用户对象（更高效的版本）
 * 方便在 ReviewPanel 中直接通过 ID 获取 TA 的姓名和邮箱
 */
    public User getUserById(Long userId) {
        if (userId == null) return null;
        // 遍历现有的内存 Map
        for (User user : usersByEmail.values()) {
            if (userId.equals(user.getUserId())) {
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
     * Validates the strict super-admin rule.
     * Only admin@test.com with ACTIVE status can enter the admin portal.
     */
    public boolean isStrictAdmin(User user) {
        return user != null
                && user.getRole() == UserRole.ADMIN
                && "admin@test.com".equalsIgnoreCase(user.getEmail())
                && user.getStatus() == AccountStatus.ACTIVE;
    }

    /**
     * Returns all users sorted by user ID.
     */
    public List<User> listAllUsers() {
        return usersByEmail.values().stream()
                .sorted((a, b) -> {
                    Long aId = a.getUserId() == null ? 0L : a.getUserId();
                    Long bId = b.getUserId() == null ? 0L : b.getUserId();
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
}
/**
 * 【新增】：获取所有申请者（TA角色）的列表
 */
    public List<User> getAllTAs() {
        return listAll().stream()
                .filter(u -> u.getRole() == UserRole.TA)
                .collect(java.util.stream.Collectors.toList());
    }
}
