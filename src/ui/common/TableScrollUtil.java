package ui.common;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

/**
 * Table scrolling and responsive columns: wide viewports absorb extra width into flexible
 * columns; narrow viewports shrink flexible columns toward their minimums; horizontal scroll
 * appears only when the viewport is narrower than the sum of minimum widths (plus fixed columns).
 */
public final class TableScrollUtil {

    private static final String VIEWPORT_LISTENER_KEY = "TableScrollUtil.viewportResponsive";

    private TableScrollUtil() {
    }

    public static JScrollPane wrapTable(JTable table) {
        return new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Column: {@code flexGrow == 0} means fixed width at {@code preferredWidth} (does not shrink).
     * Otherwise width interpolates between {@code minWidth} and {@code preferredWidth} when space is tight,
     * and grows past {@code preferredWidth} on wide screens in proportion to {@code flexGrow}.
     */
    public static final class ColumnSpec {
        public final int minWidth;
        public final int preferredWidth;
        public final double flexGrow;

        private ColumnSpec(int minWidth, int preferredWidth, double flexGrow) {
            int pref = Math.max(minWidth, preferredWidth);
            this.minWidth = Math.min(minWidth, pref);
            this.preferredWidth = pref;
            this.flexGrow = flexGrow;
        }

        public static ColumnSpec fixed(int width) {
            return new ColumnSpec(width, width, 0);
        }

        public static ColumnSpec flex(int minWidth, int preferredWidth) {
            return new ColumnSpec(minWidth, preferredWidth, 1.0);
        }

        public static ColumnSpec flex(int minWidth, int preferredWidth, double flexGrow) {
            return new ColumnSpec(minWidth, preferredWidth, flexGrow);
        }

        boolean isFixed() {
            return flexGrow <= 0;
        }
    }

    /**
     * Build flex specs from current column preferred widths (e.g. after {@link #autoSizeColumnsFromContent}).
     */
    public static ColumnSpec[] flexSpecsFromCurrentWidths(JTable table, double minFractionOfPref) {
        int n = table.getColumnCount();
        ColumnSpec[] specs = new ColumnSpec[n];
        double f = Math.max(0.2, Math.min(1.0, minFractionOfPref));
        for (int i = 0; i < n; i++) {
            TableColumn c = table.getColumnModel().getColumn(i);
            int pref = Math.max(48, c.getPreferredWidth());
            int min = (int) Math.round(pref * f);
            min = Math.max(48, Math.min(min, pref));
            specs[i] = ColumnSpec.flex(min, pref, 1.0);
        }
        return specs;
    }

    /**
     * Attach viewport listener and lay out columns. Call after table columns are configured.
     */
    public static void installResponsiveColumns(JTable table, JScrollPane scrollPane, ColumnSpec... specs) {
        if (specs.length != table.getColumnCount()) {
            throw new IllegalArgumentException(
                    "ColumnSpec count " + specs.length + " != table columns " + table.getColumnCount());
        }

        Object prev = scrollPane.getClientProperty(VIEWPORT_LISTENER_KEY);
        if (prev instanceof ComponentListener) {
            scrollPane.getViewport().removeComponentListener((ComponentListener) prev);
        }

        Runnable relayout = () -> relayoutResponsiveColumns(table, scrollPane, specs);

        ComponentListener listener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout.run();
            }
        };
        scrollPane.putClientProperty(VIEWPORT_LISTENER_KEY, listener);
        scrollPane.getViewport().addComponentListener(listener);

        SwingUtilities.invokeLater(relayout);
    }

    /**
     * Fixed column widths (legacy); no wide-stretch behaviour.
     */
    public static void applyPreferredColumnWidths(JTable table, int... widths) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            int w = widths[i];
            col.setMinWidth(48);
            col.setPreferredWidth(w);
            col.setWidth(w);
        }
    }

    /**
     * After loading rows, size columns from header + cell text (capped) for export-style tables.
     */
    public static void autoSizeColumnsFromContent(JTable table, int horizontalMargin, int maxColumnWidth) {
        if (table.getColumnCount() == 0) {
            return;
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        var fmHeader = table.getTableHeader().getFontMetrics(table.getTableHeader().getFont());
        var fmCell = table.getFontMetrics(table.getFont());
        for (int c = 0; c < table.getColumnCount(); c++) {
            String header = table.getColumnName(c);
            int w = fmHeader.stringWidth(header) + 2 * horizontalMargin;
            for (int r = 0; r < table.getRowCount(); r++) {
                Object v = table.getValueAt(r, c);
                String s = v == null ? "" : String.valueOf(v);
                w = Math.max(w, fmCell.stringWidth(s) + 2 * horizontalMargin);
            }
            w = Math.min(maxColumnWidth, Math.max(64, w));
            TableColumn col = table.getColumnModel().getColumn(c);
            col.setMinWidth(48);
            col.setPreferredWidth(w);
            col.setWidth(w);
        }
    }

    private static void relayoutResponsiveColumns(JTable table, JScrollPane scrollPane, ColumnSpec[] specs) {
        int viewportW = scrollPane.getViewport().getWidth();
        if (viewportW <= 0) {
            return;
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int n = specs.length;
        double sumFix = 0;
        double fMin = 0;
        double fPref = 0;
        double sumWeight = 0;
        for (ColumnSpec s : specs) {
            if (s.isFixed()) {
                sumFix += s.preferredWidth;
            } else {
                fMin += s.minWidth;
                fPref += s.preferredWidth;
                sumWeight += s.flexGrow;
            }
        }
        boolean hasFlex = sumWeight > 1e-6;

        double sumMinTotal = sumFix + fMin;
        double sumPrefTotal = sumFix + fPref;

        int[] widths = new int[n];

        if (viewportW + 0.5 >= sumPrefTotal) {
            double extra = viewportW - sumPrefTotal;
            for (int i = 0; i < n; i++) {
                ColumnSpec s = specs[i];
                if (s.isFixed()) {
                    widths[i] = s.preferredWidth;
                } else {
                    widths[i] = (int) Math.round(
                            s.preferredWidth + (hasFlex ? extra * (s.flexGrow / sumWeight) : 0));
                }
            }
            if (hasFlex) {
                fixWidthDriftPreferFlex(widths, specs, (int) Math.round(viewportW));
            }
        } else if (viewportW + 0.5 >= sumMinTotal) {
            double denom = fPref - fMin;
            double spaceForFlex = viewportW - sumFix;
            double ratio = denom > 1e-3 ? (spaceForFlex - fMin) / denom : 0;
            ratio = Math.max(0, Math.min(1, ratio));
            for (int i = 0; i < n; i++) {
                ColumnSpec s = specs[i];
                if (s.isFixed()) {
                    widths[i] = s.preferredWidth;
                } else {
                    widths[i] = (int) Math.round(s.minWidth + ratio * (s.preferredWidth - s.minWidth));
                }
            }
            if (hasFlex) {
                fixWidthDriftPreferFlex(widths, specs, (int) Math.round(viewportW));
            }
        } else {
            for (int i = 0; i < n; i++) {
                ColumnSpec s = specs[i];
                widths[i] = s.isFixed() ? s.preferredWidth : s.minWidth;
            }
        }

        for (int i = 0; i < n; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            ColumnSpec s = specs[i];
            int w = Math.max(s.isFixed() ? s.preferredWidth : s.minWidth, widths[i]);
            col.setMinWidth(s.minWidth);
            col.setPreferredWidth(w);
            col.setWidth(w);
        }
    }

    /** Fix rounding so total width matches viewport; prefer the widest flexible column. */
    private static void fixWidthDriftPreferFlex(int[] widths, ColumnSpec[] specs, int targetTotal) {
        int sum = Arrays.stream(widths).sum();
        int drift = targetTotal - sum;
        if (drift == 0) {
            return;
        }
        int idx = -1;
        int maxW = -1;
        for (int i = 0; i < widths.length; i++) {
            if (!specs[i].isFixed() && widths[i] >= maxW) {
                maxW = widths[i];
                idx = i;
            }
        }
        if (idx < 0) {
            for (int i = 0; i < widths.length; i++) {
                if (widths[i] >= maxW) {
                    maxW = widths[i];
                    idx = i;
                }
            }
        }
        if (idx >= 0) {
            int floor = specs[idx].isFixed() ? specs[idx].preferredWidth : specs[idx].minWidth;
            widths[idx] = Math.max(floor, widths[idx] + drift);
        }
    }
}
