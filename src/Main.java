import auth.LoginFrame;
import common.dao.UserFileDAO;
import common.entity.Admin;
import common.entity.User;

import javax.swing.*;
import java.util.List;

/**
 * 程序入口
 * TA招聘系统启动类
 *
 * <p>启动流程：</p>
 * <ol>
 *   <li>设置 Swing 外观为系统默认样式。</li>
 *   <li>执行系统初始化：若数据库中不存在任何管理员账号，
 *       则自动创建默认 Admin 账号，保证系统首次运行可用。</li>
 *   <li>在 EDT（事件调度线程）中启动登录窗口，确保 Swing 线程安全。</li>
 * </ol>
 *
 * @author Can Chen
 * @author Zhixuan Guo
 * @version 2.0
 */
public class Main {

    /** 系统默认管理员邮箱（仅首次启动时创建） */
    private static final String DEFAULT_ADMIN_EMAIL = "admin@system.local";

    /** 系统默认管理员密码（建议首次登录后立即修改） */
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@2024";

    /**
     * 主函数 - 程序启动入口
     *
     * @param args 命令行参数（暂未使用）
     * @author Zhixuan Guo
     */
    public static void main(String[] args) {

        // 1. 配置 Swing 外观
        configureSwingLookAndFeel();

        // 2. 系统初始化（数据层 bootstrap）
        try {
            initializeSystem();
        } catch (Exception e) {
            System.err.println("[WARN] 系统初始化失败，将跳过 bootstrap：" + e.getMessage());
        }

        // 3. 在 EDT 中启动 GUI
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    /**
     * 配置 Swing 外观为系统原生样式。
     * 若设置失败则静默回退到默认外观，不阻断启动。
     *
     * @author Zhixuan Guo
     */
    private static void configureSwingLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("[WARN] 无法设置系统外观，使用默认外观：" + e.getMessage());
        }
    }

    /**
     * 系统初始化逻辑。
     *
     * <p>检查用户数据库中是否已存在 Admin 账号；
     * 若不存在（首次运行），则创建默认管理员账号并在控制台打印提示。
     * 该方法确保系统始终有一个可用的管理员入口。</p>
     *
     * @author Zhixuan Guo
     */
    private static void initializeSystem() {
        UserFileDAO dao = new UserFileDAO();
        List<User> allUsers = dao.findAll();

        boolean adminExists = allUsers.stream()
                .anyMatch(u -> u instanceof Admin);

        if (!adminExists) {
            Admin defaultAdmin = new Admin(DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
            defaultAdmin.setUserId(1L);
            defaultAdmin.setName("System Administrator");
            dao.save(defaultAdmin);

            System.out.println("==============================================");
            System.out.println("  [BOOTSTRAP] 已创建默认管理员账号");
            System.out.println("  邮箱   : " + DEFAULT_ADMIN_EMAIL);
            System.out.println("  密码   : " + DEFAULT_ADMIN_PASSWORD);
            System.out.println("  请登录后立即前往设置修改默认密码！");
            System.out.println("==============================================");
        } else {
            System.out.println("[INFO] 系统初始化完成，用户数：" + allUsers.size());
        }
    }
}
