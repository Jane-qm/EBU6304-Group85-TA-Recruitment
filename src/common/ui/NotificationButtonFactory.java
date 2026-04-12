package common.ui;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class NotificationButtonFactory {
    private NotificationButtonFactory() {
    }

    public static JButton createButton(int unreadCount) {
        JButton button = new JButton(unreadCount > 0 ? String.valueOf(unreadCount) : "");
        button.setIcon(new BellIcon(16, new Color(55, 65, 81)));
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalTextPosition(JButton.RIGHT);
        button.setIconTextGap(6);
        return button;
    }

    private static final class BellIcon implements Icon {
        private final int size;
        private final Color color;

        private BellIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int w = size;
            int h = size;
            g2.drawArc(x + 3, y + 3, w - 6, h - 6, 25, 130);
            g2.drawLine(x + 4, y + h - 5, x + w - 4, y + h - 5);
            g2.drawLine(x + 4, y + h - 5, x + 6, y + 5);
            g2.drawLine(x + w - 4, y + h - 5, x + w - 6, y + 5);
            g2.drawLine(x + w / 2, y + 2, x + w / 2, y + 4);
            g2.fillOval(x + w / 2 - 2, y + h - 4, 4, 4);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
