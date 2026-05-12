package infrastructure.security;

import modules.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionServiceTest {

    @BeforeEach
    void setUp() {
        PermissionService.reload();
    }

    @Test
    void hasAccess_WhenAdminTargetsAllPortals_ReturnsTrue() {
        // 测试场景：ADMIN 访问所有门户，预期均允许访问
        // Given
        UserRole userRole = UserRole.ADMIN;

        // When
        boolean adminPortal = PermissionService.hasAccess(userRole, UserRole.ADMIN);
        boolean moPortal = PermissionService.hasAccess(userRole, UserRole.MO);
        boolean taPortal = PermissionService.hasAccess(userRole, UserRole.TA);

        // Then
        assertTrue(adminPortal);
        assertTrue(moPortal);
        assertTrue(taPortal);
    }

    @Test
    void hasAccess_WhenMoTargetsAdminOrTaPortal_ReturnsFalse() {
        // 测试场景：MO 访问非 MO 门户，预期拒绝访问
        // Given
        UserRole userRole = UserRole.MO;

        // When
        boolean adminPortal = PermissionService.hasAccess(userRole, UserRole.ADMIN);
        boolean taPortal = PermissionService.hasAccess(userRole, UserRole.TA);

        // Then
        assertFalse(adminPortal);
        assertFalse(taPortal);
    }

    @Test
    void hasAccess_WhenMoTargetsMoPortal_ReturnsTrue() {
        // 测试场景：MO 访问 MO 门户，预期允许访问
        // Given
        UserRole userRole = UserRole.MO;

        // When
        boolean result = PermissionService.hasAccess(userRole, UserRole.MO);

        // Then
        assertTrue(result);
    }

    @Test
    void hasAccess_WhenTaTargetsTaPortal_ReturnsTrue() {
        // 测试场景：TA 访问 TA 门户，预期允许访问
        // Given
        UserRole userRole = UserRole.TA;

        // When
        boolean result = PermissionService.hasAccess(userRole, UserRole.TA);

        // Then
        assertTrue(result);
    }

    @Test
    void hasAccess_WhenTaTargetsMoOrAdminPortal_ReturnsFalse() {
        // 测试场景：TA 访问 MO 或 ADMIN 门户，预期拒绝访问
        // Given
        UserRole userRole = UserRole.TA;

        // When
        boolean moPortal = PermissionService.hasAccess(userRole, UserRole.MO);
        boolean adminPortal = PermissionService.hasAccess(userRole, UserRole.ADMIN);

        // Then
        assertFalse(moPortal);
        assertFalse(adminPortal);
    }

    @Test
    void hasAccess_WhenAnyRoleIsNull_ReturnsFalse() {
        // 测试场景：用户角色或目标角色为空，预期拒绝访问
        // Given
        UserRole nullRole = null;

        // When
        boolean nullUserRole = PermissionService.hasAccess(nullRole, UserRole.TA);
        boolean nullTargetRole = PermissionService.hasAccess(UserRole.TA, nullRole);

        // Then
        assertFalse(nullUserRole);
        assertFalse(nullTargetRole);
    }
}
