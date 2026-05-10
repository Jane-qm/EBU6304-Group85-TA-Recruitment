package modules.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Null-handling branches on {@link SystemConfigService} date predicates (no config mutation).
 */
class SystemConfigServiceNullPredicateTest {

    private final SystemConfigService service = new SystemConfigService();

    @Test
    void isNowWithinRecruitmentWindow_null_returnsFalse() {
        assertFalse(service.isNowWithinRecruitmentWindow(null));
    }

    @Test
    void isWithinApplicationCycle_null_returnsFalse() {
        assertFalse(service.isWithinApplicationCycle(null));
    }

    @Test
    void isDateWithinApplicationCycle_null_returnsFalse() {
        assertFalse(service.isDateWithinApplicationCycle(null));
    }
}
