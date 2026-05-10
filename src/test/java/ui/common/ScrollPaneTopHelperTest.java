package ui.common;

import org.junit.jupiter.api.Test;

import javax.swing.JScrollPane;

import static org.junit.jupiter.api.Assertions.*;

class ScrollPaneTopHelperTest {

    @Test
    void installScrollStartsAtTop_null_noOp() {
        assertDoesNotThrow(() -> ScrollPaneTopHelper.installScrollStartsAtTop(null));
    }

    @Test
    void installScrollStartsAtTop_acceptsScrollPane() {
        JScrollPane sp = new JScrollPane();
        assertDoesNotThrow(() -> ScrollPaneTopHelper.installScrollStartsAtTop(sp));
    }
}
