package infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    @Test
    void hash_WhenPasswordIsValid_ReturnsPbkdf2Hash() {
        // 测试场景：传入合法密码进行哈希，预期返回 PBKDF2 格式的哈希字符串
        // Given
        String rawPassword = "CorrectHorseBatteryStaple9";

        // When
        String storedHash = PasswordService.hash(rawPassword);

        // Then
        assertTrue(storedHash.startsWith("PBKDF2$"));
        assertFalse(PasswordService.needsUpgrade(storedHash));
    }

    @Test
    void hash_WhenPasswordIsNull_ThrowsIllegalArgumentException() {
        // 测试场景：传入空密码进行哈希，预期抛出非法参数异常
        // Given
        String rawPassword = null;

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PasswordService.hash(rawPassword));

        // Then
        assertTrue(exception.getMessage().contains("Password must not be null."));
    }

    @Test
    void verify_WhenPbkdf2HashMatches_ReturnsTrue() {
        // 测试场景：使用匹配的原始密码验证 PBKDF2 哈希，预期验证成功
        // Given
        String rawPassword = "AnyPass123";
        String storedHash = PasswordService.hash(rawPassword);

        // When
        boolean result = PasswordService.verify(rawPassword, storedHash);

        // Then
        assertTrue(result);
    }

    @Test
    void verify_WhenPbkdf2HashDoesNotMatch_ReturnsFalse() {
        // 测试场景：使用错误密码验证 PBKDF2 哈希，预期验证失败
        // Given
        String storedHash = PasswordService.hash("AnyPass123");

        // When
        boolean result = PasswordService.verify("wrong-password", storedHash);

        // Then
        assertFalse(result);
    }

    @Test
    void verify_WhenArgumentsContainNull_ReturnsFalse() {
        // 测试场景：原始密码或存储哈希为空，预期返回 false
        // Given
        String storedHash = PasswordService.hash("SafePass123");

        // When
        boolean resultWithNullPassword = PasswordService.verify(null, storedHash);
        boolean resultWithNullHash = PasswordService.verify("SafePass123", null);

        // Then
        assertFalse(resultWithNullPassword);
        assertFalse(resultWithNullHash);
    }

    @Test
    void verify_WhenPbkdf2HashMalformed_ReturnsFalse() {
        // 测试场景：存储哈希为非法 PBKDF2 格式，预期返回 false
        // Given
        String malformedHash = "PBKDF2$not$valid";

        // When
        boolean result = PasswordService.verify("password", malformedHash);

        // Then
        assertFalse(result);
    }

    @Test
    void needsUpgrade_WhenLegacySha256HashProvided_ReturnsTrue() {
        // 测试场景：传入旧版 SHA-256 哈希，预期需要升级
        // Given
        String legacyHash = "e7cf3ef4f17c3999a94f2c6f612e8a888e5d3a0a1f9f3b935f6c5a0cf5ef8f2a";

        // When
        boolean result = PasswordService.needsUpgrade(legacyHash);

        // Then
        assertTrue(result);
    }

    @Test
    void needsUpgrade_WhenPbkdf2HashProvided_ReturnsFalse() {
        // 测试场景：传入新版 PBKDF2 哈希，预期不需要升级
        // Given
        String storedHash = PasswordService.hash("ModernPass123");

        // When
        boolean result = PasswordService.needsUpgrade(storedHash);

        // Then
        assertFalse(result);
    }
}
