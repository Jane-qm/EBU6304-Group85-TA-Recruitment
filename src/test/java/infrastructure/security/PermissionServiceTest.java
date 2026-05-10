package infrastructure.security;

import modules.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RBAC defaults / matrix behaviour (reloads file-backed config before each test).
 */
class PermissionServiceTest {

    @BeforeEach
    void reloadMatrix() {
        PermissionService.reload();
    }

    @Test
    void admin_canAccessAllPortals() {
        assertTrue(PermissionService.hasAccess(UserRole.ADMIN, UserRole.ADMIN));
        assertTrue(PermissionService.hasAccess(UserRole.ADMIN, UserRole.MO));
        assertTrue(PermissionService.hasAccess(UserRole.ADMIN, UserRole.TA));
    }

    @Test
    void mo_onlyMoPortal() {
        assertTrue(PermissionService.hasAccess(UserRole.MO, UserRole.MO));
        assertFalse(PermissionService.hasAccess(UserRole.MO, UserRole.ADMIN));
        assertFalse(PermissionService.hasAccess(UserRole.MO, UserRole.TA));
    }

    @Test
    void ta_onlyTaPortal() {
        assertTrue(PermissionService.hasAccess(UserRole.TA, UserRole.TA));
        assertFalse(PermissionService.hasAccess(UserRole.TA, UserRole.MO));
        assertFalse(PermissionService.hasAccess(UserRole.TA, UserRole.ADMIN));
    }

    @Test
    void nullRoles_denied() {
        assertFalse(PermissionService.hasAccess(null, UserRole.TA));
        assertFalse(PermissionService.hasAccess(UserRole.TA, null));
    }
}
