// ta/ui/components/StatusCellRenderer.java
package ta.ui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    
    // 状态颜色定义
    private static final Color HIRED_BG = new Color(220, 252, 231);      // 绿色
    private static final Color HIRED_FG = new Color(22, 101, 52);
    
    private static final Color OFFER_BG = new Color(219, 234, 254);      // 蓝色
    private static final Color OFFER_FG = new Color(29, 78, 216);
    
    private static final Color SUBMITTED_BG = new Color(254, 249, 195);  // 黄色
    private static final Color SUBMITTED_FG = new Color(161, 98, 7);
    
    private static final Color WAITLISTED_BG = new Color(243, 232, 255); // 紫色
    private static final Color WAITLISTED_FG = new Color(126, 34, 206);
    
    private static final Color REJECTED_BG = new Color(254, 226, 226);   // 红色
    private static final Color REJECTED_FG = new Color(153, 27, 27);
    
    private static final Color CANCELLED_BG = new Color(243, 244, 246);  // 灰色
    private static final Color CANCELLED_FG = new Color(107, 114, 128);
    
    private static final Color EXPIRED_BG = new Color(243, 244, 246);    // 灰色
    private static final Color EXPIRED_FG = new Color(107, 114, 128);
    
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
        } else if (status.equalsIgnoreCase("Hired") || status.equalsIgnoreCase("hired")) {
            setBackground(HIRED_BG);
            setForeground(HIRED_FG);
        } else if (status.equalsIgnoreCase("Offer Received") || status.equalsIgnoreCase("Offer Sent") ||
                   status.equalsIgnoreCase("OFFER_SENT")) {
            setBackground(OFFER_BG);
            setForeground(OFFER_FG);
        } else if (status.equalsIgnoreCase("Submitted") || status.equalsIgnoreCase("submitted") ||
                   status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("pending")) {
            setBackground(SUBMITTED_BG);
            setForeground(SUBMITTED_FG);
        } else if (status.equalsIgnoreCase("Waitlisted") || status.equalsIgnoreCase("waitlisted")) {
            setBackground(WAITLISTED_BG);
            setForeground(WAITLISTED_FG);
        } else if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("rejected")) {
            setBackground(REJECTED_BG);
            setForeground(REJECTED_FG);
        } else if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("cancelled")) {
            setBackground(CANCELLED_BG);
            setForeground(CANCELLED_FG);
        } else if (status.equalsIgnoreCase("Expired") || status.equalsIgnoreCase("expired")) {
            setBackground(EXPIRED_BG);
            setForeground(EXPIRED_FG);
        } else {
            setBackground(Color.WHITE);
            setForeground(new Color(107, 114, 128));
        }
        
        return this;
    }
}