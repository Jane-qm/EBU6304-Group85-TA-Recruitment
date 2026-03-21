package auth;

import common.entity.User;
import common.entity.UserRole;
import common.entity.AccountStatus;
import common.service.UserService;

/**
 * 认证服务
 * 提供统一的注册、登录入口，衔接 UI 层与业务层
 * 
 * @author Can Chen
 * @version 1.0
 */
public class AuthService {
    
    private UserService userService;
    
    /**
     * 构造函数
     * 初始化用户业务服务
     */
    public AuthService() {
        this.userService = new UserService();
    }
    
    /**
     * 用户注册
     * 
     * @param email 邮箱
     * @param password 密码
     * @param role 用户角色
     * @return 注册成功的用户对象，失败返回 null
     */
    public User register(String email, String password, UserRole role) {
        // 参数校验
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }
        
        // 调用业务层
        return userService.register(email, password, role);
    }
    
    /**
     * 用户登录
     * 
     * @param email 邮箱
     * @param password 密码
     * @return 登录成功的用户对象，失败返回 null
     */
    public User login(String email, String password) {
        // 参数校验
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 调用业务层
        return userService.login(email, password);
    }
    
    /**
     * 检查账号状态（登录后额外校验）
     * 
     * @param user 用户对象
     * @return true=可正常使用，false=账号异常
     */
    public boolean isAccountValid(User user) {
        if (user == null) {
            return false;
        }
        
        switch (user.getStatus()) {
            case ACTIVE:
                return true;
            case PENDING:
                return false;  // 待激活
            case DISABLED:
                return false;  // 已禁用
            default:
                return false;
        }
    }
    
    /**
     * 获取账号状态描述（用于 UI 提示）
     * 
     * @param user 用户对象
     * @return 状态描述
     */
    public String getAccountStatusMessage(User user) {
        if (user == null) {
            return "用户不存在";
        }
        
        switch (user.getStatus()) {
            case ACTIVE:
                return "账号正常";
            case PENDING:
                return "账号待激活，请联系管理员";
            case DISABLED:
                return "账号已被禁用，请联系管理员";
            default:
                return "未知状态";
        }
    }
}