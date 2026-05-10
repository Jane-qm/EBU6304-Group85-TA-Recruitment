package modules.cv;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CVInfoTest {

    @Test
    void fileType_fromExtension() {
        assertEquals(CVInfo.FileType.PDF, CVInfo.FileType.fromExtension("pdf"));
        assertEquals(CVInfo.FileType.DOCX, CVInfo.FileType.fromExtension("DOCX"));
        assertNull(CVInfo.FileType.fromExtension("exe"));
    }

    @Test
    void isFileSizeValid_bounds() {
        assertFalse(CVInfo.isFileSizeValid(0));
        assertTrue(CVInfo.isFileSizeValid(1));
        assertTrue(CVInfo.isFileSizeValid(CVInfo.MAX_FILE_SIZE));
        assertFalse(CVInfo.isFileSizeValid(CVInfo.MAX_FILE_SIZE + 1));
    }

    @Test
    void getFileExtension() {
        assertEquals("pdf", CVInfo.getFileExtension("a.PDF"));
        assertEquals("", CVInfo.getFileExtension("noext"));
        assertEquals("", CVInfo.getFileExtension(null));
    }

    @Test
    void isFileTypeSupported() {
        assertTrue(CVInfo.isFileTypeSupported("cv.docx"));
        assertFalse(CVInfo.isFileTypeSupported("x.exe"));
        assertFalse(CVInfo.isFileTypeSupported(null));
    }

    @Test
    void isCvNameValid() {
        assertFalse(CVInfo.isCvNameValid(null));
        assertFalse(CVInfo.isCvNameValid("   "));
        assertFalse(CVInfo.isCvNameValid("a".repeat(51)));
        assertTrue(CVInfo.isCvNameValid("My CV"));
    }

    @Test
    void getFileSizeDisplay_units() {
        CVInfo cv = new CVInfo();
        cv.setFileSize(512);
        assertTrue(cv.getFileSizeDisplay().contains("B"));
        cv.setFileSize(2048);
        assertTrue(cv.getFileSizeDisplay().contains("KB"));
        cv.setFileSize(2 * 1024 * 1024);
        assertTrue(cv.getFileSizeDisplay().contains("MB"));
    }

    @Test
    void generateSavedFileName_containsTaAndExtension() {
        CVInfo cv = new CVInfo(99L, "t@qmul.ac.uk", "Name");
        cv.setCvName("Research CV");
        cv.setFileType(CVInfo.FileType.PDF);
        cv.setUploadedAt(LocalDateTime.of(2026, 5, 10, 14, 30, 0));
        String name = cv.generateSavedFileName();
        assertTrue(name.startsWith("ta_99_"));
        assertTrue(name.endsWith(".pdf"));
        assertTrue(name.contains("Research"));
    }
}
