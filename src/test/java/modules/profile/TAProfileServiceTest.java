package modules.profile;

import modules.user.TA;
import modules.user.User;
import modules.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TAProfileServiceTest {

    @Mock
    private TAProfileDAO profileDAO;

    @Mock
    private UserService userService;

    @InjectMocks
    private TAProfileService taProfileService;

    @BeforeEach
    void setUp() {
        taProfileService = new TAProfileService(profileDAO, userService);
    }

    @Test
    void getProfileByTaId_WhenProfileExists_ReturnsProfile() {
        // 测试场景：按 TA ID 查询到资料，预期返回对应资料对象
        // Given
        TAProfile profile = new TAProfile(1001L, "student@qmul.ac.uk");
        when(profileDAO.findByTaId(1001L)).thenReturn(profile);

        // When
        TAProfile result = taProfileService.getProfileByTaId(1001L);

        // Then
        assertEquals(profile, result);
    }

    @Test
    void getProfileByTaId_WhenProfileDoesNotExist_ReturnsNull() {
        // 测试场景：按 TA ID 未查询到资料，预期返回 null
        // Given
        when(profileDAO.findByTaId(1001L)).thenReturn(null);

        // When
        TAProfile result = taProfileService.getProfileByTaId(1001L);

        // Then
        assertEquals(null, result);
    }

    @Test
    void getProfileByEmail_WhenProfileExists_ReturnsProfile() {
        // 测试场景：按邮箱查询到资料，预期返回对应资料对象
        // Given
        TAProfile profile = new TAProfile(1001L, "student@qmul.ac.uk");
        when(profileDAO.findByEmail("student@qmul.ac.uk")).thenReturn(profile);

        // When
        TAProfile result = taProfileService.getProfileByEmail("student@qmul.ac.uk");

        // Then
        assertEquals(profile, result);
    }

    @Test
    void getProfileByUser_WhenProfileNotFound_CreatesAndSavesNewProfile() {
        // 测试场景：按用户查询时资料不存在，预期创建新资料并保存
        // Given
        TA user = new TA("student@qmul.ac.uk", "Password123");
        user.setUserId(1001L);
        when(profileDAO.findByTaId(1001L)).thenReturn(null);
        when(profileDAO.findByEmail("student@qmul.ac.uk")).thenReturn(null);

        // When
        TAProfile result = taProfileService.getProfileByUser(user);

        // Then
        assertNotNull(result);
        assertEquals(1001L, result.getTaId());
        verify(profileDAO).save(result);
    }

    @Test
    void getProfileByUser_WhenUserIsNull_ReturnsNull() {
        // 测试场景：传入空用户，预期返回 null
        // Given
        User user = null;

        // When
        TAProfile result = taProfileService.getProfileByUser(user);

        // Then
        assertEquals(null, result);
    }

    @Test
    void saveProfile_WhenProfileIsValid_SavesProfile() {
        // 测试场景：保存合法且完整的 TA 资料，预期调用 DAO 持久化
        // Given
        TA user = new TA("student@qmul.ac.uk", "Password123");
        user.setUserId(1001L);
        TAProfile profile = createCompleteProfile(1001L, "old-email@test.com");
        when(userService.findById(1001L)).thenReturn(user);

        // When
        taProfileService.saveProfile(profile);

        // Then
        assertEquals("student@qmul.ac.uk", profile.getEmail());
        assertTrue(profile.isProfileCompleted());
        verify(profileDAO).save(profile);
    }

    @Test
    void saveProfile_WhenProfileIsNull_ThrowsIllegalArgumentException() {
        // 测试场景：保存资料时传入 null，预期抛出非法参数异常
        // Given
        TAProfile profile = null;

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taProfileService.saveProfile(profile));

        // Then
        assertTrue(exception.getMessage().contains("Profile cannot be null"));
    }

    @Test
    void updateProfile_WhenProfileExists_PreservesCreatedAtAndSaves() {
        // 测试场景：更新已存在资料，预期保留原 createdAt 并保存
        // Given
        TAProfile existing = new TAProfile(1001L, "student@qmul.ac.uk");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        existing.setCreatedAt(createdAt);
        TAProfile incoming = new TAProfile(1001L, "student@qmul.ac.uk");
        when(profileDAO.findByTaId(1001L)).thenReturn(existing);

        // When
        taProfileService.updateProfile(incoming);

        // Then
        assertEquals(createdAt, incoming.getCreatedAt());
        verify(profileDAO).save(incoming);
    }

    @Test
    void updateProfile_WhenProfileDoesNotExist_ThrowsIllegalArgumentException() {
        // 测试场景：更新的资料不存在，预期抛出非法参数异常
        // Given
        TAProfile incoming = new TAProfile(1001L, "student@qmul.ac.uk");
        when(profileDAO.findByTaId(1001L)).thenReturn(null);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taProfileService.updateProfile(incoming));

        // Then
        assertTrue(exception.getMessage().contains("Profile not found"));
    }

    @Test
    void isProfileComplete_WhenProfileCompleted_ReturnsTrue() {
        // 测试场景：资料已完整填写，预期返回 true
        // Given
        TAProfile profile = createCompleteProfile(1001L, "student@qmul.ac.uk");
        profile.saveProfile();
        when(profileDAO.findByTaId(1001L)).thenReturn(profile);

        // When
        boolean result = taProfileService.isProfileComplete(1001L);

        // Then
        assertTrue(result);
    }

    @Test
    void isProfileComplete_WhenProfileMissing_ReturnsFalse() {
        // 测试场景：资料不存在，预期返回 false
        // Given
        when(profileDAO.findByTaId(1001L)).thenReturn(null);

        // When
        boolean result = taProfileService.isProfileComplete(1001L);

        // Then
        assertFalse(result);
    }

    @Test
    void getProfileCompletion_WhenProfileIsComplete_ReturnsExpectedPercentage() {
        // 测试场景：资料字段完整，预期返回 100 完善度
        // Given
        TAProfile profile = createCompleteProfile(1001L, "student@qmul.ac.uk");
        profile.setChineseName("张三");
        profile.setPreviousExperience("TA before");
        when(profileDAO.findByTaId(1001L)).thenReturn(profile);

        // When
        int result = taProfileService.getProfileCompletion(1001L);

        // Then
        assertEquals(100, result);
    }

    @Test
    void getProfileCompletion_WhenRequiredFieldsMissing_ReturnsLessThanHundred() {
        // 测试场景：必填字段缺失，预期完善度小于 100
        // Given
        TAProfile profile = new TAProfile(1001L, "student@qmul.ac.uk");
        profile.setStudentId("12345678");
        when(profileDAO.findByTaId(1001L)).thenReturn(profile);

        // When
        int result = taProfileService.getProfileCompletion(1001L);

        // Then
        assertTrue(result < 100);
    }

    @Test
    void getMissingFields_WhenProfileMissing_ReturnsProfileNotFoundMessage() {
        // 测试场景：资料不存在，预期返回包含 Profile not found 的列表
        // Given
        when(profileDAO.findByTaId(1001L)).thenReturn(null);

        // When
        List<String> result = taProfileService.getMissingFields(1001L);

        // Then
        assertEquals(List.of("Profile not found"), result);
    }

    @Test
    void initializeProfile_WhenInputsAreNull_ThrowsIllegalArgumentException() {
        // 测试场景：初始化资料时 taId 或邮箱为空，预期抛出非法参数异常
        // Given
        Long taId = null;

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taProfileService.initializeProfile(taId, null));

        // Then
        assertTrue(exception.getMessage().contains("TA ID and email cannot be null"));
    }

    @Test
    void refreshProfile_WhenTaIdProvided_RefreshesFromDao() {
        // 测试场景：强制刷新指定 TA 的资料缓存，预期调用 DAO 刷新
        // Given
        Long taId = 1001L;

        // When
        taProfileService.refreshProfile(taId);

        // Then
        verify(profileDAO).refreshFromFile(taId);
    }

    @Test
    void getProfileAndRefresh_WhenProfileExists_ReturnsRefreshedProfile() {
        // 测试场景：刷新并查询资料，预期返回 DAO 中最新资料
        // Given
        TAProfile profile = new TAProfile(1001L, "student@qmul.ac.uk");
        when(profileDAO.findByTaId(1001L)).thenReturn(profile);

        // When
        TAProfile result = taProfileService.getProfileAndRefresh(1001L);

        // Then
        assertEquals(profile, result);
        verify(profileDAO).refreshFromFile(1001L);
    }

    @Test
    void deleteProfile_WhenTaIdProvided_DelegatesToDao() {
        // 测试场景：删除指定 TA 资料，预期调用 DAO 删除
        // Given
        Long taId = 1001L;

        // When
        taProfileService.deleteProfile(taId);

        // Then
        verify(profileDAO).delete(taId);
    }

    @Test
    void getAllProfiles_WhenProfilesExist_ReturnsAllProfiles() {
        // 测试场景：DAO 中存在多条资料，预期返回全部资料列表
        // Given
        List<TAProfile> profiles = List.of(
                new TAProfile(1001L, "student1@qmul.ac.uk"),
                new TAProfile(1002L, "student2@qmul.ac.uk")
        );
        when(profileDAO.findAll()).thenReturn(profiles);

        // When
        List<TAProfile> result = taProfileService.getAllProfiles();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void getCompletedProfiles_WhenCompletedProfilesExist_ReturnsCompletedProfiles() {
        // 测试场景：DAO 中存在已完成资料，预期返回完成资料列表
        // Given
        TAProfile completed = createCompleteProfile(1001L, "student@qmul.ac.uk");
        completed.saveProfile();
        when(profileDAO.findCompletedProfiles()).thenReturn(List.of(completed));

        // When
        List<TAProfile> result = taProfileService.getCompletedProfiles();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isProfileCompleted());
    }

    private TAProfile createCompleteProfile(Long taId, String email) {
        TAProfile profile = new TAProfile(taId, email);
        profile.setStudentId("240001");
        profile.setSurname("Zhang");
        profile.setForename("San");
        profile.setChineseName("张三");
        profile.setPhone("13812345678");
        profile.setGender(TAProfile.Gender.MALE);
        profile.setSchool("BUPT");
        profile.setSupervisor("Dr. Smith");
        profile.setStudentType(TAProfile.StudentType.MASTER);
        profile.setCurrentYear(TAProfile.Year.YEAR_1);
        profile.setCampus(TAProfile.Campus.SHAHE);
        profile.setPreviousExperience("Lab assistant");
        return profile;
    }
}
