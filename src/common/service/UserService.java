package common.service;

import common.entity.*;
import common.dao.UserFileDAO;
import java.time.LocalDateTime;

/**
 * 用户业务服务
 * 负责用户注册、登录等核心业务逻辑
 * 
 * @author Can Chen
 * @version 1.0
 */
public class UserService {
    
    private UserFileDAO userDAO;
    
    /**
     * 构造函数
     * 初始化 DAO 层
     */
    public UserService() {
        this.userDAO = new UserFileDAO();
    }
    
    // ==================== 注册业务 ====================
    
    /**
     * 用户注册
     * 
     * @param email 邮箱（必填，格式需符合邮箱规范）
     * @param password 密码（必填，长度至少6位）
     * @param role 用户角色（TA 或 MO）
     * @return 注册成功的用户对象，失败返回 null
     * 
     * 业务规则：
     * 1. 邮箱不能为空
     * 2. 密码不能为空且长度≥6
     * 3. 邮箱格式必须正确
     * 4. 邮箱不能重复注册
     * 5. TA 注册后状态为 ACTIVE（直接可用）
     * 6. MO 注册后状态为 PENDING（需管理员激活）
     */
    public User register(String email, String password, UserRole role) {
        // 1. 参数校验
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度至少6位");
        }
        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }
        
        // 2. 邮箱格式校验
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        
        // 3. 检查邮箱是否已注册
        User existingUser = userDAO.findByEmail(email);
        if (existingUser != null) {
            return null;  // 邮箱已存在
        }
        
        // 4. 根据角色创建用户
        User newUser;
        if (role == UserRole.TA) {
            newUser = new TA(email, password);
        } else if (role == UserRole.MO) {
            newUser = new MO(email, password);
        } else {
            throw new IllegalArgumentException("不支持的用户角色: " + role);
        }
        
        // 5. 生成用户ID（使用时间戳）
        newUser.setUserId(System.currentTimeMillis());
        
        // 6. 保存到文件
        try {
            userDAO.save(newUser);
        } catch (Exception e) {
            throw new RuntimeException("保存用户失败: " + e.getMessage(), e);
        }
        
        return newUser;
    }
    
    // ==================== 登录业务 ====================
    
    /**
     * 用户登录
     * 
     * @param email 邮箱
     * @param password 密码
     * @return 登录成功的用户对象，失败返回 null
     * 
     * 业务规则：
     * 1. 邮箱不能为空
     * 2. 密码不能为空
     * 3. 用户必须存在
     * 4. 密码必须正确
     * 5. 账号状态不能是 DISABLED（禁用）
     * 6. 登录成功后更新最后登录时间
     */
    public User login(String email, String password) {
        // 1. 参数校验
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 2. 查找用户
        User user = userDAO.findByEmail(email);
        
        // 3. 验证用户存在
        if (user == null) {
            return null;
        }
        
        // 4. 验证密码
        if (!user.checkPassword(password)) {
            return null;
        }
        
        // 5. 验证账号状态
        if (user.getStatus() == AccountStatus.DISABLED) {
            return null;
        }
        
        // 6. 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        try {
            userDAO.update(user);
        } catch (Exception e) {
            // 登录时间更新失败不影响登录，只记录日志
            System.err.println("更新登录时间失败: " + e.getMessage());
        }
        
        return user;
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱地址
     * @return true=格式正确，false=格式错误
     */
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        // 简单邮箱格式校验：包含@和.，且不以@或.开头结尾
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}