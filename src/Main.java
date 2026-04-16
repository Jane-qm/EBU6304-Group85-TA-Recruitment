import auth.LoginFrame;
import common.dao.JsonPersistenceManager;
import common.entity.User;
import common.entity.UserRole;
import common.service.MOJobService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mo.ui.MODashboardFrame; // 导入新写的 MO 首页

/**
 * 程序入口类
 */
public class Main {
    private static final JsonPersistenceManager JSON_PERSISTENCE_MANAGER = new JsonPersistenceManager();

    public static void main(String[] args) {
        initializeJsonStorage();
        setupSystemLookAndFeel();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    /**
     * Iteration 2: ensure JSON stores under {@code data/} exist (empty arrays and {@code data/cvs/}).
     * Filename constants live in {@link JsonPersistenceManager}.
     *
     * MO-009.2: also runs the auto-close check so that expired jobs are closed
     * before any user session begins.
     */
    private static void initializeJsonStorage() {
        JSON_PERSISTENCE_MANAGER.initializeBaseFiles();
        // ADM-001: guarantee default super-admin exists before any login attempt
        new common.service.UserService().ensureDefaultAdmin();
        new MOJobService().autoCloseExpiredJobs();
    }

    /**
     * Basic role-based routing placeholder after login.
     */
    public static String resolveHomeRoute(User user) {
        if (user == null || user.getRole() == null) {
            return "UnknownHome";
        }
        if (user.getRole() == UserRole.ADMIN) {
            return "AdminHome";
        }
        if (user.getRole() == UserRole.MO) {
            return "MOHome";
        }
        return "TAHome";
    }

    private static void setupSystemLookAndFeel() {
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
