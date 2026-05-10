package ui.common;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Applies a modest global font size increase for Swing (after the look-and-feel is installed).
 */
public final class AppUiFonts {

    /** Extra points added to every LAF {@link FontUIResource}. */
    public static final int FONT_SIZE_BOOST = 3;

    private AppUiFonts() {
    }

    public static void applyGlobalFontBoost() {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        Enumeration<Object> keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value instanceof FontUIResource) {
                FontUIResource f = (FontUIResource) value;
                Font larger = f.deriveFont(f.getSize2D() + FONT_SIZE_BOOST);
                UIManager.put(key, new FontUIResource(larger));
            }
        }
    }
}
