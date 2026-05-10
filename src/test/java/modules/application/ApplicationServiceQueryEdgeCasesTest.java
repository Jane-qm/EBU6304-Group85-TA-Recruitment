package modules.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Null-safe query helpers on {@link ApplicationService} backed by the JSON store.
 */
class ApplicationServiceQueryEdgeCasesTest {

    private final ApplicationService service = new ApplicationService();

    @Test
    void listByTaUserId_null_returnsEmpty() {
        List<Application> list = service.listByTaUserId(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void listByJobId_null_returnsEmpty() {
        List<Application> list = service.listByJobId(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void findById_null_returnsNull() {
        assertNull(service.findById(null));
    }

    @Test
    void getActiveApplicationCount_nullTa_returnsZero() {
        assertEquals(0, service.getActiveApplicationCount(null));
    }
}
