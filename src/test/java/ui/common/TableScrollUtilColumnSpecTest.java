package ui.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link TableScrollUtil.ColumnSpec} factory helpers and width normalization (no Swing required).
 */
class TableScrollUtilColumnSpecTest {

    @Test
    void fixed_setsZeroFlex_and_equalWidths() {
        TableScrollUtil.ColumnSpec s = TableScrollUtil.ColumnSpec.fixed(120);
        assertTrue(s.isFixed());
        assertEquals(120, s.minWidth);
        assertEquals(120, s.preferredWidth);
        assertEquals(0.0, s.flexGrow, 1e-9);
    }

    @Test
    void flex_whenMinExceedsPreferred_collapsesToSingleWidth() {
        TableScrollUtil.ColumnSpec s = TableScrollUtil.ColumnSpec.flex(200, 100);
        assertFalse(s.isFixed());
        assertEquals(200, s.minWidth);
        assertEquals(200, s.preferredWidth);
    }

    @Test
    void flex_typicalMinLessThanPreferred_preserved() {
        TableScrollUtil.ColumnSpec s = TableScrollUtil.ColumnSpec.flex(60, 180);
        assertEquals(60, s.minWidth);
        assertEquals(180, s.preferredWidth);
    }

    @Test
    void flex_withWeight_preservesFlexGrow() {
        TableScrollUtil.ColumnSpec s = TableScrollUtil.ColumnSpec.flex(60, 120, 2.5);
        assertFalse(s.isFixed());
        assertEquals(2.5, s.flexGrow, 1e-9);
    }
}
