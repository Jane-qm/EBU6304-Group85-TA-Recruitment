package modules.auth;

import modules.profile.TAProfileService;
import modules.user.AccountStatus;
import modules.user.TA;
import modules.user.User;
import modules.user.UserRole;
import modules.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TAProfileService taProfileService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, taProfileService);
    }

    @Test
    void register_WhenTaEmailAndPasswordAreValid_ReturnsRegisteredUser() {
        // 测试场景：TA 使用合法学校邮箱和密码注册，预期返回注册用户并初始化空白资料
        // Given
        TA user = new TA("student@qmul.ac.uk", "Password123");
        user.setUserId(1001L);
        when(userService.register("student@qmul.ac.uk", "Password123", UserRole.TA)).thenReturn(user);

        // When
        User result = authService.register("student@qmul.ac.uk", "Password123", UserRole.TA);

        // Then
        assertNotNull(result);
        assertEquals(UserRole.TA, result.getRole());
        verify(taProfileService).initializeProfile(1001L, "student@qmul.ac.uk");
    }

    @Test
    void register_WhenEmailDomainIsInvalid_ThrowsIllegalArgumentException() {
        // 测试场景：注册邮箱不是允许的学校域名，预期抛出非法参数异常
        // Given
        String email = "student@gmail.com";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(email, "Password123", UserRole.TA));

        // Then
        assertTrue(exception.getMessage().contains("Only university emails are allowed"));
    }

    @Test
    void login_WhenCredentialsAreValid_ReturnsUser() {
        // 测试场景：登录参数合法且用户服务返回用户，预期登录成功
        // Given
        TA user = new TA("student@qmul.ac.uk", "Password123");
        when(userService.login("student@qmul.ac.uk", "Password123")).thenReturn(user);

        // When
        User result = authService.login("student@qmul.ac.uk", "Password123");

        // Then
        assertEquals(user, result);
    }

    @Test
    void login_WhenPasswordIsTooShort_ThrowsIllegalArgumentException() {
        // 测试场景：登录时密码长度不足，预期抛出非法参数异常
        // Given
        String password = "123";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.login("student@qmul.ac.uk", password));

        // Then
        assertTrue(exception.getMessage().contains("Password must be at least 6 characters."));
    }

    @Test
    void isAccountValid_WhenUserIsActive_ReturnsTrue() {
        // 测试场景：账户状态为 ACTIVE，预期返回 true
        // Given
        TA user = new TA("student@qmul.ac.uk", "Password123");
        user.setStatus(AccountStatus.ACTIVE);

        // When
        boolean result = authService.isAccountValid(user);

        // Then
        assertTrue(result);
    }

    @Test
    void isAccountValid_WhenUserIsNull_ReturnsFalse() {
        // 测试场景：传入空用户对象，预期返回 false
        // Given
        User user = null;

        // When
        boolean result = authService.isAccountValid(user);

        // Then
        assertFalse(result);
    }

    @Test
    void getAccountStatusMessage_WhenUserIsPending_ReturnsPendingMessage() {
        // 测试场景：用户状态为待审核，预期返回待审核提示信息
        // Given
        TA user = new TA("student@qmul.ac.uk", "Password123");
        user.setStatus(AccountStatus.PENDING);

        // When
        String result = authService.getAccountStatusMessage(user);

        // Then
        assertEquals("Account is pending approval. Please wait for admin review.", result);
    }

    @Test
    void getAccountStatusMessage_WhenUserIsNull_ReturnsUserNotFoundMessage() {
        // 测试场景：传入空用户对象，预期返回用户未找到提示
        // Given
        User user = null;

        // When
        String result = authService.getAccountStatusMessage(user);

        // Then
        assertEquals("User not found.", result);
    }

    @Test
    void sendVerificationCode_WhenEmailExists_ReturnsTrue() {
        // 测试场景：邮箱已存在，预期发送验证码返回 true
        // Given
        when(userService.emailExists("student@qmul.ac.uk")).thenReturn(true);

        // When
        boolean result = authService.sendVerificationCode("student@qmul.ac.uk");

        // Then
        assertTrue(result);
    }

    @Test
    void sendVerificationCode_WhenEmailDoesNotExist_ReturnsFalse() {
        // 测试场景：邮箱不存在，预期发送验证码返回 false
        // Given
        when(userService.emailExists("student@qmul.ac.uk")).thenReturn(false);

        // When
        boolean result = authService.sendVerificationCode("student@qmul.ac.uk");

        // Then
        assertFalse(result);
    }

    @Test
    void checkEmailExists_WhenEmailFormatIsValid_ReturnsLookup() {
        // 测试场景：邮箱格式合法，预期返回用户服务的查询结果
        // Given
        when(userService.emailExists("student@qmul.ac.uk")).thenReturn(true);

        // When
        boolean result = authService.checkEmailExists("student@qmul.ac.uk");

        // Then
        assertTrue(result);
    }

    @Test
    void checkEmailExists_WhenEmailFormatIsInvalid_ThrowsIllegalArgumentException() {
        // 测试场景：邮箱格式不合法，预期抛出非法参数异常
        // Given
        String email = "invalid-email";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.checkEmailExists(email));

        // Then
        assertTrue(exception.getMessage().contains("Invalid email format."));
    }

    @Test
    void resetPassword_WhenEmailAndPasswordAreValid_UpdatesPassword() {
        // 测试场景：邮箱和新密码合法，预期调用用户服务更新密码
        // Given
        String email = "student@qmul.ac.uk";
        String newPassword = "NewPassword123";

        // When
        authService.resetPassword(email, newPassword);

        // Then
        verify(userService).updatePassword(email, newPassword);
    }

    @Test
    void resetPassword_WhenNewPasswordIsBlank_ThrowsIllegalArgumentException() {
        // 测试场景：重置密码时新密码为空白，预期抛出非法参数异常
        // Given
        String email = "student@qmul.ac.uk";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(email, " "));

        // Then
        assertTrue(exception.getMessage().contains("Password must not be empty."));
    }

    @Test
    void isPasswordChangeRequired_WhenEmailIsValid_ReturnsLookup() {
        // 测试场景：邮箱格式合法，预期返回用户服务的密码修改要求结果
        // Given
        when(userService.isPasswordChangeRequired("student@qmul.ac.uk")).thenReturn(true);

        // When
        boolean result = authService.isPasswordChangeRequired("student@qmul.ac.uk");

        // Then
        assertTrue(result);
    }

    @Test
    void isPasswordChangeRequired_WhenEmailIsBlank_ThrowsIllegalArgumentException() {
        // 测试场景：邮箱为空白，预期抛出非法参数异常
        // Given
        String email = " ";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.isPasswordChangeRequired(email));

        // Then
        assertTrue(exception.getMessage().contains("Email must not be empty."));
    }
}
