package modules.job;

import modules.application.Application;
import modules.application.ApplicationDAO;
import modules.application.ApplicationService;
import modules.config.SystemConfig;
import modules.config.SystemConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobDAO jobDAO;

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobDAO, applicationDAO, systemConfigService, applicationService);
    }

    @Test
    void createOrUpdate_WhenDraftJobIsValid_ReturnsSavedJob() {
        // 测试场景：创建合法草稿岗位，预期保存并返回岗位对象
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "DRAFT");
        when(jobDAO.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobDAO.findAll()).thenReturn(List.of());

        // When
        Job result = jobService.createOrUpdate(job);

        // Then
        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
        verify(jobDAO).save(job);
    }

    @Test
    void createOrUpdate_WhenJobIsNull_ThrowsIllegalArgumentException() {
        // 测试场景：创建或更新岗位时传入 null，预期抛出非法参数异常
        // Given
        Job job = null;

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jobService.createOrUpdate(job));

        // Then
        assertTrue(exception.getMessage().contains("Job must not be null."));
    }

    @Test
    void createOrUpdate_WhenDuplicateModuleOpenJobExists_ThrowsIllegalStateException() {
        // 测试场景：同一 MO 对同一模块已存在开放岗位，预期抛出非法状态异常
        // Given
        Job existing = createJob(2000L, 3001L, "EBU6304", "OPEN");
        Job incoming = createJob(2001L, 3001L, "EBU6304", "OPEN");
        existing.setApplicationDeadline(LocalDate.now().plusDays(1).atTime(23, 59, 59));
        existing.setOfferResponseDeadline(LocalDate.now().plusDays(2).atTime(23, 59, 59));
        incoming.setApplicationDeadline(LocalDate.now().plusDays(1).atTime(23, 59, 59));
        incoming.setOfferResponseDeadline(LocalDate.now().plusDays(2).atTime(23, 59, 59));
        when(jobDAO.findAll()).thenReturn(List.of(existing));
        doNothing().when(systemConfigService).requireOpenRecruitmentWindowForPublish();
        doNothing().when(systemConfigService).validateDateWithinApplicationCycle(any(LocalDate.class));
        doNothing().when(systemConfigService).validateDeadlineAfterNow(any(LocalDate.class));
        when(systemConfigService.getConfig()).thenReturn(new SystemConfig());

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> jobService.createOrUpdate(incoming));

        // Then
        assertTrue(exception.getMessage().contains("already have an open posting"));
    }

    @Test
    void listAll_WhenJobsExist_ReturnsJobsWithSyncedDeadlines() {
        // 测试场景：查询全部岗位时存在描述中的 Deadline，预期返回同步后的岗位列表
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        job.setDescription("Deadline: 2030-06-01\nOffer response due: 2030-06-02");
        when(jobDAO.findAll()).thenReturn(List.of(job));

        // When
        List<Job> result = jobService.listAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2030, 6, 1), result.get(0).getApplicationDeadline().toLocalDate());
    }

    @Test
    void listAll_WhenNoJobsExist_ReturnsEmptyList() {
        // 测试场景：查询全部岗位时无数据，预期返回空列表
        // Given
        when(jobDAO.findAll()).thenReturn(List.of());

        // When
        List<Job> result = jobService.listAll();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void listPublishedJobs_WhenOpenAndNotExpired_ReturnsVisibleJobs() {
        // 测试场景：岗位状态为 OPEN 且未过期，预期返回给 TA 可见的岗位列表
        // Given
        Job openJob = createJob(2001L, 3001L, "EBU6304", "OPEN");
        openJob.setApplicationDeadline(LocalDateTime.now().plusDays(1));
        when(jobDAO.findAll()).thenReturn(List.of(openJob));

        // When
        List<Job> result = jobService.listPublishedJobs();

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void listPublishedJobs_WhenJobExpired_ReturnsEmptyList() {
        // 测试场景：岗位已过期，预期不返回给 TA 可见列表
        // Given
        Job expiredJob = createJob(2001L, 3001L, "EBU6304", "OPEN");
        expiredJob.setApplicationDeadline(LocalDateTime.now().minusDays(1));
        when(jobDAO.findAll()).thenReturn(List.of(expiredJob));

        // When
        List<Job> result = jobService.listPublishedJobs();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getPublishedJob_WhenJobExists_ReturnsJob() {
        // 测试场景：按 ID 查询已发布岗位存在，预期返回岗位对象
        // Given
        Job openJob = createJob(2001L, 3001L, "EBU6304", "OPEN");
        openJob.setApplicationDeadline(LocalDateTime.now().plusDays(1));
        when(jobDAO.findAll()).thenReturn(List.of(openJob));

        // When
        Job result = jobService.getPublishedJob(2001L);

        // Then
        assertEquals(openJob, result);
    }

    @Test
    void getPublishedJob_WhenJobDoesNotExist_ReturnsNull() {
        // 测试场景：按 ID 查询已发布岗位不存在，预期返回 null
        // Given
        when(jobDAO.findAll()).thenReturn(List.of());

        // When
        Job result = jobService.getPublishedJob(2001L);

        // Then
        assertNull(result);
    }

    @Test
    void getJobById_WhenJobExists_ReturnsJob() {
        // 测试场景：按 ID 查询岗位存在，预期返回岗位对象
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "DRAFT");
        when(jobDAO.findAll()).thenReturn(List.of(job));

        // When
        Job result = jobService.getJobById(2001L);

        // Then
        assertEquals(job, result);
    }

    @Test
    void getJobById_WhenJobDoesNotExist_ReturnsNull() {
        // 测试场景：按 ID 查询岗位不存在，预期返回 null
        // Given
        when(jobDAO.findAll()).thenReturn(List.of());

        // When
        Job result = jobService.getJobById(2001L);

        // Then
        assertNull(result);
    }

    @Test
    void publishJob_WhenRecruitmentWindowOpen_ReturnsPublishedJob() {
        // 测试场景：招聘周期开放且岗位合法，预期岗位成功发布
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        job.setApplicationDeadline(LocalDate.now().plusDays(1).atTime(23, 59, 59));
        job.setOfferResponseDeadline(LocalDate.now().plusDays(2).atTime(23, 59, 59));
        when(jobDAO.findAll()).thenReturn(List.of(job));
        when(jobDAO.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(systemConfigService).requireOpenRecruitmentWindowForPublish();
        doNothing().when(systemConfigService).validateDateWithinApplicationCycle(any(LocalDate.class));
        doNothing().when(systemConfigService).validateDeadlineAfterNow(any(LocalDate.class));
        when(systemConfigService.getConfig()).thenReturn(new SystemConfig());

        // When
        Job result = jobService.publishJob(2001L);

        // Then
        assertEquals("PUBLISHED", result.getStatus());
    }

    @Test
    void publishJob_WhenJobDoesNotExist_ThrowsIllegalArgumentException() {
        // 测试场景：发布不存在岗位，预期抛出非法参数异常
        // Given
        when(jobDAO.findAll()).thenReturn(List.of());

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jobService.publishJob(2001L));

        // Then
        assertTrue(exception.getMessage().contains("Job not found."));
    }

    @Test
    void closeJob_WhenJobExists_ClosesJobAndExpiresApplications() {
        // 测试场景：关闭已存在岗位，预期状态改为 CLOSED 并处理关联申请
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        when(jobDAO.findAll()).thenReturn(List.of(job));
        when(jobDAO.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Job result = jobService.closeJob(2001L);

        // Then
        assertEquals("CLOSED", result.getStatus());
        verify(applicationService).processExpiredApplicationsForJob(2001L);
    }

    @Test
    void closeJob_WhenJobAlreadyClosed_ThrowsIllegalArgumentException() {
        // 测试场景：关闭已关闭岗位，预期抛出非法参数异常
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "CLOSED");
        when(jobDAO.findAll()).thenReturn(List.of(job));

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jobService.closeJob(2001L));

        // Then
        assertTrue(exception.getMessage().contains("already closed"));
    }

    @Test
    void extractDeadline_WhenPersistedDeadlineExists_ReturnsPersistedDate() {
        // 测试场景：岗位已持久化 applicationDeadline，预期直接返回该日期
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        job.setApplicationDeadline(LocalDate.of(2030, 6, 1).atTime(23, 59, 59));

        // When
        LocalDate result = jobService.extractDeadline(job);

        // Then
        assertEquals(LocalDate.of(2030, 6, 1), result);
    }

    @Test
    void extractDeadline_WhenDescriptionHasInvalidFormat_ThrowsIllegalArgumentException() {
        // 测试场景：描述中的 Deadline 格式非法，预期抛出非法参数异常
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        job.setDescription("Deadline: invalid-date");

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jobService.extractDeadline(job));

        // Then
        assertTrue(exception.getMessage().contains("Job deadline must use format YYYY-MM-DD"));
    }

    @Test
    void validateDeadlineWithinCycle_WhenDeadlineExists_DelegatesToSystemConfigValidation() {
        // 测试场景：岗位截止日期合法存在，预期委托系统配置服务校验
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        job.setApplicationDeadline(LocalDate.now().plusDays(1).atTime(23, 59, 59));
        doNothing().when(systemConfigService).validateDateWithinApplicationCycle(any(LocalDate.class));
        doNothing().when(systemConfigService).validateDeadlineAfterNow(any(LocalDate.class));

        // When
        jobService.validateDeadlineWithinCycle(job);

        // Then
        verify(systemConfigService).validateDateWithinApplicationCycle(job.getApplicationDeadline().toLocalDate());
        verify(systemConfigService).validateDeadlineAfterNow(job.getApplicationDeadline().toLocalDate());
    }

    @Test
    void validateDeadlineWithinCycle_WhenDeadlineMissing_ThrowsIllegalArgumentException() {
        // 测试场景：岗位截止日期缺失，预期抛出非法参数异常
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        job.setDescription("");
        job.setApplicationDeadline(null);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jobService.validateDeadlineWithinCycle(job));

        // Then
        assertTrue(exception.getMessage().contains("Job deadline is required"));
    }

    @Test
    void validateOfferResponseForPublishedJob_WhenOfferDeadlineIsValid_ReturnsWithoutException() {
        // 测试场景：已发布岗位的 offer 响应截止时间合法，预期不抛异常
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        job.setApplicationDeadline(LocalDate.now().plusDays(1).atTime(23, 59, 59));
        job.setOfferResponseDeadline(LocalDate.now().plusDays(2).atTime(23, 59, 59));
        when(systemConfigService.getConfig()).thenReturn(new SystemConfig());

        // When
        jobService.validateOfferResponseForPublishedJob(job);

        // Then
        assertTrue(true);
    }

    @Test
    void validateOfferResponseForPublishedJob_WhenOfferDeadlineMissing_ThrowsIllegalArgumentException() {
        // 测试场景：已发布岗位缺少 offer 响应截止时间，预期抛出非法参数异常
        // Given
        Job job = createJob(2001L, 3001L, "EBU6304", "OPEN");
        job.setApplicationDeadline(LocalDate.now().plusDays(1).atTime(23, 59, 59));
        job.setOfferResponseDeadline(null);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jobService.validateOfferResponseForPublishedJob(job));

        // Then
        assertTrue(exception.getMessage().contains("Offer response deadline is required"));
    }

    @Test
    void autoCloseExpiredJobs_WhenOpenJobIsExpired_ClosesJobAndReturnsCount() {
        // 测试场景：存在已过期开放岗位，预期自动关闭并返回关闭数量
        // Given
        Job expiredJob = createJob(2001L, 3001L, "EBU6304", "OPEN");
        expiredJob.setApplicationDeadline(LocalDateTime.now().minusDays(1));
        when(jobDAO.findAll()).thenReturn(List.of(expiredJob));
        when(jobDAO.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int result = jobService.autoCloseExpiredJobs();

        // Then
        assertEquals(1, result);
        assertEquals("CLOSED", expiredJob.getStatus());
        verify(applicationService).processExpiredApplicationsForJob(2001L);
    }

    @Test
    void autoCloseExpiredJobs_WhenNoExpiredOpenJobs_ReturnsZero() {
        // 测试场景：没有已过期开放岗位，预期返回 0
        // Given
        Job activeJob = createJob(2001L, 3001L, "EBU6304", "OPEN");
        activeJob.setApplicationDeadline(LocalDateTime.now().plusDays(1));
        when(jobDAO.findAll()).thenReturn(List.of(activeJob));

        // When
        int result = jobService.autoCloseExpiredJobs();

        // Then
        assertEquals(0, result);
    }

    @Test
    void listAllApplications_WhenApplicationsExist_ReturnsApplications() {
        // 测试场景：查询全部申请时存在数据，预期返回申请列表
        // Given
        Application application = new Application();
        application.setApplicationId(3001L);
        when(applicationDAO.findAll()).thenReturn(List.of(application));

        // When
        List<Application> result = jobService.listAllApplications();

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void updateApplication_WhenApplicationProvided_SavesApplication() {
        // 测试场景：更新申请对象，预期委托 DAO 保存
        // Given
        Application application = new Application();
        application.setApplicationId(3001L);

        // When
        jobService.updateApplication(application);

        // Then
        verify(applicationDAO).save(application);
    }

    private Job createJob(Long jobId, Long moUserId, String moduleCode, String status) {
        Job job = new Job();
        job.setJobId(jobId);
        job.setMoUserId(moUserId);
        job.setModuleCode(moduleCode);
        job.setTitle("Teaching Assistant");
        job.setStatus(status);
        job.setWeeklyHours(10);
        job.setCreatedAt(LocalDateTime.now());
        return job;
    }
}
