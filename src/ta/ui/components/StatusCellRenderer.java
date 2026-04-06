package ta.ui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        String status = (String) value;
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        if ("accepted".equalsIgnoreCase(status)) {
            setBackground(new Color(220, 252, 231));
            setForeground(new Color(22, 101, 52));
        } else if ("pending".equalsIgnoreCase(status)) {
            setBackground(new Color(254, 249, 195));
            setForeground(new Color(161, 98, 7));
        } else if ("rejected".equalsIgnoreCase(status)) {
            setBackground(new Color(254, 226, 226));
            setForeground(new Color(153, 27, 27));
        } else {
            setBackground(Color.WHITE);
            setForeground(new Color(107, 114, 128));
        }
        return this;
    }
}