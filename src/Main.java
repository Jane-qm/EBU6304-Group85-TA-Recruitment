import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import auth.LoginFrame;
import javax.swing.*;

import auth.LoginFrame;

/**
 * 程序入口类
 */
public class Main {
    public static void main(String[] args) {
        // 1. 设置外观风格（Look and Feel）
        setupSystemLookAndFeel();

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

        // 2. 在事件分发线程中启动 UI
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            
            // 可选：启动时最大化窗口（使用 Swing 原生方法）
            // loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            // 或使用 BaseFrame 提供的方法
            // loginFrame.maximizeWindow();
        });
    }

    private static void setupSystemLookAndFeel() {
        try {
            // 启用抗锯齿字体，解决文字渲染发虚问题
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}