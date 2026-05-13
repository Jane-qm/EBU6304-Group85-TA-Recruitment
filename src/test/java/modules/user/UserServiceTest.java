package modules.user;

import infrastructure.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        TimeProvider.setClock(Clock.fixed(Instant.parse("2026-05-13T12:00:00Z"), ZoneId.systemDefault()));
        when(userDAO.loadAll()).thenReturn(List.of());
        userService = new UserService(userDAO);
    }

    @Test
    void register_WhenEmailAndRoleAreValid_ReturnsRegisteredUser() {
        // 测试场景：使用合法邮箱和角色注册用户，预期成功创建用户并保存
        // Given
        String email = "student@qmul.ac.uk";

        // When
        User result = userService.register(email, "Password123", UserRole.TA);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(UserRole.TA, result.getRole());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        verify(userDAO).saveAll(anyMap());
    }

    @Test
    void register_WhenEmailAlreadyExists_ThrowsIllegalArgumentException() {
        // 测试场景：注册邮箱已存在，预期抛出非法参数异常
        // Given
        userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.register("student@qmul.ac.uk", "Password456", UserRole.TA));

        // Then
        assertTrue(exception.getMessage().contains("Email already registered."));
    }

    @Test
    void register_WhenEmailContainsWhitespace_NormalizesAndRegistersUser() {
        // 测试场景：注册邮箱包含前后空格和大写，预期标准化后成功注册
        // Given
        String email = "  Student@QMUL.AC.UK ";

        // When
        User result = userService.register(email, "Password123", UserRole.TA);

        // Then
        assertEquals("student@qmul.ac.uk", result.getEmail());
        assertNotNull(userService.findByEmail("student@qmul.ac.uk"));
    }

    @Test
    void login_WhenCredentialsAreCorrect_ReturnsUser() {
        // 测试场景：使用正确邮箱和密码登录，预期返回用户对象并重置失败计数
        // Given
        userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        User result = userService.login("student@qmul.ac.uk", "Password123");

        // Then
        assertNotNull(result);
        assertEquals(0, result.getFailedLoginCount());
        assertNotNull(result.getLastLogin());
    }

    @Test
    void login_WhenPasswordWrongFiveTimes_LocksAccountAndReturnsNull() {
        // 测试场景：密码连续错误达到上限，预期账户被锁定且登录返回 null
        // Given
        String email = "student@qmul.ac.uk";
        userService.register(email, "Password123", UserRole.TA);

        // When
        for (int i = 0; i < 5; i++) {
            userService.login(email, "WrongPassword");
        }
        User storedUser = userService.findByEmail(email);

        // Then
        assertNotNull(storedUser.getLockedUntil());
        assertEquals(0, storedUser.getFailedLoginCount());
        assertTrue(storedUser.getLockedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void login_WhenAccountIsLocked_ReturnsNull() {
        // 测试场景：账户处于锁定期内，预期登录返回 null
        // Given
        String email = "student@qmul.ac.uk";
        userService.register(email, "Password123", UserRole.TA);
        User storedUser = userService.findByEmail(email);
        storedUser.setLockedUntil(TimeProvider.now().plusMinutes(10));
        userService.saveUser(storedUser);

        // When
        User result = userService.login(email, "Password123");

        // Then
        assertNull(result);
    }

    @Test
    void login_WhenPasswordWrongButBelowLimit_IncrementsFailedCountAndReturnsNull() {
        // 测试场景：密码错误但未达到锁定阈值，预期失败次数加一且返回 null
        // Given
        String email = "student@qmul.ac.uk";
        userService.register(email, "Password123", UserRole.TA);

        // When
        User result = userService.login(email, "WrongPassword");

        // Then
        assertNull(result);
        assertEquals(1, userService.findByEmail(email).getFailedLoginCount());
    }

    @Test
    void findByEmail_WhenUserExists_ReturnsUser() {
        // 测试场景：按邮箱查询已存在用户，预期返回用户对象
        // Given
        userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        User result = userService.findByEmail("student@qmul.ac.uk");

        // Then
        assertNotNull(result);
        assertEquals("student@qmul.ac.uk", result.getEmail());
    }

    @Test
    void findByEmail_WhenEmailIsBlank_ThrowsIllegalArgumentException() {
        // 测试场景：按空白邮箱查询用户，预期抛出非法参数异常
        // Given
        String email = " ";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.findByEmail(email));

        // Then
        assertTrue(exception.getMessage().contains("Email must not be blank."));
    }

    @Test
    void emailExists_WhenUserExists_ReturnsTrue() {
        // 测试场景：邮箱已注册，预期返回 true
        // Given
        userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        boolean result = userService.emailExists("student@qmul.ac.uk");

        // Then
        assertTrue(result);
    }

    @Test
    void emailExists_WhenUserDoesNotExist_ReturnsFalse() {
        // 测试场景：邮箱未注册，预期返回 false
        // Given
        String email = "missing@qmul.ac.uk";

        // When
        boolean result = userService.emailExists(email);

        // Then
        assertFalse(result);
    }

    @Test
    void updatePassword_WhenUserExists_UpdatesPasswordAndClearsLockState() {
        // 测试场景：更新已存在用户密码，预期密码更新并清除锁定状态
        // Given
        String email = "student@qmul.ac.uk";
        userService.register(email, "Password123", UserRole.TA);
        User storedUser = userService.findByEmail(email);
        storedUser.setMustChangePassword(true);
        storedUser.setFailedLoginCount(3);
        storedUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        userService.saveUser(storedUser);

        // When
        userService.updatePassword(email, "NewPassword123");
        User result = userService.login(email, "NewPassword123");

        // Then
        assertNotNull(result);
        assertFalse(result.isMustChangePassword());
        assertEquals(0, result.getFailedLoginCount());
        assertNull(result.getLockedUntil());
    }

    @Test
    void updatePassword_WhenUserDoesNotExist_ThrowsIllegalArgumentException() {
        // 测试场景：更新不存在用户的密码，预期抛出非法参数异常
        // Given
        String email = "missing@qmul.ac.uk";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updatePassword(email, "NewPassword123"));

        // Then
        assertTrue(exception.getMessage().contains("Email is not registered."));
    }

    @Test
    void saveUser_WhenUserIsValid_SavesUser() {
        // 测试场景：保存有效用户对象，预期持久化保存成功
        // Given
        User user = new TA("student@qmul.ac.uk", "Password123");

        // When
        userService.saveUser(user);

        // Then
        assertEquals(user, userService.findByEmail("student@qmul.ac.uk"));
        verify(userDAO).saveAll(anyMap());
    }

    @Test
    void saveUser_WhenEmailContainsUppercase_StoresNormalizedLookupKey() {
        // 测试场景：保存用户邮箱含大写，预期可通过标准化邮箱查询到
        // Given
        User user = new TA("Student@QMUL.AC.UK", "Password123");

        // When
        userService.saveUser(user);

        // Then
        assertEquals(user, userService.findByEmail("student@qmul.ac.uk"));
    }

    @Test
    void saveUser_WhenUserIsNull_ThrowsIllegalArgumentException() {
        // 测试场景：保存空用户对象，预期抛出非法参数异常
        // Given
        User user = null;

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.saveUser(user));

        // Then
        assertTrue(exception.getMessage().contains("User must not be null."));
    }

    @Test
    void findById_WhenUserExists_ReturnsUser() {
        // 测试场景：按用户 ID 查询已存在用户，预期返回对应用户
        // Given
        User user = userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        User result = userService.findById(user.getUserId());

        // Then
        assertEquals(user, result);
    }

    @Test
    void findById_WhenUserDoesNotExist_ReturnsNull() {
        // 测试场景：按不存在的用户 ID 查询，预期返回 null
        // Given
        Long userId = 9999L;

        // When
        User result = userService.findById(userId);

        // Then
        assertNull(result);
    }

    @Test
    void updateUser_WhenUserIsValid_UpdatesStoredUser() {
        // 测试场景：更新有效用户对象，预期用户信息被更新
        // Given
        User user = userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);
        user.setStatus(AccountStatus.DISABLED);

        // When
        userService.updateUser(user);

        // Then
        assertEquals(AccountStatus.DISABLED, userService.findByEmail("student@qmul.ac.uk").getStatus());
    }

    @Test
    void updateUser_WhenUserIsNull_ReturnsWithoutChange() {
        // 测试场景：更新空用户对象，预期不抛异常且不做任何修改
        // Given
        User user = null;

        // When
        userService.updateUser(user);

        // Then
        assertTrue(userService.listAll().isEmpty());
    }

    @Test
    void updateUser_WhenEmailIsBlank_ThrowsIllegalArgumentException() {
        // 测试场景：更新用户时邮箱为空白字符串，预期抛出非法参数异常
        // Given
        User user = new TA("student@qmul.ac.uk", "Password123") {
            @Override
            public String getEmail() {
                return " ";
            }
        };

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(user));

        // Then
        assertTrue(exception.getMessage().contains("Email must not be blank."));
    }

    @Test
    void listAll_WhenUsersExist_ReturnsAllUsers() {
        // 测试场景：系统中存在多个用户，预期返回所有用户列表
        // Given
        userService.register("ta1@qmul.ac.uk", "Password123", UserRole.TA);
        userService.register("ta2@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        List<User> result = userService.listAll();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void listAll_WhenNoUsersExist_ReturnsEmptyList() {
        // 测试场景：系统中没有用户，预期返回空列表
        // Given
        when(userDAO.loadAll()).thenReturn(List.of());
        userService = new UserService(userDAO);

        // When
        List<User> result = userService.listAll();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ReturnsNull() {
        // 测试场景：按不存在 ID 查询用户，预期返回 null
        // Given
        Long userId = 9999L;

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNull(result);
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        // 测试场景：按用户 ID 查询存在用户，预期返回对应用户对象
        // Given
        User user = userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        User result = userService.getUserById(user.getUserId());

        // Then
        assertEquals(user, result);
    }

    @Test
    void isStrictAdmin_WhenUserIsActiveAdmin_ReturnsTrue() {
        // 测试场景：用户为 ACTIVE 状态的 ADMIN，预期返回 true
        // Given
        Admin admin = new Admin("admin@qmul.ac.uk", "Password123");
        admin.setStatus(AccountStatus.ACTIVE);

        // When
        boolean result = userService.isStrictAdmin(admin);

        // Then
        assertTrue(result);
    }

    @Test
    void isStrictAdmin_WhenUserIsNull_ReturnsFalse() {
        // 测试场景：用户为空，预期返回 false
        // Given
        User user = null;

        // When
        boolean result = userService.isStrictAdmin(user);

        // Then
        assertFalse(result);
    }

    @Test
    void listAllUsers_WhenUsersExist_ReturnsSortedUsers() {
        // 测试场景：存在多个用户，预期按用户 ID 升序返回列表
        // Given
        User user1 = userService.register("ta1@qmul.ac.uk", "Password123", UserRole.TA);
        User user2 = userService.register("ta2@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        List<User> result = userService.listAllUsers();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.get(0).getUserId() <= result.get(1).getUserId());
        assertEquals(user1.getEmail(), result.get(0).getEmail());
        assertEquals(user2.getEmail(), result.get(1).getEmail());
    }

    @Test
    void updateAccountStatus_WhenUserExists_UpdatesStatus() {
        // 测试场景：更新已存在用户账户状态，预期状态被更新
        // Given
        userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        userService.updateAccountStatus("student@qmul.ac.uk", AccountStatus.DISABLED);

        // Then
        assertEquals(AccountStatus.DISABLED, userService.findByEmail("student@qmul.ac.uk").getStatus());
    }

    @Test
    void updateAccountStatus_WhenStatusIsActive_UpdatesStatus() {
        // 测试场景：将已存在用户状态更新为 ACTIVE，预期保存更新后状态
        // Given
        User user = userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);
        user.setStatus(AccountStatus.DISABLED);
        userService.saveUser(user);

        // When
        userService.updateAccountStatus("student@qmul.ac.uk", AccountStatus.ACTIVE);

        // Then
        assertEquals(AccountStatus.ACTIVE, userService.findByEmail("student@qmul.ac.uk").getStatus());
    }

    @Test
    void updateAccountStatus_WhenUserDoesNotExist_ThrowsIllegalArgumentException() {
        // 测试场景：更新不存在用户的账户状态，预期抛出非法参数异常
        // Given
        String email = "missing@qmul.ac.uk";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateAccountStatus(email, AccountStatus.DISABLED));

        // Then
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void approveMoAccount_WhenMoExists_SetsStatusActive() {
        // 测试场景：审批已存在 MO 账户，预期其状态变为 ACTIVE
        // Given
        User user = userService.register("mo@qmul.ac.uk", "Password123", UserRole.MO);
        user.setStatus(AccountStatus.PENDING);
        userService.saveUser(user);

        // When
        userService.approveMoAccount("mo@qmul.ac.uk");

        // Then
        assertEquals(AccountStatus.ACTIVE, userService.findByEmail("mo@qmul.ac.uk").getStatus());
    }

    @Test
    void approveMoAccount_WhenUserIsNotMo_ThrowsIllegalArgumentException() {
        // 测试场景：审批的用户不是 MO，预期抛出非法参数异常
        // Given
        userService.register("ta@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.approveMoAccount("ta@qmul.ac.uk"));

        // Then
        assertTrue(exception.getMessage().contains("MO account not found"));
    }

    @Test
    void approveMoAccount_WhenUserMissing_ThrowsIllegalArgumentException() {
        // 测试场景：审批不存在的 MO 账户，预期抛出非法参数异常
        // Given
        String email = "missing@qmul.ac.uk";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.approveMoAccount(email));

        // Then
        assertTrue(exception.getMessage().contains("MO account not found"));
    }

    @Test
    void disableAccount_WhenUserExists_SetsStatusDisabled() {
        // 测试场景：禁用已存在用户，预期其状态变为 DISABLED
        // Given
        userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // When
        userService.disableAccount("student@qmul.ac.uk");

        // Then
        assertEquals(AccountStatus.DISABLED, userService.findByEmail("student@qmul.ac.uk").getStatus());
    }

    @Test
    void disableAccount_WhenUserDoesNotExist_ThrowsIllegalArgumentException() {
        // 测试场景：禁用不存在用户，预期抛出非法参数异常
        // Given
        String email = "missing@qmul.ac.uk";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.disableAccount(email));

        // Then
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void resetPasswordByAdmin_WhenUserExists_ResetsPassword() {
        // 测试场景：管理员重置已存在用户密码，预期用户可使用新密码登录
        // Given
        String email = "student@qmul.ac.uk";
        userService.register(email, "Password123", UserRole.TA);

        // When
        userService.resetPasswordByAdmin(email, "AdminReset123");
        User result = userService.login(email, "AdminReset123");

        // Then
        assertNotNull(result);
    }

    @Test
    void resetPasswordByAdmin_WhenUserMissing_ThrowsIllegalArgumentException() {
        // 测试场景：管理员重置不存在用户密码，预期抛出非法参数异常
        // Given
        String email = "missing@qmul.ac.uk";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.resetPasswordByAdmin(email, "AdminReset123"));

        // Then
        assertTrue(exception.getMessage().contains("Email is not registered."));
    }

    @Test
    void login_WhenEmailDoesNotExist_ReturnsNull() {
        // 测试场景：使用不存在邮箱登录，预期返回 null
        // Given
        String email = "missing@qmul.ac.uk";

        // When
        User result = userService.login(email, "Password123");

        // Then
        assertNull(result);
    }

    @Test
    void register_WhenRoleIsNull_ThrowsIllegalArgumentException() {
        // 测试场景：注册时角色为空，预期抛出非法参数异常
        // Given
        String email = "student@qmul.ac.uk";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.register(email, "Password123", null));

        // Then
        assertTrue(exception.getMessage().contains("Role must not be null."));
    }

    @Test
    void updateUser_WhenEmailIsNull_ReturnsWithoutChange() {
        // 测试场景：更新用户时邮箱为空，预期直接返回且不保存
        // Given
        User user = new TA("student@qmul.ac.uk", "Password123") {
            @Override
            public String getEmail() {
                return null;
            }
        };

        // When
        userService.updateUser(user);

        // Then
        assertTrue(userService.listAll().isEmpty());
    }

    @Test
    void getUserById_WhenUserIdIsNull_ReturnsNull() {
        // 测试场景：按空用户 ID 查询，预期返回 null
        // Given
        Long userId = null;

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNull(result);
    }

    @Test
    void isStrictAdmin_WhenUserIsInactiveAdmin_ReturnsFalse() {
        // 测试场景：用户为非 ACTIVE 状态的 ADMIN，预期返回 false
        // Given
        Admin admin = new Admin("admin@qmul.ac.uk", "Password123");
        admin.setStatus(AccountStatus.DISABLED);

        // When
        boolean result = userService.isStrictAdmin(admin);

        // Then
        assertFalse(result);
    }

    @Test
    void isStrictAdmin_WhenUserIsActiveButNotAdmin_ReturnsFalse() {
        // 测试场景：用户状态为 ACTIVE 但角色不是 ADMIN，预期返回 false
        // Given
        TA ta = new TA("student@qmul.ac.uk", "Password123");
        ta.setStatus(AccountStatus.ACTIVE);

        // When
        boolean result = userService.isStrictAdmin(ta);

        // Then
        assertFalse(result);
    }

    @Test
    void isPasswordChangeRequired_WhenUserExists_ReturnsFlag() {
        // 测试场景：查询已存在用户的改密标记，预期返回对应布尔值
        // Given
        User user = userService.register("student@qmul.ac.uk", "Password123", UserRole.TA);
        user.setMustChangePassword(true);
        userService.saveUser(user);

        // When
        boolean result = userService.isPasswordChangeRequired("student@qmul.ac.uk");

        // Then
        assertTrue(result);
    }

    @Test
    void isPasswordChangeRequired_WhenUserDoesNotExist_ReturnsFalse() {
        // 测试场景：查询不存在用户的改密标记，预期返回 false
        // Given
        String email = "missing@qmul.ac.uk";

        // When
        boolean result = userService.isPasswordChangeRequired(email);

        // Then
        assertFalse(result);
    }

    @Test
    void importMOFromCSV_WhenFileMissing_ReturnsFailureResult(@TempDir Path tempDir) {
        // 测试场景：导入的 CSV 文件不存在，预期返回读取失败结果
        // Given
        String filePath = tempDir.resolve("missing.csv").toString();

        // When
        UserService.MOImportResult result = userService.importMOFromCSV(filePath);

        // Then
        assertEquals(0, result.successCount);
        assertEquals(1, result.failCount);
        assertTrue(result.errors.get(0).contains("读取文件失败"));
    }

    @Test
    void importMOFromCSV_WhenRowsContainMixedValidity_ReturnsAggregatedResult(@TempDir Path tempDir) throws Exception {
        // 测试场景：CSV 同时包含合法、重复、格式错误和非法邮箱数据，预期分别统计成功和失败
        // Given
        userService.register("duplicate@qmul.ac.uk", "Password123", UserRole.MO);
        Path csvFile = tempDir.resolve("mos.csv");
        Files.writeString(csvFile, String.join(System.lineSeparator(),
                "email,password,name",
                "newmo@qmul.ac.uk,Secret123,New Mo",
                "duplicate@qmul.ac.uk,Secret123,Old Mo",
                "invalid-email,Secret123,Bad Mo",
                "broken-row"
        ));

        // When
        UserService.MOImportResult result = userService.importMOFromCSV(csvFile.toString());

        // Then
        assertEquals(1, result.successCount);
        assertEquals(3, result.failCount);
        assertTrue(result.errors.stream().anyMatch(error -> error.contains("MO已存在")));
        assertTrue(result.errors.stream().anyMatch(error -> error.contains("邮箱格式错误")));
        assertTrue(result.errors.stream().anyMatch(error -> error.contains("格式错误")));
        User imported = userService.findByEmail("newmo@qmul.ac.uk");
        assertNotNull(imported);
        assertEquals(UserRole.MO, imported.getRole());
    }

    @Test
    void importMOFromCSV_WhenHeaderAndBlankLinesOnly_ReturnsZeroCounts(@TempDir Path tempDir) throws Exception {
        // 测试场景：CSV 仅包含表头和空行，预期成功数失败数都为 0
        // Given
        Path csvFile = tempDir.resolve("empty.csv");
        Files.writeString(csvFile, String.join(System.lineSeparator(),
                "email,password,name",
                "",
                "   "
        ));

        // When
        UserService.MOImportResult result = userService.importMOFromCSV(csvFile.toString());

        // Then
        assertEquals(0, result.successCount);
        assertEquals(0, result.failCount);
        assertTrue(result.errors.isEmpty());
    }
}
