package modules.config;

import infrastructure.persistence.JsonPersistenceManager;
import infrastructure.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private JsonPersistenceManager persistenceManager;

    @InjectMocks
    private SystemConfigService systemConfigService;

    @BeforeEach
    void setUp() {
        TimeProvider.setClock(Clock.fixed(Instant.parse("2026-05-13T12:00:00Z"), ZoneId.systemDefault()));
        systemConfigService = new SystemConfigService(persistenceManager);
    }

    @Test
    void getConfig_WhenPersistenceReturnsConfig_ReturnsPersistedConfig() {
        // 测试场景：持久化层返回已配置对象，预期原样返回配置
        // Given
        SystemConfig config = new SystemConfig();
        config.setApplicationStart(LocalDateTime.now().plusDays(1));
        config.setApplicationEnd(LocalDateTime.now().plusDays(2));
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(config);

        // When
        SystemConfig result = systemConfigService.getConfig();

        // Then
        assertEquals(config, result);
    }

    @Test
    void getConfig_WhenPersistenceReturnsNull_ReturnsEmptyConfig() {
        // 测试场景：持久化层返回空配置，预期返回新的空配置对象
        // Given
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(null);

        // When
        SystemConfig result = systemConfigService.getConfig();

        // Then
        assertNotNull(result);
        assertFalse(result.isConfigured());
    }

    @Test
    void updateApplicationCycle_WhenDatesAreValid_ReturnsNormalizedConfig() {
        // 测试场景：传入合法起止日期，预期保存并返回规范化后的整天时间范围
        // Given
        LocalDateTime start = LocalDate.now().plusDays(1).atTime(10, 30);
        LocalDateTime end = LocalDate.now().plusDays(5).atTime(8, 15);

        // When
        SystemConfig result = systemConfigService.updateApplicationCycle(start, end, "admin@test.com");

        // Then
        assertEquals(start.toLocalDate().atStartOfDay(), result.getApplicationStart());
        assertEquals(end.toLocalDate().atTime(23, 59, 59), result.getApplicationEnd());
        assertEquals("admin@test.com", result.getUpdatedBy());
        verify(persistenceManager).writeObject(eq(JsonPersistenceManager.SYSTEM_CONFIG_FILE), eq(result));
    }

    @Test
    void updateApplicationCycle_WhenStartIsNull_ThrowsIllegalArgumentException() {
        // 测试场景：开始时间为空，预期抛出非法参数异常
        // Given
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> systemConfigService.updateApplicationCycle(null, end, "admin@test.com"));

        // Then
        assertTrue(exception.getMessage().contains("Start and end time must not be null."));
    }

    @Test
    void updateApplicationCycle_WhenStartDateIsBeforeToday_ThrowsIllegalArgumentException() {
        // 测试场景：招聘开始日期早于今天，预期抛出非法参数异常
        // Given
        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> systemConfigService.updateApplicationCycle(start, end, "admin@test.com"));

        // Then
        assertTrue(exception.getMessage().contains("Recruitment start date must be today or a future date"));
    }

    @Test
    void updateApplicationCycle_WhenEndDateBeforeStartDate_ThrowsIllegalArgumentException() {
        // 测试场景：招聘结束日期早于开始日期，预期抛出非法参数异常
        // Given
        LocalDateTime start = LocalDate.now().plusDays(3).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> systemConfigService.updateApplicationCycle(start, end, "admin@test.com"));

        // Then
        assertTrue(exception.getMessage().contains("Recruitment end date must be on or after the start date"));
    }

    @Test
    void isNowWithinRecruitmentWindow_WhenNowWithinConfiguredWindow_ReturnsTrue() {
        // 测试场景：当前时间在招聘周期内，预期返回 true
        // Given
        SystemConfig config = new SystemConfig();
        config.setApplicationStart(LocalDateTime.now().minusDays(1));
        config.setApplicationEnd(LocalDateTime.now().plusDays(1));
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(config);

        // When
        boolean result = systemConfigService.isNowWithinRecruitmentWindow(LocalDateTime.now());

        // Then
        assertTrue(result);
    }

    @Test
    void isNowWithinRecruitmentWindow_WhenConfigIsMissing_ReturnsFalse() {
        // 测试场景：系统未配置招聘周期，预期返回 false
        // Given
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(new SystemConfig());

        // When
        boolean result = systemConfigService.isNowWithinRecruitmentWindow(LocalDateTime.now());

        // Then
        assertFalse(result);
    }

    @Test
    void isWithinApplicationCycle_WhenDateTimeWithinWindow_ReturnsTrue() {
        // 测试场景：指定时间在申请周期内，预期返回 true
        // Given
        LocalDateTime target = LocalDateTime.now();
        SystemConfig config = new SystemConfig();
        config.setApplicationStart(target.minusHours(1));
        config.setApplicationEnd(target.plusHours(1));
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(config);

        // When
        boolean result = systemConfigService.isWithinApplicationCycle(target);

        // Then
        assertTrue(result);
    }

    @Test
    void isWithinApplicationCycle_WhenDateTimeIsNull_ReturnsFalse() {
        // 测试场景：传入空时间，预期返回 false
        // Given
        LocalDateTime target = null;

        // When
        boolean result = systemConfigService.isWithinApplicationCycle(target);

        // Then
        assertFalse(result);
    }

    @Test
    void isDateWithinApplicationCycle_WhenDateInsideWindow_ReturnsTrue() {
        // 测试场景：日期处于申请周期内，预期返回 true
        // Given
        LocalDate date = LocalDate.now().plusDays(2);
        SystemConfig config = new SystemConfig();
        config.setApplicationStart(LocalDate.now().plusDays(1).atStartOfDay());
        config.setApplicationEnd(LocalDate.now().plusDays(3).atTime(23, 59, 59));
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(config);

        // When
        boolean result = systemConfigService.isDateWithinApplicationCycle(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isDateWithinApplicationCycle_WhenDateIsNull_ReturnsFalse() {
        // 测试场景：日期为空，预期返回 false
        // Given
        LocalDate date = null;

        // When
        boolean result = systemConfigService.isDateWithinApplicationCycle(date);

        // Then
        assertFalse(result);
    }

    @Test
    void validateDateWithinApplicationCycle_WhenDateOutsideWindow_ThrowsIllegalArgumentException() {
        // 测试场景：日期不在申请周期内，预期抛出非法参数异常
        // Given
        LocalDate date = LocalDate.now().plusDays(10);
        SystemConfig config = new SystemConfig();
        config.setApplicationStart(LocalDate.now().plusDays(1).atStartOfDay());
        config.setApplicationEnd(LocalDate.now().plusDays(3).atTime(23, 59, 59));
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(config);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> systemConfigService.validateDateWithinApplicationCycle(date));

        // Then
        assertTrue(exception.getMessage().contains("Job deadline must fall within the configured recruitment period."));
    }

    @Test
    void validateDateWithinApplicationCycle_WhenDateInsideWindow_ReturnsWithoutException() {
        // 测试场景：日期在申请周期内，预期不抛异常
        // Given
        LocalDate date = LocalDate.now().plusDays(2);
        SystemConfig config = new SystemConfig();
        config.setApplicationStart(LocalDate.now().plusDays(1).atStartOfDay());
        config.setApplicationEnd(LocalDate.now().plusDays(5).atTime(23, 59, 59));
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(config);

        // When
        systemConfigService.validateDateWithinApplicationCycle(date);

        // Then
        assertTrue(true);
    }

    @Test
    void validateDeadlineAfterNow_WhenDeadlineIsFuture_ReturnsWithoutException() {
        // 测试场景：截止日期在未来，预期不抛异常
        // Given
        LocalDate deadline = LocalDate.now().plusDays(1);

        // When
        systemConfigService.validateDeadlineAfterNow(deadline);

        // Then
        assertTrue(true);
    }

    @Test
    void validateDeadlineAfterNow_WhenDeadlineIsNull_ThrowsIllegalArgumentException() {
        // 测试场景：截止日期为空，预期抛出非法参数异常
        // Given
        LocalDate deadline = null;

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> systemConfigService.validateDeadlineAfterNow(deadline));

        // Then
        assertTrue(exception.getMessage().contains("Deadline date is required."));
    }

    @Test
    void validateDeadlineAfterNow_WhenDeadlineIsBeforeToday_ThrowsIllegalArgumentException() {
        // 测试场景：截止日期早于今天，预期抛出非法参数异常
        // Given
        LocalDate deadline = LocalDate.now().minusDays(1);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> systemConfigService.validateDeadlineAfterNow(deadline));

        // Then
        assertTrue(exception.getMessage().contains("Job deadline must be today or a future date."));
    }

    @Test
    void requireOpenRecruitmentWindowForPublish_WhenWindowIsConfiguredAndOpen_ReturnsWithoutException() {
        // 测试场景：招聘周期已配置且当前时间在周期内，预期允许发布
        // Given
        SystemConfig config = new SystemConfig();
        config.setApplicationStart(LocalDateTime.now().minusDays(1));
        config.setApplicationEnd(LocalDateTime.now().plusDays(1));
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(config);

        // When
        systemConfigService.requireOpenRecruitmentWindowForPublish();

        // Then
        assertTrue(true);
    }

    @Test
    void requireOpenRecruitmentWindowForPublish_WhenWindowNotConfigured_ThrowsIllegalStateException() {
        // 测试场景：招聘周期未配置，预期抛出非法状态异常
        // Given
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(new SystemConfig());

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                systemConfigService::requireOpenRecruitmentWindowForPublish);

        // Then
        assertTrue(exception.getMessage().contains("Recruitment period is not configured yet."));
    }

    @Test
    void requireOpenRecruitmentWindowForPublish_WhenNowOutsideWindow_ThrowsIllegalStateException() {
        // 测试场景：招聘周期已配置但当前不在周期内，预期抛出非法状态异常
        // Given
        SystemConfig config = new SystemConfig();
        config.setApplicationStart(LocalDateTime.now().plusDays(1));
        config.setApplicationEnd(LocalDateTime.now().plusDays(2));
        when(persistenceManager.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class))
                .thenReturn(config);

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                systemConfigService::requireOpenRecruitmentWindowForPublish);

        // Then
        assertTrue(exception.getMessage().contains("Jobs can only be published during the recruitment period"));
    }
}
