package infrastructure.security;

import modules.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionServiceTest {

    private static final Path PERMISSIONS_FILE = Path.of("data", "permissions.json");
    private String originalPermissionsContent;
    private boolean permissionsFileOriginallyExisted;

    @BeforeEach
    void setUp() throws IOException {
        permissionsFileOriginallyExisted = Files.exists(PERMISSIONS_FILE);
        if (permissionsFileOriginallyExisted) {
            originalPermissionsContent = Files.readString(PERMISSIONS_FILE);
        }
        PermissionService.reload();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (permissionsFileOriginallyExisted) {
            Files.writeString(PERMISSIONS_FILE, originalPermissionsContent);
        } else {
            Files.deleteIfExists(PERMISSIONS_FILE);
        }
        PermissionService.reload();
    }

    @Test
    void hasAccess_WhenAdminTargetsAllPortals_ReturnsTrue() throws IOException {
        // 测试场景：ADMIN 访问所有门户，预期均允许访问
        // Given
        Files.deleteIfExists(PERMISSIONS_FILE);
        PermissionService.reload();
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
    void hasAccess_WhenMoTargetsAdminOrTaPortal_ReturnsFalse() throws IOException {
        // 测试场景：MO 访问非 MO 门户，预期拒绝访问
        // Given
        Files.deleteIfExists(PERMISSIONS_FILE);
        PermissionService.reload();
        UserRole userRole = UserRole.MO;

        // When
        boolean adminPortal = PermissionService.hasAccess(userRole, UserRole.ADMIN);
        boolean taPortal = PermissionService.hasAccess(userRole, UserRole.TA);

        // Then
        assertFalse(adminPortal);
        assertFalse(taPortal);
    }

    @Test
    void hasAccess_WhenMoTargetsMoPortal_ReturnsTrue() throws IOException {
        // 测试场景：MO 访问 MO 门户，预期允许访问
        // Given
        Files.deleteIfExists(PERMISSIONS_FILE);
        PermissionService.reload();
        UserRole userRole = UserRole.MO;

        // When
        boolean result = PermissionService.hasAccess(userRole, UserRole.MO);

        // Then
        assertTrue(result);
    }

    @Test
    void hasAccess_WhenTaTargetsTaPortal_ReturnsTrue() throws IOException {
        // 测试场景：TA 访问 TA 门户，预期允许访问
        // Given
        Files.deleteIfExists(PERMISSIONS_FILE);
        PermissionService.reload();
        UserRole userRole = UserRole.TA;

        // When
        boolean result = PermissionService.hasAccess(userRole, UserRole.TA);

        // Then
        assertTrue(result);
    }

    @Test
    void hasAccess_WhenTaTargetsMoOrAdminPortal_ReturnsFalse() throws IOException {
        // 测试场景：TA 访问 MO 或 ADMIN 门户，预期拒绝访问
        // Given
        Files.deleteIfExists(PERMISSIONS_FILE);
        PermissionService.reload();
        UserRole userRole = UserRole.TA;

        // When
        boolean moPortal = PermissionService.hasAccess(userRole, UserRole.MO);
        boolean adminPortal = PermissionService.hasAccess(userRole, UserRole.ADMIN);

        // Then
        assertFalse(moPortal);
        assertFalse(adminPortal);
    }

    @Test
    void hasAccess_WhenAnyRoleIsNull_ReturnsFalse() throws IOException {
        // 测试场景：用户角色或目标角色为空，预期拒绝访问
        // Given
        Files.deleteIfExists(PERMISSIONS_FILE);
        PermissionService.reload();
        UserRole nullRole = null;

        // When
        boolean nullUserRole = PermissionService.hasAccess(nullRole, UserRole.TA);
        boolean nullTargetRole = PermissionService.hasAccess(UserRole.TA, nullRole);

        // Then
        assertFalse(nullUserRole);
        assertFalse(nullTargetRole);
    }

    @Test
    void hasAccess_WhenPermissionsFileDefinesCustomMatrix_ReturnsConfiguredAccess() throws IOException {
        // 测试场景：权限文件定义自定义矩阵，预期按文件规则返回访问结果
        // Given
        Files.createDirectories(PERMISSIONS_FILE.getParent());
        Files.writeString(PERMISSIONS_FILE,
                "{\"roleAccess\":{\"TA\":[\"TA\",\"MO\"],\"MO\":[\"MO\"],\"ADMIN\":[\"ADMIN\"]}}");
        PermissionService.reload();

        // When
        boolean taToMo = PermissionService.hasAccess(UserRole.TA, UserRole.MO);
        boolean adminToTa = PermissionService.hasAccess(UserRole.ADMIN, UserRole.TA);

        // Then
        assertTrue(taToMo);
        assertFalse(adminToTa);
    }

    @Test
    void hasAccess_WhenPermissionsFileIsInvalid_FallsBackToDefaults() throws IOException {
        // 测试场景：权限文件内容非法，预期回退到内置默认权限矩阵
        // Given
        Files.createDirectories(PERMISSIONS_FILE.getParent());
        Files.writeString(PERMISSIONS_FILE, "{invalid-json");
        PermissionService.reload();

        // When
        boolean adminToTa = PermissionService.hasAccess(UserRole.ADMIN, UserRole.TA);
        boolean taToMo = PermissionService.hasAccess(UserRole.TA, UserRole.MO);

        // Then
        assertTrue(adminToTa);
        assertFalse(taToMo);
    }
}
