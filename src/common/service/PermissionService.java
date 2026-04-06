package common.service;

import common.entity.UserRole;
/**
 * 权限控制服务
 * 用于校验不同角色对各界面的访问权限
 */
public class PermissionService {

    /**
     * 校验用户是否有访问目标页面的权限
     * 规则说明：
     * 1. ADMIN 具有最高权限，可访问所有界面。
     * 2. TA 仅限访问 TA 界面，MO 仅限访问 MO 界面。
     *
     * @param userRole 用户的实际角色
     * @param targetRole 目标页面所属的角色要求
     * @return true 表示允许访问, false 表示拒绝访问
     *
     * @author Yiping Zheng
     */
    public static boolean hasAccess(UserRole userRole, UserRole targetRole) {
        if (userRole == null) {
            return false;
        }
        // Admin 拥有所有界面的访问权限
        if (userRole == UserRole.ADMIN) {
            return true;
        }
        // 其他角色必须与目标页面的角色严格匹配
        return userRole == targetRole;
    }
}