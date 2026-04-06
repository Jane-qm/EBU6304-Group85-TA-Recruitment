// ta/ui/components/ActionButtonRenderer.java
package ta.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ActionButtonRenderer extends DefaultTableCellRenderer {
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color CANCEL_RED = new Color(220, 38, 38);
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        String action = (String) value;
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        if ("Apply".equals(action) || "Accept Offer".equals(action)) {
            setForeground(PRIMARY_BLUE);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setBackground(Color.WHITE);
        } else if ("Cancel".equals(action)) {
            setForeground(CANCEL_RED);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setBackground(Color.WHITE);
        } else {
            setForeground(new Color(156, 163, 175));
            setFont(new Font("SansSerif", Font.PLAIN, 12));
            setBackground(Color.WHITE);
        }
        return this;
    }
}