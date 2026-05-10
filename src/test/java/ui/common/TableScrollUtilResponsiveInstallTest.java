package ui.common;

import org.junit.jupiter.api.Test;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal Swing regression: responsive installer must reject mismatched column counts.
 */
class TableScrollUtilResponsiveInstallTest {

    @Test
    void installResponsiveColumns_specCountMismatch_throws() {
        JTable table = new JTable(2, 3);
        JScrollPane scroll = new JScrollPane(table);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> TableScrollUtil.installResponsiveColumns(table, scroll,
                        TableScrollUtil.ColumnSpec.fixed(40)));

        assertTrue(ex.getMessage().contains("ColumnSpec count"));
    }
}
