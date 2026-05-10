package modules.config;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SystemConfigTest {

    @Test
    void notConfigured_whenEitherBoundaryNull() {
        SystemConfig c = new SystemConfig();
        assertFalse(c.isConfigured());
        c.setApplicationStart(LocalDateTime.now());
        assertFalse(c.isConfigured());
    }

    @Test
    void configured_whenBothBoundariesSet() {
        SystemConfig c = new SystemConfig();
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 31, 23, 59);
        c.setApplicationStart(start);
        c.setApplicationEnd(end);
        assertTrue(c.isConfigured());
        assertTrue(c.isValidRange());
    }

    @Test
    void invalidRange_whenEndBeforeStart() {
        SystemConfig c = new SystemConfig();
        c.setApplicationStart(LocalDateTime.of(2026, 8, 1, 0, 0));
        c.setApplicationEnd(LocalDateTime.of(2026, 6, 1, 0, 0));
        assertTrue(c.isConfigured());
        assertFalse(c.isValidRange());
    }
}
