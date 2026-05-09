package ui.mo;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Cursor;

/**
 * Shared MO portal button look: white background, black text, black border.
 */
public final class MoUiStyles {

    private MoUiStyles() {
    }

    public static void applyTextButton(AbstractButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
