// ta/ui/components/StatusCellRenderer.java
package ta.ui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    
    private static final Color ACCEPTED_BG = new Color(220, 252, 231);
    private static final Color ACCEPTED_FG = new Color(22, 101, 52);
    private static final Color PENDING_BG = new Color(254, 249, 195);
    private static final Color PENDING_FG = new Color(161, 98, 7);
    private static final Color REJECTED_BG = new Color(254, 226, 226);
    private static final Color REJECTED_FG = new Color(153, 27, 27);
    private static final Color WAITLISTED_BG = new Color(219, 234, 254);
    private static final Color WAITLISTED_FG = new Color(29, 78, 216);
    private static final Color CANCELLED_BG = new Color(243, 244, 246);
    private static final Color CANCELLED_FG = new Color(107, 114, 128);
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        String status = (String) value;
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        if (status == null) {
            setBackground(Color.WHITE);
            setForeground(new Color(107, 114, 128));
        } else if ("accepted".equalsIgnoreCase(status)) {
            setBackground(ACCEPTED_BG);
            setForeground(ACCEPTED_FG);
        } else if ("pending".equalsIgnoreCase(status)) {
            setBackground(PENDING_BG);
            setForeground(PENDING_FG);
        } else if ("rejected".equalsIgnoreCase(status)) {
            setBackground(REJECTED_BG);
            setForeground(REJECTED_FG);
        } else if ("waitlisted".equalsIgnoreCase(status)) {
            setBackground(WAITLISTED_BG);
            setForeground(WAITLISTED_FG);
        } else if ("cancelled".equalsIgnoreCase(status)) {
            setBackground(CANCELLED_BG);
            setForeground(CANCELLED_FG);
        } else {
            setBackground(Color.WHITE);
            setForeground(new Color(107, 114, 128));
        }
        
        return this;
    }
}