package modules.cv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Early validation paths in {@link CVService#uploadCV} before file persistence (no successful upload).
 */
class CVServiceUploadValidationTest {

    private final CVService cvService = new CVService();

    @Test
    void uploadCV_nullTaId_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cvService.uploadCV(null, "e@test.uk", "N", "My CV", "", "doc.pdf", new byte[]{1}));
        assertTrue(ex.getMessage().contains("TA ID"));
    }

    @Test
    void uploadCV_invalidCvName_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cvService.uploadCV(1L, "e@test.uk", "N", "", "", "doc.pdf", new byte[]{1}));
        assertTrue(ex.getMessage().contains("CV name") || ex.getMessage().contains("name"));
    }

    @Test
    void uploadCV_emptyFile_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cvService.uploadCV(1L, "e@test.uk", "N", "ValidName", "", "doc.pdf", new byte[0]));
        assertTrue(ex.getMessage().toLowerCase().contains("file")
                || ex.getMessage().toLowerCase().contains("size"));
    }

    @Test
    void uploadCV_unsupportedExtension_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> cvService.uploadCV(1L, "e@test.uk", "N", "ValidName", "", "virus.exe", new byte[]{1, 2, 3}));
    }
}
