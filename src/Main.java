package main;

import auth.RegisterFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 程序入口类
 */
public class Main {
    public static void main(String[] args) {
        // 1. 设置外观风格（Look and Feel）
        setupSystemLookAndFeel();

        // 2. 在事件分发线程中启动 UI
        SwingUtilities.invokeLater(() -> {
            RegisterFrame registerFrame = new RegisterFrame();
            registerFrame.setVisible(true);
        });
    }

    private static void setupSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}