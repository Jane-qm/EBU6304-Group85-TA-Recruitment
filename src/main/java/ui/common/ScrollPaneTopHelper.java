package ui.common;

import java.awt.Point;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * JOptionPane 内嵌 {@link JScrollPane} 打开时常常会因布局/焦点滚到中部或底部；
 * 在挂载到窗口后把视口拉回顶部，保证“从头开始”阅读。
 */
public final class ScrollPaneTopHelper {

    private ScrollPaneTopHelper() {
    }

    public static void installScrollStartsAtTop(JScrollPane scroll) {
        if (scroll == null) {
            return;
        }
        Runnable toTop = () -> {
            scroll.getViewport().setViewPosition(new Point(0, 0));
            scroll.getVerticalScrollBar().setValue(0);
            scroll.getHorizontalScrollBar().setValue(0);
        };
        scroll.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                SwingUtilities.invokeLater(toTop);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }
}
