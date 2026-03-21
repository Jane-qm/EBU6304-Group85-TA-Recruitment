package common.entity;

/**
 * 管理员实体类
 * 继承 User，代表系统管理员
 * 
 * @author Can Chen
 * @version 1.0
 */
public class Admin extends User {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造函数 - 创建管理员用户
     * 
     * @param email 邮箱
     * @param password 密码
     */
    public Admin(String email, String password) {
        super(email, password, UserRole.ADMIN);
    }
}