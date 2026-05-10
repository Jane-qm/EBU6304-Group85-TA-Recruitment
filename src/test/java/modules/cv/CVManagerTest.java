package modules.cv;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * In-memory tests for {@link CVManager} list / default / name lookup behaviour.
 */
class CVManagerTest {

    @Test
    void isCvNameExists_and_getCVByName() {
        CVManager m = new CVManager(1L, "ta@test.uk", "TA");
        CVInfo cv = new CVInfo(1L, "ta@test.uk", "TA");
        cv.setCvId(10L);
        cv.setCvName("Summer CV");
        cv.setUploadedAt(LocalDateTime.of(2026, 3, 1, 12, 0));
        m.addCV(cv);

        assertTrue(m.isCvNameExists("Summer CV"));
        assertFalse(m.isCvNameExists("Other"));
        assertEquals("Summer CV", m.getCVByName("Summer CV").getCvName());
    }

    @Test
    void setDefaultCV_marksExactlyOneDefault() {
        CVManager m = new CVManager(2L, "x@test.uk", "X");

        CVInfo a = new CVInfo(2L, "x@test.uk", "X");
        a.setCvId(1L);
        a.setCvName("A");
        a.setUploadedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        a.setDefault(true);

        CVInfo b = new CVInfo(2L, "x@test.uk", "X");
        b.setCvId(2L);
        b.setCvName("B");
        b.setUploadedAt(LocalDateTime.of(2026, 2, 1, 0, 0));

        m.addCV(a);
        m.addCV(b);

        assertTrue(m.setDefaultCV(2L));

        CVInfo def = m.getDefaultCV();
        assertNotNull(def);
        assertEquals(2L, def.getCvId());
        assertTrue(def.isDefault());
        assertFalse(m.getCVByName("A").isDefault());
    }

    @Test
    void getAllCVsSorted_newestFirst() {
        CVManager m = new CVManager(3L, "y@test.uk", "Y");
        CVInfo oldCv = new CVInfo(3L, "y@test.uk", "Y");
        oldCv.setCvId(1L);
        oldCv.setCvName("old");
        oldCv.setUploadedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        CVInfo newCv = new CVInfo(3L, "y@test.uk", "Y");
        newCv.setCvId(2L);
        newCv.setCvName("new");
        newCv.setUploadedAt(LocalDateTime.of(2026, 6, 1, 0, 0));
        m.addCV(oldCv);
        m.addCV(newCv);

        assertEquals("new", m.getAllCVsSorted().get(0).getCvName());
    }

    @Test
    void getCVNames_listsInOrder() {
        CVManager m = new CVManager(4L, "z@test.uk", "Z");
        CVInfo x = new CVInfo(4L, "z@test.uk", "Z");
        x.setCvId(1L);
        x.setCvName("first");
        x.setUploadedAt(LocalDateTime.now());
        m.addCV(x);

        assertEquals(1, m.getCVCount());
        assertEquals("first", m.getCVNames().get(0));
    }
}
