package common.entity;

/**
 * 课程教师实体类（Module Organizer）
 * 继承 User，代表发布招聘岗位的教师
 *
 * @author Can Chen
 * @author Zhixuan Guo
 * @version 2.0
 */
public class MO extends User {

    private static final long serialVersionUID = 2L;

    /**
     * 所在院系，例如 "School of Electronic Engineering and Computer Science"
     *
     * @author Zhixuan Guo
     */
    private String department;

    /**
     * 工号，用于与学校 HR 系统对接
     *
     * @author Zhixuan Guo
     */
    private String employeeId;

    // ==================== Constructors ====================

    /**
     * 最简构造函数，兼容现有注册流程。
     *
     * @param email    注册邮箱
     * @param password 明文密码（由父类加密）
     */
    public MO(String email, String password) {
        super(email, password, UserRole.MO);
    }

    /**
     * 完整构造函数。
     *
     * @param email      注册邮箱
     * @param password   明文密码
     * @param department 所在院系
     * @param employeeId 工号
     * @author Zhixuan Guo
     */
    public MO(String email, String password, String department, String employeeId) {
        super(email, password, UserRole.MO);
        this.department = department;
        this.employeeId = employeeId;
    }

    // ==================== Getters and Setters ====================

    /** @author Zhixuan Guo */
    public String getDepartment() {
        return department;
    }

    /** @author Zhixuan Guo */
    public void setDepartment(String department) {
        this.department = department;
    }

    /** @author Zhixuan Guo */
    public String getEmployeeId() {
        return employeeId;
    }

    /** @author Zhixuan Guo */
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    // ==================== Object Overrides ====================

    /**
     * @author Zhixuan Guo
     */
    @Override
    public String toString() {
        return "MO{" +
                "userId=" + getUserId() +
                ", email='" + getEmail() + '\'' +
                ", status=" + getStatus() +
                ", department='" + department + '\'' +
                ", employeeId='" + employeeId + '\'' +
                '}';
    }
}
