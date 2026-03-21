package common.entity;

/**
 * 课程教师实体类
 * 继承 User，代表发布招聘岗位的教师
 * 
 * @author Can Chen
 * @version 1.0
 */
public class MO extends User {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造函数 - 创建教师用户
     * 
     * @param email 邮箱
     * @param password 密码
     */
    public MO(String email, String password) {
        super(email, password, UserRole.MO);
    }
}