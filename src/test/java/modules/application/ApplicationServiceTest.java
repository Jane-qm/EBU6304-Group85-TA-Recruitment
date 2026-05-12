package modules.application;

import modules.config.SystemConfig;
import modules.config.SystemConfigService;
import modules.cv.CVInfo;
import modules.cv.CVService;
import modules.job.Job;
import modules.job.JobService;
import modules.notification.NotificationService;
import modules.profile.TAProfile;
import modules.profile.TAProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private JobService jobService;

    @Mock
    private TAProfileService taProfileService;

    @Mock
    private CVService cvService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(
                applicationDAO,
                jobService,
                taProfileService,
                cvService,
                notificationService,
                systemConfigService
        );
    }

    @Test
    void createOrUpdate_WhenAppliedAtIsNull_SetsAppliedAtAndSaves() {
        // 测试场景：创建申请时 appliedAt 为空，预期自动补时间并保存
        // Given
        Application application = new Application();
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Application result = applicationService.createOrUpdate(application);

        // Then
        assertNotNull(result.getAppliedAt());
        verify(applicationDAO).save(application);
    }

    @Test
    void createOrUpdate_WhenAppliedAtAlreadyExists_PreservesAppliedAt() {
        // 测试场景：更新申请时 appliedAt 已存在，预期保留原时间并保存
        // Given
        Application application = new Application();
        LocalDateTime appliedAt = LocalDateTime.now().minusDays(1);
        application.setAppliedAt(appliedAt);
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Application result = applicationService.createOrUpdate(application);

        // Then
        assertEquals(appliedAt, result.getAppliedAt());
    }

    @Test
    void listAll_WhenApplicationsExist_ReturnsApplications() {
        // 测试场景：查询全部申请时存在数据，预期返回申请列表
        // Given
        when(applicationDAO.findAll()).thenReturn(List.of(new Application(), new Application()));

        // When
        List<Application> result = applicationService.listAll();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void listAll_WhenNoApplicationsExist_ReturnsEmptyList() {
        // 测试场景：查询全部申请时无数据，预期返回空列表
        // Given
        when(applicationDAO.findAll()).thenReturn(List.of());

        // When
        List<Application> result = applicationService.listAll();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void listByTaUserId_WhenMatchingApplicationsExist_ReturnsFilteredApplications() {
        // 测试场景：按 TA ID 查询存在匹配申请，预期返回过滤后的列表
        // Given
        Application a1 = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Application a2 = createApplication(3002L, 1002L, 2002L, ApplicationStatus.SUBMITTED);
        when(applicationDAO.findAll()).thenReturn(List.of(a1, a2));

        // When
        List<Application> result = applicationService.listByTaUserId(1001L);

        // Then
        assertEquals(1, result.size());
        assertEquals(1001L, result.get(0).getTaUserId());
    }

    @Test
    void listByTaUserId_WhenTaUserIdIsNull_ReturnsEmptyList() {
        // 测试场景：按空 TA ID 查询，预期返回空列表
        // Given
        when(applicationDAO.findAll()).thenReturn(List.of(createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED)));

        // When
        List<Application> result = applicationService.listByTaUserId(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void listByJobId_WhenMatchingApplicationsExist_ReturnsFilteredApplications() {
        // 测试场景：按岗位 ID 查询存在匹配申请，预期返回过滤后的列表
        // Given
        Application a1 = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Application a2 = createApplication(3002L, 1002L, 2002L, ApplicationStatus.SUBMITTED);
        when(applicationDAO.findAll()).thenReturn(List.of(a1, a2));

        // When
        List<Application> result = applicationService.listByJobId(2001L);

        // Then
        assertEquals(1, result.size());
        assertEquals(2001L, result.get(0).getJobId());
    }

    @Test
    void listByJobId_WhenJobIdIsNull_ReturnsEmptyList() {
        // 测试场景：按空岗位 ID 查询，预期返回空列表
        // Given
        when(applicationDAO.findAll()).thenReturn(List.of(createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED)));

        // When
        List<Application> result = applicationService.listByJobId(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_WhenApplicationExists_ReturnsApplication() {
        // 测试场景：按申请 ID 查询存在申请，预期返回申请对象
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        when(applicationDAO.findAll()).thenReturn(List.of(application));

        // When
        Application result = applicationService.findById(3001L);

        // Then
        assertEquals(application, result);
    }

    @Test
    void findById_WhenApplicationDoesNotExist_ReturnsNull() {
        // 测试场景：按申请 ID 查询不存在申请，预期返回 null
        // Given
        when(applicationDAO.findAll()).thenReturn(List.of());

        // When
        Application result = applicationService.findById(3001L);

        // Then
        assertEquals(null, result);
    }

    @Test
    void listByTaUserIdSorted_WhenApplicationsHaveDifferentTimes_ReturnsDescendingOrder() {
        // 测试场景：同一 TA 有不同提交时间申请，预期按提交时间倒序返回
        // Given
        Application older = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        older.setAppliedAt(LocalDateTime.now().minusDays(1));
        Application newer = createApplication(3002L, 1001L, 2002L, ApplicationStatus.SUBMITTED);
        newer.setAppliedAt(LocalDateTime.now());
        when(applicationDAO.findAll()).thenReturn(List.of(older, newer));

        // When
        List<Application> result = applicationService.listByTaUserIdSorted(1001L);

        // Then
        assertEquals(3002L, result.get(0).getApplicationId());
        assertEquals(3001L, result.get(1).getApplicationId());
    }

    @Test
    void getActiveApplicationCount_WhenActiveAndInactiveApplicationsExist_ReturnsActiveCount() {
        // 测试场景：同一 TA 同时存在活跃和非活跃申请，预期只统计活跃申请数
        // Given
        Application active1 = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Application active2 = createApplication(3002L, 1001L, 2002L, ApplicationStatus.HIRED);
        Application inactive = createApplication(3003L, 1001L, 2003L, ApplicationStatus.REJECTED);
        when(applicationDAO.findAll()).thenReturn(List.of(active1, active2, inactive));

        // When
        int result = applicationService.getActiveApplicationCount(1001L);

        // Then
        assertEquals(2, result);
    }

    @Test
    void validateApplicationAccess_WhenApplicationWindowClosed_ThrowsIllegalStateException() {
        // 测试场景：招聘周期关闭时验证申请权限，预期由于岗位不可用或资料校验失败抛异常
        // Given
        doNothing().when(taProfileService).refreshProfile(1001L);
        when(taProfileService.getProfileByTaId(1001L)).thenReturn(null);
        when(taProfileService.isProfileComplete(1001L)).thenReturn(false);

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.validateApplicationAccess(1001L, 2001L));

        // Then
        assertTrue(exception.getMessage().contains("Please complete your TA profile before applying."));
    }

    @Test
    void validateApplicationAccess_WhenAllChecksPass_ReturnsWithoutException() {
        // 测试场景：申请权限校验全部通过，预期不抛异常
        // Given
        Job job = createOpenJob(2001L, LocalDateTime.now().plusDays(1));
        TAProfile profile = new TAProfile(1001L, "student@qmul.ac.uk");
        doNothing().when(taProfileService).refreshProfile(1001L);
        when(taProfileService.getProfileByTaId(1001L)).thenReturn(profile);
        when(taProfileService.isProfileComplete(1001L)).thenReturn(true);
        when(cvService.hasCV(1001L)).thenReturn(true);
        when(jobService.getPublishedJob(2001L)).thenReturn(job);

        // When
        applicationService.validateApplicationAccess(1001L, 2001L);

        // Then
        assertTrue(true);
    }

    @Test
    void submitApplication_WhenRecruitmentClosed_ThrowsIllegalStateException() {
        // 测试场景：招聘周期关闭时提交申请，预期抛出非法状态异常
        // Given
        SystemConfig config = new SystemConfig();
        when(systemConfigService.isWithinApplicationCycle(any(LocalDateTime.class))).thenReturn(false);
        when(systemConfigService.getConfig()).thenReturn(config);

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.submitApplication(1001L, 2001L, "statement", 5001L));

        // Then
        assertTrue(exception.getMessage().contains("Applications are currently closed."));
    }

    @Test
    void submitApplication_WhenProfileIncomplete_ThrowsIllegalStateException() {
        // 测试场景：TA 资料未完善时提交申请，预期抛出非法状态异常
        // Given
        when(systemConfigService.isWithinApplicationCycle(any(LocalDateTime.class))).thenReturn(true);
        doNothing().when(taProfileService).refreshProfile(1001L);
        when(taProfileService.getProfileByTaId(1001L)).thenReturn(new TAProfile(1001L, "student@qmul.ac.uk"));
        when(taProfileService.isProfileComplete(1001L)).thenReturn(false);
        when(taProfileService.getMissingFields(1001L)).thenReturn(List.of("Student ID"));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.submitApplication(1001L, 2001L, "statement", 5001L));

        // Then
        assertTrue(exception.getMessage().contains("Please complete your TA profile before applying."));
    }

    @Test
    void submitApplication_WhenJobClosedOrUnavailable_ThrowsIllegalStateException() {
        // 测试场景：岗位已关闭或不可申请时提交申请，预期抛出非法状态异常
        // Given
        when(systemConfigService.isWithinApplicationCycle(any(LocalDateTime.class))).thenReturn(true);
        doNothing().when(taProfileService).refreshProfile(1001L);
        when(taProfileService.getProfileByTaId(1001L)).thenReturn(new TAProfile(1001L, "student@qmul.ac.uk"));
        when(taProfileService.isProfileComplete(1001L)).thenReturn(true);
        when(cvService.getCVById(1001L, 5001L)).thenReturn(createCvInfo(1001L, 5001L));
        when(jobService.getPublishedJob(2001L)).thenReturn(null);

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.submitApplication(1001L, 2001L, "statement", 5001L));

        // Then
        assertTrue(exception.getMessage().contains("This position is not available for application."));
    }

    @Test
    void submitApplication_WhenDuplicateActiveApplicationExists_ThrowsIllegalStateException() {
        // 测试场景：同一岗位已存在活跃申请时重复提交，预期抛出非法状态异常
        // Given
        Job job = createOpenJob(2001L, LocalDateTime.now().plusDays(1));
        Application existing = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        when(systemConfigService.isWithinApplicationCycle(any(LocalDateTime.class))).thenReturn(true);
        doNothing().when(taProfileService).refreshProfile(1001L);
        when(taProfileService.getProfileByTaId(1001L)).thenReturn(new TAProfile(1001L, "student@qmul.ac.uk"));
        when(taProfileService.isProfileComplete(1001L)).thenReturn(true);
        when(cvService.getCVById(1001L, 5001L)).thenReturn(createCvInfo(1001L, 5001L));
        when(jobService.getPublishedJob(2001L)).thenReturn(job);
        when(applicationDAO.findAll()).thenReturn(List.of(existing));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.submitApplication(1001L, 2001L, "statement", 5001L));

        // Then
        assertTrue(exception.getMessage().contains("already have an active application"));
    }

    @Test
    void submitApplication_WhenActiveApplicationCountExceedsThree_ThrowsIllegalStateException() {
        // 测试场景：有效申请数已超过 3 个时提交新申请，预期抛出非法状态异常
        // Given
        Job job = createOpenJob(2004L, LocalDateTime.now().plusDays(1));
        Application a1 = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Application a2 = createApplication(3002L, 1001L, 2002L, ApplicationStatus.WAITLISTED);
        Application a3 = createApplication(3003L, 1001L, 2003L, ApplicationStatus.OFFER_SENT);
        when(systemConfigService.isWithinApplicationCycle(any(LocalDateTime.class))).thenReturn(true);
        doNothing().when(taProfileService).refreshProfile(1001L);
        when(taProfileService.getProfileByTaId(1001L)).thenReturn(new TAProfile(1001L, "student@qmul.ac.uk"));
        when(taProfileService.isProfileComplete(1001L)).thenReturn(true);
        when(cvService.getCVById(1001L, 5001L)).thenReturn(createCvInfo(1001L, 5001L));
        when(jobService.getPublishedJob(2004L)).thenReturn(job);
        when(applicationDAO.findAll()).thenReturn(List.of(a1, a2, a3));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.submitApplication(1001L, 2004L, "statement", 5001L));

        // Then
        assertTrue(exception.getMessage().contains("only have 3 active applications"));
    }

    @Test
    void submitApplication_WhenAllChecksPass_ReturnsSubmittedApplication() {
        // 测试场景：申请校验全部通过，预期创建并保存 SUBMITTED 状态申请
        // Given
        Job job = createOpenJob(2001L, LocalDateTime.now().plusDays(1));
        when(systemConfigService.isWithinApplicationCycle(any(LocalDateTime.class))).thenReturn(true);
        doNothing().when(taProfileService).refreshProfile(1001L);
        when(taProfileService.getProfileByTaId(1001L)).thenReturn(new TAProfile(1001L, "student@qmul.ac.uk"));
        when(taProfileService.isProfileComplete(1001L)).thenReturn(true);
        when(cvService.getCVById(1001L, 5001L)).thenReturn(createCvInfo(1001L, 5001L));
        when(jobService.getPublishedJob(2001L)).thenReturn(job);
        when(applicationDAO.findAll()).thenReturn(List.of());
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Application result = applicationService.submitApplication(1001L, 2001L, "statement", 5001L);

        // Then
        assertEquals(ApplicationStatus.SUBMITTED, result.getStatus());
        assertEquals(1001L, result.getTaUserId());
        assertEquals(2001L, result.getJobId());
    }

    @Test
    void cancelApplication_WhenStatusIsCancellable_ReturnsCancelledApplication() {
        // 测试场景：取消待审核申请，预期状态变为 CANCELLED 并发送通知
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Job job = createOpenJob(2001L, LocalDateTime.now().plusDays(1));
        when(applicationDAO.findAll()).thenReturn(List.of(application));
        when(jobService.getJobById(2001L)).thenReturn(job);
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Application result = applicationService.cancelApplication(3001L);

        // Then
        assertEquals(ApplicationStatus.CANCELLED, result.getStatus());
        verify(notificationService).notifyUser(anyLong(), any(), any(), any(), any());
    }

    @Test
    void cancelApplication_WhenApplicationDoesNotExist_ThrowsIllegalArgumentException() {
        // 测试场景：取消不存在申请，预期抛出非法参数异常
        // Given
        when(applicationDAO.findAll()).thenReturn(List.of());

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> applicationService.cancelApplication(3001L));

        // Then
        assertTrue(exception.getMessage().contains("Application not found"));
    }

    @Test
    void sendOffer_WhenApplicationIsEligible_ReturnsOfferSentApplication() {
        // 测试场景：MO 对可处理申请发送 Offer，预期申请状态变为 OFFER_SENT
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Job job = createOpenJob(2001L, LocalDateTime.now().plusDays(1));
        job.setWeeklyHours(10);
        job.setOfferResponseDeadline(LocalDateTime.now().plusDays(3));
        when(applicationDAO.findAll()).thenReturn(List.of(application));
        when(jobService.getJobById(2001L)).thenReturn(job);
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Application result = applicationService.sendOffer(3001L, 10);

        // Then
        assertEquals(ApplicationStatus.OFFER_SENT, result.getStatus());
        assertEquals(10, result.getOfferedHours());
        verify(notificationService).notifyUser(anyLong(), any(), any(), any(), any());
    }

    @Test
    void sendOffer_WhenApplicationDoesNotExist_ThrowsIllegalArgumentException() {
        // 测试场景：对不存在申请发送 Offer，预期抛出非法参数异常
        // Given
        when(applicationDAO.findAll()).thenReturn(List.of());

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> applicationService.sendOffer(3001L, 10));

        // Then
        assertTrue(exception.getMessage().contains("Application not found."));
    }

    @Test
    void acceptOffer_WhenOfferIsValid_ReturnsHiredApplication() {
        // 测试场景：TA 接受有效 Offer，预期申请状态变为 HIRED
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.OFFER_SENT);
        application.setOfferExpiryAt(LocalDateTime.now().plusDays(1));
        Job job = createOpenJob(2001L, LocalDateTime.now().plusDays(1));
        when(applicationDAO.findAll()).thenReturn(List.of(application));
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobService.getJobById(2001L)).thenReturn(job);

        // When
        Application result = applicationService.acceptOffer(3001L);

        // Then
        assertEquals(ApplicationStatus.HIRED, result.getStatus());
        assertNotNull(result.getRespondedAt());
    }

    @Test
    void acceptOffer_WhenOfferExpired_ThrowsIllegalStateException() {
        // 测试场景：TA 接受已过期 Offer，预期抛出非法状态异常并标记 EXPIRED
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.OFFER_SENT);
        application.setOfferExpiryAt(LocalDateTime.now().minusDays(1));
        when(applicationDAO.findAll()).thenReturn(List.of(application));
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.acceptOffer(3001L));

        // Then
        assertTrue(exception.getMessage().contains("offer has expired"));
        assertEquals(ApplicationStatus.EXPIRED, application.getStatus());
    }

    @Test
    void rejectOffer_WhenOfferIsValid_ReturnsRejectedApplication() {
        // 测试场景：TA 拒绝有效 Offer，预期申请状态变为 REJECTED
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.OFFER_SENT);
        application.setOfferExpiryAt(LocalDateTime.now().plusDays(1));
        Job job = createOpenJob(2001L, LocalDateTime.now().plusDays(1));
        when(applicationDAO.findAll()).thenReturn(List.of(application));
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobService.getJobById(2001L)).thenReturn(job);

        // When
        Application result = applicationService.rejectOffer(3001L);

        // Then
        assertEquals(ApplicationStatus.REJECTED, result.getStatus());
        assertNotNull(result.getRespondedAt());
    }

    @Test
    void rejectOffer_WhenApplicationNotInOfferSentStatus_ThrowsIllegalStateException() {
        // 测试场景：TA 对非 OFFER_SENT 状态申请拒绝 Offer，预期抛出非法状态异常
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        when(applicationDAO.findAll()).thenReturn(List.of(application));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.rejectOffer(3001L));

        // Then
        assertTrue(exception.getMessage().contains("must be in OFFER_SENT status"));
    }

    @Test
    void autoExpireOffers_WhenExpiredOfferExists_ReturnsExpiredCount() {
        // 测试场景：存在已过期 Offer，预期自动过期并返回数量
        // Given
        Application expiredOffer = createApplication(3001L, 1001L, 2001L, ApplicationStatus.OFFER_SENT);
        expiredOffer.setOfferExpiryAt(LocalDateTime.now().minusDays(1));
        when(applicationDAO.findAll()).thenReturn(List.of(expiredOffer));
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int result = applicationService.autoExpireOffers();

        // Then
        assertEquals(1, result);
        assertEquals(ApplicationStatus.EXPIRED, expiredOffer.getStatus());
    }

    @Test
    void autoExpireOffers_WhenNoExpiredOfferExists_ReturnsZero() {
        // 测试场景：不存在已过期 Offer，预期返回 0
        // Given
        Application activeOffer = createApplication(3001L, 1001L, 2001L, ApplicationStatus.OFFER_SENT);
        activeOffer.setOfferExpiryAt(LocalDateTime.now().plusDays(1));
        when(applicationDAO.findAll()).thenReturn(List.of(activeOffer));

        // When
        int result = applicationService.autoExpireOffers();

        // Then
        assertEquals(0, result);
    }

    @Test
    void processExpiredApplicationsForJob_WhenPendingApplicationsExist_ReturnsProcessedCount() {
        // 测试场景：岗位下存在待处理申请，预期这些申请被标记为 EXPIRED
        // Given
        Application submitted = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Application waitlisted = createApplication(3002L, 1002L, 2001L, ApplicationStatus.WAITLISTED);
        when(applicationDAO.findAll()).thenReturn(List.of(submitted, waitlisted));
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int result = applicationService.processExpiredApplicationsForJob(2001L);

        // Then
        assertEquals(2, result);
        assertEquals(ApplicationStatus.EXPIRED, submitted.getStatus());
        assertEquals(ApplicationStatus.EXPIRED, waitlisted.getStatus());
    }

    @Test
    void processExpiredApplicationsForJob_WhenNoPendingApplicationsExist_ReturnsZero() {
        // 测试场景：岗位下不存在待处理申请，预期返回 0
        // Given
        Application rejected = createApplication(3001L, 1001L, 2001L, ApplicationStatus.REJECTED);
        when(applicationDAO.findAll()).thenReturn(List.of(rejected));

        // When
        int result = applicationService.processExpiredApplicationsForJob(2001L);

        // Then
        assertEquals(0, result);
    }

    @Test
    void markAsWaitlisted_WhenApplicationIsSubmitted_ReturnsWaitlistedApplication() {
        // 测试场景：MO 将已提交申请加入候选名单，预期状态变为 WAITLISTED
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        when(applicationDAO.findAll()).thenReturn(List.of(application));
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Application result = applicationService.markAsWaitlisted(3001L);

        // Then
        assertEquals(ApplicationStatus.WAITLISTED, result.getStatus());
    }

    @Test
    void markAsWaitlisted_WhenApplicationNotSubmitted_ThrowsIllegalStateException() {
        // 测试场景：MO 对非 SUBMITTED 申请加入候选名单，预期抛出非法状态异常
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.REJECTED);
        when(applicationDAO.findAll()).thenReturn(List.of(application));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.markAsWaitlisted(3001L));

        // Then
        assertTrue(exception.getMessage().contains("Cannot mark as waitlisted"));
    }

    @Test
    void rejectApplication_WhenApplicationIsSubmitted_ReturnsRejectedApplication() {
        // 测试场景：MO 拒绝已提交申请，预期状态变为 REJECTED
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        when(applicationDAO.findAll()).thenReturn(List.of(application));
        when(applicationDAO.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Application result = applicationService.rejectApplication(3001L);

        // Then
        assertEquals(ApplicationStatus.REJECTED, result.getStatus());
    }

    @Test
    void rejectApplication_WhenApplicationIsTerminal_ThrowsIllegalStateException() {
        // 测试场景：MO 拒绝已终态申请，预期抛出非法状态异常
        // Given
        Application application = createApplication(3001L, 1001L, 2001L, ApplicationStatus.HIRED);
        when(applicationDAO.findAll()).thenReturn(List.of(application));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> applicationService.rejectApplication(3001L));

        // Then
        assertTrue(exception.getMessage().contains("Cannot reject application"));
    }

    @Test
    void listByStatus_WhenMatchingStatusExists_ReturnsFilteredApplications() {
        // 测试场景：按状态查询存在匹配申请，预期返回过滤后的列表
        // Given
        Application submitted = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Application hired = createApplication(3002L, 1002L, 2002L, ApplicationStatus.HIRED);
        when(applicationDAO.findAll()).thenReturn(List.of(submitted, hired));

        // When
        List<Application> result = applicationService.listByStatus(ApplicationStatus.SUBMITTED);

        // Then
        assertEquals(1, result.size());
        assertEquals(ApplicationStatus.SUBMITTED, result.get(0).getStatus());
    }

    @Test
    void listWaitlistedByJobId_WhenWaitlistedApplicationsExist_ReturnsSortedApplications() {
        // 测试场景：同一岗位存在候选申请，预期按申请时间升序返回
        // Given
        Application older = createApplication(3001L, 1001L, 2001L, ApplicationStatus.WAITLISTED);
        older.setAppliedAt(LocalDateTime.now().minusDays(1));
        Application newer = createApplication(3002L, 1002L, 2001L, ApplicationStatus.WAITLISTED);
        newer.setAppliedAt(LocalDateTime.now());
        when(applicationDAO.findAll()).thenReturn(List.of(newer, older));

        // When
        List<Application> result = applicationService.listWaitlistedByJobId(2001L);

        // Then
        assertEquals(3001L, result.get(0).getApplicationId());
        assertEquals(3002L, result.get(1).getApplicationId());
    }

    @Test
    void listApplicationsAwaitingReview_WhenSubmittedApplicationsExist_ReturnsSubmittedOnly() {
        // 测试场景：存在待审核和非待审核申请，预期只返回 SUBMITTED 列表
        // Given
        Application submitted = createApplication(3001L, 1001L, 2001L, ApplicationStatus.SUBMITTED);
        Application hired = createApplication(3002L, 1002L, 2002L, ApplicationStatus.HIRED);
        when(applicationDAO.findAll()).thenReturn(List.of(submitted, hired));

        // When
        List<Application> result = applicationService.listApplicationsAwaitingReview();

        // Then
        assertEquals(1, result.size());
        assertEquals(ApplicationStatus.SUBMITTED, result.get(0).getStatus());
    }

    @Test
    void buildApplicationSummary_WhenApplicationIsNull_ReturnsPlaceholder() {
        // 测试场景：构建空申请摘要，预期返回占位提示
        // Given
        Application application = null;

        // When
        String result = applicationService.buildApplicationSummary(application);

        // Then
        assertEquals("No application selected.", result);
    }

    private Application createApplication(Long applicationId, Long taUserId, Long jobId, String status) {
        Application application = new Application();
        application.setApplicationId(applicationId);
        application.setTaUserId(taUserId);
        application.setJobId(jobId);
        application.setStatus(status);
        application.setAppliedAt(LocalDateTime.now());
        application.setStatement("I want to teach");
        return application;
    }

    private Job createOpenJob(Long jobId, LocalDateTime deadline) {
        Job job = new Job();
        job.setJobId(jobId);
        job.setMoUserId(5001L);
        job.setModuleCode("EBU6304");
        job.setTitle("Teaching Assistant");
        job.setStatus("OPEN");
        job.setApplicationDeadline(deadline);
        job.setOfferResponseDeadline(LocalDateTime.now().plusDays(2));
        job.setWeeklyHours(10);
        return job;
    }

    private CVInfo createCvInfo(Long taId, Long cvId) {
        CVInfo cvInfo = new CVInfo(taId, "student@qmul.ac.uk", "Student");
        cvInfo.setCvId(cvId);
        cvInfo.setCvName("Resume");
        return cvInfo;
    }
}
