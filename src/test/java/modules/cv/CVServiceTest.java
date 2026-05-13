package modules.cv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CVServiceTest {

    @Mock
    private CVDao cvDao;

    @InjectMocks
    private CVService cvService;

    @BeforeEach
    void setUp() {
        cvService = new CVService(cvDao);
    }

    @Test
    void getCVManager_WhenManagerExists_ReturnsManager() {
        // 测试场景：DAO 中存在指定 TA 的 CV 管理器，预期直接返回该管理器
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        CVManager result = cvService.getCVManager(1L);

        // Then
        assertEquals(manager, result);
    }

    @Test
    void uploadCV_WhenInputIsValid_ReturnsSavedCvInfo() {
        // 测试场景：上传合法 CV，预期创建并保存 CV 信息且首个 CV 被设为默认
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        byte[] fileData = new byte[]{1, 2, 3};
        when(cvDao.getOrCreateCVManager(1L, "ta@test.com", "TA Name")).thenReturn(manager);
        when(cvDao.saveCVFile(1L, "Resume1", "resume.pdf", fileData)).thenReturn("data/cvs/resume.pdf");

        // When
        CVInfo result = cvService.uploadCV(1L, "ta@test.com", "TA Name",
                "Resume1", "Main CV", "resume.pdf", fileData);

        // Then
        assertNotNull(result);
        assertEquals("Resume1", result.getCvName());
        assertEquals("resume.pdf", result.getOriginalFileName());
        assertTrue(result.isDefault());
        verify(cvDao).saveCV(any(CVInfo.class));
        verify(cvDao).refreshFromFile(1L);
    }

    @Test
    void uploadCV_WhenCvNameAlreadyExists_ThrowsIllegalArgumentException() {
        // 测试场景：上传的 CV 名称已存在，预期抛出非法参数异常
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo existing = new CVInfo(1L, "ta@test.com", "TA Name");
        existing.setCvName("Resume1");
        manager.addCV(existing);
        when(cvDao.getOrCreateCVManager(1L, "ta@test.com", "TA Name")).thenReturn(manager);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cvService.uploadCV(1L, "ta@test.com", "TA Name",
                        "Resume1", "Main CV", "resume.pdf", new byte[]{1, 2, 3}));

        // Then
        assertTrue(exception.getMessage().contains("CV name already exists"));
    }

    @Test
    void uploadCV_WhenTaIdIsNull_ThrowsIllegalArgumentException() {
        // 测试场景：上传 CV 时 taId 为空，预期抛出非法参数异常
        // Given
        byte[] fileData = new byte[]{1, 2, 3};

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cvService.uploadCV(null, "ta@test.com", "TA Name",
                        "Resume1", "Main CV", "resume.pdf", fileData));

        // Then
        assertTrue(exception.getMessage().contains("TA ID cannot be null"));
    }

    @Test
    void uploadCV_WhenCvNameInvalid_ThrowsIllegalArgumentException() {
        // 测试场景：上传 CV 时名称非法，预期抛出非法参数异常
        // Given
        byte[] fileData = new byte[]{1, 2, 3};

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cvService.uploadCV(1L, "ta@test.com", "TA Name",
                        "", "Main CV", "resume.pdf", fileData));

        // Then
        assertTrue(exception.getMessage().contains("CV name must be 1-50 characters"));
    }

    @Test
    void uploadCV_WhenFileTypeUnsupported_ThrowsIllegalArgumentException() {
        // 测试场景：上传 CV 文件类型不支持，预期抛出非法参数异常
        // Given
        byte[] fileData = new byte[]{1, 2, 3};

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cvService.uploadCV(1L, "ta@test.com", "TA Name",
                        "Resume1", "Main CV", "resume.txt", fileData));

        // Then
        assertTrue(exception.getMessage().contains("File type not supported"));
    }

    @Test
    void uploadCV_WhenSaveFileFails_ThrowsRuntimeException() {
        // 测试场景：底层文件保存失败，预期抛出运行时异常
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        byte[] fileData = new byte[]{1, 2, 3};
        when(cvDao.getOrCreateCVManager(1L, "ta@test.com", "TA Name")).thenReturn(manager);
        when(cvDao.saveCVFile(1L, "Resume1", "resume.pdf", fileData)).thenReturn(null);

        // When
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cvService.uploadCV(1L, "ta@test.com", "TA Name",
                        "Resume1", "Main CV", "resume.pdf", fileData));

        // Then
        assertTrue(exception.getMessage().contains("Failed to save CV file"));
    }

    @Test
    void downloadCV_WhenManagerMissing_ThrowsIllegalArgumentException() {
        // 测试场景：指定 TA 不存在 CV 管理器，预期抛出非法参数异常
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cvService.downloadCV(1L, 10L));

        // Then
        assertTrue(exception.getMessage().contains("No CV found for TA ID"));
        verify(cvDao).refreshFromFile(1L);
    }

    @Test
    void downloadCV_WhenCvExists_ReturnsFileBytes() {
        // 测试场景：CV 存在且文件可读，预期返回文件字节数组
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo cvInfo = new CVInfo(1L, "ta@test.com", "TA Name");
        cvInfo.setCvId(10L);
        cvInfo.setFilePath("data/cvs/resume.pdf");
        manager.addCV(cvInfo);
        byte[] bytes = new byte[]{9, 8, 7};
        when(cvDao.getCVManager(1L)).thenReturn(manager);
        when(cvDao.readCVFile("data/cvs/resume.pdf")).thenReturn(bytes);

        // When
        byte[] result = cvService.downloadCV(1L, 10L);

        // Then
        assertArrayEquals(bytes, result);
    }

    @Test
    void downloadCV_WhenCvMissing_ThrowsIllegalArgumentException() {
        // 测试场景：管理器存在但指定 CV 不存在，预期抛出非法参数异常
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cvService.downloadCV(1L, 10L));

        // Then
        assertTrue(exception.getMessage().contains("CV not found with ID"));
    }

    @Test
    void downloadDefaultCV_WhenDefaultCvMissing_ThrowsIllegalArgumentException() {
        // 测试场景：TA 没有默认 CV，预期抛出非法参数异常
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cvService.downloadDefaultCV(1L));

        // Then
        assertTrue(exception.getMessage().contains("No default CV found"));
    }

    @Test
    void downloadDefaultCV_WhenManagerMissing_ThrowsIllegalArgumentException() {
        // 测试场景：指定 TA 没有 CV 管理器，预期抛出非法参数异常
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cvService.downloadDefaultCV(1L));

        // Then
        assertTrue(exception.getMessage().contains("No CV found for TA ID"));
    }

    @Test
    void deleteCV_WhenDaoReturnsTrue_ReturnsTrueAndRefreshesCache() {
        // 测试场景：删除 CV 成功，预期返回 true 并刷新缓存
        // Given
        when(cvDao.deleteCV(1L, 10L)).thenReturn(true);

        // When
        boolean result = cvService.deleteCV(1L, 10L);

        // Then
        assertTrue(result);
        verify(cvDao).refreshFromFile(1L);
    }

    @Test
    void setDefaultCV_WhenDaoReturnsFalse_ReturnsFalseAndRefreshesCache() {
        // 测试场景：设置默认 CV 失败，预期返回 false 但仍刷新缓存
        // Given
        when(cvDao.setDefaultCV(1L, 10L)).thenReturn(false);

        // When
        boolean result = cvService.setDefaultCV(1L, 10L);

        // Then
        assertFalse(result);
        verify(cvDao).refreshFromFile(1L);
    }

    @Test
    void setDefaultCV_WhenDaoReturnsTrue_ReturnsTrueAndRefreshesCache() {
        // 测试场景：设置默认 CV 成功，预期返回 true 并刷新缓存
        // Given
        when(cvDao.setDefaultCV(1L, 10L)).thenReturn(true);

        // When
        boolean result = cvService.setDefaultCV(1L, 10L);

        // Then
        assertTrue(result);
        verify(cvDao).refreshFromFile(1L);
    }

    @Test
    void getCVManager_WhenManagerMissing_ReturnsNull() {
        // 测试场景：DAO 中不存在指定 TA 的 CV 管理器，预期返回 null
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        CVManager result = cvService.getCVManager(1L);

        // Then
        assertNull(result);
    }

    @Test
    void getDefaultCV_WhenManagerMissing_ReturnsNull() {
        // 测试场景：管理器不存在，预期返回 null
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        CVInfo result = cvService.getDefaultCV(1L);

        // Then
        assertNull(result);
    }

    @Test
    void getCVNames_WhenManagerMissing_ReturnsEmptyList() {
        // 测试场景：指定 TA 没有 CV 管理器，预期返回空列表
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        List<String> result = cvService.getCVNames(1L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getCVByName_WhenManagerMissing_ReturnsNull() {
        // 测试场景：指定 TA 没有 CV 管理器，预期返回 null
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        CVInfo result = cvService.getCVByName(1L, "Resume1");

        // Then
        assertNull(result);
    }

    @Test
    void hasCV_WhenManagerContainsCv_ReturnsTrue() {
        // 测试场景：CV 管理器中存在 CV，预期返回 true
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo cvInfo = new CVInfo(1L, "ta@test.com", "TA Name");
        cvInfo.setCvName("Resume1");
        manager.addCV(cvInfo);
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        boolean result = cvService.hasCV(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void hasCV_WhenManagerMissing_ReturnsFalse() {
        // 测试场景：不存在 CV 管理器，预期返回 false
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        boolean result = cvService.hasCV(1L);

        // Then
        assertFalse(result);
    }

    @Test
    void hasCV_WhenManagerHasNoCv_ReturnsFalse() {
        // 测试场景：管理器存在但没有 CV，预期返回 false
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        boolean result = cvService.hasCV(1L);

        // Then
        assertFalse(result);
    }

    @Test
    void getCVCount_WhenManagerMissing_ReturnsZero() {
        // 测试场景：指定 TA 没有 CV 管理器，预期返回 0
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        int result = cvService.getCVCount(1L);

        // Then
        assertEquals(0, result);
    }

    @Test
    void getCVCount_WhenManagerExists_ReturnsCount() {
        // 测试场景：管理器存在多个 CV，预期返回正确数量
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo cv1 = new CVInfo(1L, "ta@test.com", "TA Name");
        cv1.setCvName("Resume1");
        CVInfo cv2 = new CVInfo(1L, "ta@test.com", "TA Name");
        cv2.setCvName("Resume2");
        manager.addCV(cv1);
        manager.addCV(cv2);
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        int result = cvService.getCVCount(1L);

        // Then
        assertEquals(2, result);
    }

    @Test
    void getDefaultCV_WhenManagerExists_ReturnsDefaultCv() {
        // 测试场景：管理器存在默认 CV，预期返回该默认 CV
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo defaultCv = new CVInfo(1L, "ta@test.com", "TA Name");
        defaultCv.setCvId(10L);
        defaultCv.setCvName("Resume1");
        defaultCv.setDefault(true);
        manager.addCV(defaultCv);
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        CVInfo result = cvService.getDefaultCV(1L);

        // Then
        assertEquals(defaultCv, result);
    }

    @Test
    void getAllCVs_WhenManagerExists_ReturnsAllCvs() {
        // 测试场景：管理器存在多个 CV，预期返回全部 CV 列表
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo cv1 = new CVInfo(1L, "ta@test.com", "TA Name");
        cv1.setCvName("Resume1");
        CVInfo cv2 = new CVInfo(1L, "ta@test.com", "TA Name");
        cv2.setCvName("Resume2");
        manager.addCV(cv1);
        manager.addCV(cv2);
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        List<CVInfo> result = cvService.getAllCVs(1L);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void getAllCVsSorted_WhenManagerExists_ReturnsSortedCvs() {
        // 测试场景：管理器存在多个不同上传时间的 CV，预期按时间倒序返回
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo older = new CVInfo(1L, "ta@test.com", "TA Name");
        older.setCvId(1L);
        older.setCvName("Older");
        older.setUploadedAt(LocalDateTime.now().minusDays(1));
        CVInfo newer = new CVInfo(1L, "ta@test.com", "TA Name");
        newer.setCvId(2L);
        newer.setCvName("Newer");
        newer.setUploadedAt(LocalDateTime.now());
        manager.addCV(older);
        manager.addCV(newer);
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        List<CVInfo> result = cvService.getAllCVsSorted(1L);

        // Then
        assertEquals("Newer", result.get(0).getCvName());
        assertEquals("Older", result.get(1).getCvName());
    }

    @Test
    void getCVById_WhenCvBelongsToTa_ReturnsCv() {
        // 测试场景：CV 属于指定 TA，预期返回该 CV
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo cvInfo = new CVInfo(1L, "ta@test.com", "TA Name");
        cvInfo.setCvId(10L);
        manager.addCV(cvInfo);
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        CVInfo result = cvService.getCVById(1L, 10L);

        // Then
        assertEquals(cvInfo, result);
    }

    @Test
    void getCVById_WhenCvBelongsToDifferentTa_ReturnsNull() {
        // 测试场景：CV 不属于指定 TA，预期返回 null
        // Given
        CVManager manager = new CVManager(1L, "ta@test.com", "TA Name");
        CVInfo cvInfo = new CVInfo(2L, "other@test.com", "Other TA");
        cvInfo.setCvId(10L);
        manager.addCV(cvInfo);
        when(cvDao.getCVManager(1L)).thenReturn(manager);

        // When
        CVInfo result = cvService.getCVById(1L, 10L);

        // Then
        assertNull(result);
    }

    @Test
    void getCVById_WhenManagerMissing_ReturnsNull() {
        // 测试场景：不存在 CV 管理器，预期返回 null
        // Given
        when(cvDao.getCVManager(1L)).thenReturn(null);

        // When
        CVInfo result = cvService.getCVById(1L, 10L);

        // Then
        assertNull(result);
    }

    @Test
    void refreshCVs_WhenTaIdProvided_RefreshesCache() {
        // 测试场景：主动刷新指定 TA 的 CV 缓存，预期调用 DAO 刷新方法
        // Given
        Long taId = 1L;

        // When
        cvService.refreshCVs(taId);

        // Then
        verify(cvDao).refreshFromFile(taId);
    }
}
