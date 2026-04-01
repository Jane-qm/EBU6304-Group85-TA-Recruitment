package common.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

/**
 * 基础UI窗口类
 * 提供所有UI界面的通用功能：
 * - 窗口大小设置
 * - 居中显示
 * - Swing原生最大化/还原功能
 * - 统一的外观风格
 * 
 * @author System
 * @version 2.0
 */
public abstract class BaseFrame extends JFrame {
    
    // 默认窗口尺寸
    protected static final int DEFAULT_WIDTH = 600;
    protected static final int DEFAULT_HEIGHT = 500;
    
    // 最小窗口尺寸
    protected static final int MIN_WIDTH = 400;
    protected static final int MIN_HEIGHT = 300;
    
    /**
     * 构造函数
     * @param title 窗口标题
     */
    public BaseFrame(String title) {
        super(title);
        setupFrame(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * 构造函数（带尺寸参数）
     * @param title 窗口标题
     * @param width 宽度
     * @param height 高度
     */
    public BaseFrame(String title, int width, int height) {
        super(title);
        setupFrame(width, height);
    }
    
    /**
     * 设置窗口基础属性
     * @param width 宽度
     * @param height 高度
     */
    private void setupFrame(int width, int height) {
        // 设置窗口大小
        setSize(width, height);
        
        // 设置最小尺寸
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        
        // 居中显示
        setLocationRelativeTo(null);
        
        // 允许窗口最大化/调整大小（Swing 原生支持）
        setResizable(true);
        
        // 设置默认关闭操作（由子类决定）
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // 添加窗口状态监听器，用于跟踪最大化状态变化
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onWindowResized();
            }
        });
        
        // 添加窗口状态变化监听器（Swing 原生最大化事件）
        addWindowStateListener(e -> {
            if ((e.getNewState() & Frame.MAXIMIZED_BOTH) != 0) {
                onWindowMaximized();
            } else if ((e.getNewState() & Frame.NORMAL) != 0) {
                onWindowRestored();
            }
        });
    }
    
    /**
     * 窗口大小改变时的回调（可被子类重写）
     */
    protected void onWindowResized() {
        // 子类可重写此方法
    }
    
    /**
     * 窗口最大化时的回调（可被子类重写）
     */
    protected void onWindowMaximized() {
        // 子类可重写此方法
    }
    
    /**
     * 窗口还原时的回调（可被子类重写）
     */
    protected void onWindowRestored() {
        // 子类可重写此方法
    }
    
    /**
     * 最大化窗口（使用 Swing 原生方法）
     */
    public void maximizeWindow() {
        setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
    }
    
    /**
     * 还原窗口（使用 Swing 原生方法）
     */
    public void restoreWindow() {
        setExtendedState(getExtendedState() & ~Frame.MAXIMIZED_BOTH);
    }
    
    /**
     * 切换最大化/还原状态
     */
    public void toggleMaximize() {
        if (isWindowMaximized()) {
            restoreWindow();
        } else {
            maximizeWindow();
        }
    }
    
    /**
     * 检查窗口是否最大化
     */
    public boolean isWindowMaximized() {
        return (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
    }
    
    /**
     * 刷新窗口布局
     */
    public void refreshLayout() {
        revalidate();
        repaint();
    }
    
    /**
     * 显示错误消息
     */
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * 显示警告消息
     */
    protected void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * 显示信息消息
     */
    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示确认对话框
     */
    protected boolean showConfirm(String message, String title) {
        int result = JOptionPane.showConfirmDialog(this, message, title, 
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * 显示输入对话框
     */
    protected String showInput(String message, String title) {
        return JOptionPane.showInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
    }
    
    /**
     * 初始化UI（抽象方法，由子类实现）
     */
    protected abstract void initUI();
}