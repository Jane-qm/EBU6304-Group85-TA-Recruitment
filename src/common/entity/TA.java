package common.entity;

/**
 * 助教实体类
 * 继承 User，代表申请助教岗位的学生
 * 
 * @author Can Chen
 * @version 1.0
 */
public class TA extends User {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造函数 - 创建助教用户
     * 
     * @param email 邮箱
     * @param password 密码
     */
    public TA(String email, String password) {
        super(email, password, UserRole.TA);
    }
}