package common.service;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import common.entity.UserRole;
import common.util.GsonUtils;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * SYS-001: Role-Based Access Control (RBAC) service.
 *
 * <p>The permission matrix is loaded lazily from {@code data/permissions.json} on
 * first use.  If the file is absent or cannot be parsed, the class falls back to
 * the built-in three-tier defaults so the application is never left in a broken
 * state:
 * <ul>
 *   <li>ADMIN — may enter ADMIN, MO, and TA portals
 *   <li>MO    — may enter MO portal only
 *   <li>TA    — may enter TA portal only
 * </ul>
 *
 * <p>To change permissions at runtime, edit {@code data/permissions.json} and call
 * {@link #reload()} (or restart the application).
 *
 * @author Yiping Zheng (original)
 * @version 2.0 – file-backed permission matrix (SYS-001)
 */
public class PermissionService {

    private static final Path PERMISSIONS_FILE = Path.of("data", "permissions.json");

    /** role-name → set of accessible portal area names (e.g. "TA", "MO", "ADMIN") */
    private static volatile Map<String, Set<String>> roleAccessMatrix = null;

    // -----------------------------------------------------------------------
    // Matrix loading
    // -----------------------------------------------------------------------

    /** Loads the permission matrix from file (once), falling back to defaults. */
    private static synchronized void ensureLoaded() {
        if (roleAccessMatrix != null) {
            return;
        }
        try {
            if (Files.exists(PERMISSIONS_FILE)) {
                String json = Files.readString(PERMISSIONS_FILE);
                Gson gson = GsonUtils.getGson();
                Type type = new TypeToken<PermissionsConfig>() {}.getType();
                PermissionsConfig config = gson.fromJson(json, PermissionsConfig.class);
                if (config != null && config.roleAccess != null && !config.roleAccess.isEmpty()) {
                    Map<String, Set<String>> matrix = new HashMap<>();
                    for (Map.Entry<String, List<String>> entry : config.roleAccess.entrySet()) {
                        matrix.put(entry.getKey().toUpperCase(),
                                new HashSet<>(entry.getValue()));
                    }
                    roleAccessMatrix = Collections.unmodifiableMap(matrix);
                    System.out.println("[PermissionService] Loaded RBAC matrix from "
                            + PERMISSIONS_FILE.toAbsolutePath());
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("[PermissionService] Could not load " + PERMISSIONS_FILE
                    + " — using built-in defaults. Reason: " + e.getMessage());
        }
        roleAccessMatrix = buildDefaults();
        System.out.println("[PermissionService] Using built-in default RBAC matrix.");
    }

    private static Map<String, Set<String>> buildDefaults() {
        Map<String, Set<String>> m = new HashMap<>();
        m.put("ADMIN", Set.of("ADMIN", "MO", "TA"));
        m.put("MO",    Set.of("MO"));
        m.put("TA",    Set.of("TA"));
        return Collections.unmodifiableMap(m);
    }

    /**
     * Forces a reload from {@code data/permissions.json}.
     * Call this if the file is edited while the application is running.
     */
    public static synchronized void reload() {
        roleAccessMatrix = null;
        ensureLoaded();
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code userRole} is permitted to enter the portal
     * area designated for {@code targetRole}.
     *
     * <p>Examples:
     * <pre>
     *   hasAccess(ADMIN, TA)    → true   (admin may view TA portal)
     *   hasAccess(MO,    MO)    → true
     *   hasAccess(MO,    ADMIN) → false  (MO cannot enter Admin portal)
     *   hasAccess(TA,    MO)    → false
     * </pre>
     *
     * @param userRole   the role of the currently logged-in user
     * @param targetRole the role whose portal area is being accessed
     * @return {@code true} if access is permitted
     */
    public static boolean hasAccess(UserRole userRole, UserRole targetRole) {
        if (userRole == null || targetRole == null) {
            return false;
        }
        ensureLoaded();
        Set<String> allowed = roleAccessMatrix.get(userRole.name());
        return allowed != null && allowed.contains(targetRole.name());
    }

    // -----------------------------------------------------------------------
    // JSON model
    // -----------------------------------------------------------------------

    private static class PermissionsConfig {
        @SerializedName("roleAccess")
        Map<String, List<String>> roleAccess;
    }
}
