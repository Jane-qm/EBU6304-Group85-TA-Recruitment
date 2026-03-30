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
                usersByEmail.put(user.getEmail(), user);
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
        usersByEmail.put(activeTa.getEmail(), activeTa);

        // 演示 MO 用户（状态 PENDING，需管理员激活）
        User pendingMo = new MO("mo@test.com", "123456");
        pendingMo.setUserId(idGenerator.incrementAndGet());
        pendingMo.setStatus(AccountStatus.PENDING);
        usersByEmail.put(pendingMo.getEmail(), pendingMo);
        
        // 演示管理员用户
        User admin = new Admin("admin@test.com", "admin123");
        admin.setUserId(idGenerator.incrementAndGet());
        admin.setStatus(AccountStatus.ACTIVE);
        usersByEmail.put(admin.getEmail(), admin);
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
}