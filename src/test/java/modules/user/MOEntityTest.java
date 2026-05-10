package modules.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic MO entity behaviour (no persistence).
 */
class MOEntityTest {

    @Test
    void constructor_setsMoRole() {
        MO mo = new MO("mo.entity@test.uk", "Secret123456");
        assertEquals(UserRole.MO, mo.getRole());
        assertTrue(mo.toString().contains("MO{"));
    }

    @Test
    void name_roundTrip() {
        MO mo = new MO("mo.name@test.uk", "Secret123456");
        mo.setName("Alex");
        assertEquals("Alex", mo.getName());
        assertTrue(mo.toString().contains("Alex"));
    }
}
