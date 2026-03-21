package common.entity;

/**
 * 用户角色枚举
 * 定义系统中的用户角色类型
 * 
 * @author Can Chen
 * @version 1.0
 */
public enum UserRole {
    /** 助教 - 可以申请职位 */
    TA,
    /** 教师 - 管理课程 */
    MO,
    /** 管理员 - 系统管理 */
    ADMIN
}