import auth.LoginFrame;
import common.dao.JsonPersistenceManager;
import common.entity.User;
import common.entity.UserRole;

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
     * Initialize six core JSON files with default empty-array content.
     */
    private static void initializeJsonStorage() {
        JSON_PERSISTENCE_MANAGER.initializeBaseFiles();
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
