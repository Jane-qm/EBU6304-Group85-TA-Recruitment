package ui.common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Borderless, bold text styling for action labels inside tables (view / accept / reject, etc.).
 */
public final class TableListActionStyle {

    public static final Color ACTION_BLUE = new Color(29, 78, 216);
    public static final Color ACTION_GREEN = new Color(22, 163, 74);
    public static final Color ACTION_RED = new Color(220, 38, 38);
    public static final Color ACTION_MUTED = new Color(156, 163, 175);

    private static final Font BOLD = new Font("SansSerif", Font.BOLD, 14);
    private static final Font PLAIN_MUTED = new Font("SansSerif", Font.PLAIN, 14);

    private TableListActionStyle() {
    }

    public static boolean isDisabledActionText(String text) {
        if (text == null) {
            return true;
        }
        String t = text.trim();
        return t.isEmpty() || "—".equals(t);
    }

    public static Color colorForActionLabel(String text) {
        if (isDisabledActionText(text)) {
            return ACTION_MUTED;
        }
        String t = text.trim();
        if ("Reject".equals(t) || "Disable".equals(t) || "Close".equals(t) || "Cancel".equals(t)) {
            return ACTION_RED;
        }
        if (t.startsWith("Accept") || "Shortlist".equals(t) || "Apply".equals(t) || "Activate".equals(t)) {
            return ACTION_GREEN;
        }
        return ACTION_BLUE;
    }

    public static void applyToButton(AbstractButton b, String label) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        String t = label == null ? "" : label.trim();
        if (isDisabledActionText(t)) {
            b.setForeground(ACTION_MUTED);
            b.setFont(PLAIN_MUTED);
            b.setCursor(Cursor.getDefaultCursor());
        } else {
            b.setForeground(colorForActionLabel(t));
            b.setFont(BOLD);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    public static void applyToLabel(JLabel l, String text, boolean actionColumn) {
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        if (!actionColumn) {
            return;
        }
        String t = text == null ? "" : text.trim();
        if (isDisabledActionText(t)) {
            l.setForeground(ACTION_MUTED);
            l.setFont(PLAIN_MUTED);
            l.setCursor(Cursor.getDefaultCursor());
        } else {
            l.setForeground(colorForActionLabel(t));
            l.setFont(BOLD);
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}
