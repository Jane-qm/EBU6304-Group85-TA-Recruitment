package common.entity;

/**
 * 账户状态枚举
 * 定义账户的当前状态
 * 
 * @author Can Chen
 * @version 1.0
 */
public enum AccountStatus {
    /** 正常 - 可以登录并使用系统 */
    ACTIVE,
    
    /** 待激活 - MO注册后需管理员激活 */
    PENDING,
    
    /** 禁用 - 被管理员禁用，无法登录 */
    DISABLED
}