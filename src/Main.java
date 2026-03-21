import auth.LoginFrame;
import javax.swing.*;

/**
 * 程序入口
 * TA招聘系统启动类
 * 
 * @author Can Chen
 * @version 1.0
 */
public class Main {
    
    /**
     * 主函数 - 程序启动入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 设置Swing外观为系统默认样式
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 在事件调度线程中启动GUI，确保线程安全
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 创建并显示登录窗口
                new LoginFrame().setVisible(true);
            }
        });
    }
}