// ta/ui/components/ActionButtonRenderer.java
package ui.ta.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ui.common.TableListActionStyle;

public class ActionButtonRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        String action = value == null ? "" : String.valueOf(value);
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(Color.WHITE);
            setForeground(TableListActionStyle.colorForActionLabel(action));
        }
        if (TableListActionStyle.isDisabledActionText(action)) {
            setFont(new Font("SansSerif", Font.PLAIN, 12));
        } else {
            setFont(new Font("SansSerif", Font.BOLD, 12));
        }
        return this;
    }
}