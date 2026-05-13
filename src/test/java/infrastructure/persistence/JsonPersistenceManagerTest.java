package infrastructure.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import infrastructure.security.PasswordService;
import modules.application.Application;
import modules.config.SystemConfig;
import modules.notification.NotificationMessage;
import modules.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JSON persistence initialization and read/write helpers.
 *
 * @version 1.1
 * @contributor Jiaze Wang
 * @update
 * - Added coverage for users.template.json based initialization
 * - Added coverage for the dual seeded admin demo password template
 */
class JsonPersistenceManagerTest {

    @Test
    void initializeBaseFiles_createsDirectoriesAndEmptyStores(@TempDir Path root) throws Exception {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        assertTrue(Files.isDirectory(root.resolve("cvs")));
        assertEquals("[]", Files.readString(root.resolve(JsonPersistenceManager.USERS_FILE), StandardCharsets.UTF_8).trim());
        assertEquals("{}", Files.readString(root.resolve(JsonPersistenceManager.SYSTEM_CONFIG_FILE), StandardCharsets.UTF_8).trim());
    }

    @Test
    void initializeBaseFiles_copiesUsersTemplateWhenUsersJsonMissing(@TempDir Path root) throws Exception {
        String templateJson = """
                [
                  {
                    "userId": 100001,
                    "email": "admin@qmul.ac.uk",
                    "passwordHash": "PBKDF2$120000$XbpaB3BP8C49xmjePKapaQ==$H6Pwx1l9PxDTxKzDBwM5m6fQdqr4cn7qvEPXIWZfM0Y=",
                    "role": "ADMIN",
                    "status": "ACTIVE",
                    "mustChangePassword": false,
                    "failedLoginCount": 0,
                    "lockedUntil": null
                  },
                  {
                    "userId": 100002,
                    "email": "admin@bupt.edu.cn",
                    "passwordHash": "PBKDF2$120000$m+wzGDRp3buntMPhI/WkEA==$kn8xof6uEm7yVRUvv6+oXQ4C+PTccObtIW6UEzsXCQE=",
                    "role": "ADMIN",
                    "status": "ACTIVE",
                    "mustChangePassword": false,
                    "failedLoginCount": 0,
                    "lockedUntil": null
                  }
                ]
                """;
        Files.writeString(root.resolve(JsonPersistenceManager.USERS_TEMPLATE_FILE), templateJson, StandardCharsets.UTF_8);

        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        assertEquals(templateJson.trim(),
                Files.readString(root.resolve(JsonPersistenceManager.USERS_FILE), StandardCharsets.UTF_8).trim());
    }

    @Test
    void initializeBaseFiles_doesNotOverwriteExistingUsersJson(@TempDir Path root) throws Exception {
        String existingUsersJson = "[{\"email\":\"existing.ta@qmul.ac.uk\",\"role\":\"TA\"}]";
        String templateJson = "[{\"email\":\"admin@qmul.ac.uk\",\"role\":\"ADMIN\"}]";
        Files.writeString(root.resolve(JsonPersistenceManager.USERS_FILE), existingUsersJson, StandardCharsets.UTF_8);
        Files.writeString(root.resolve(JsonPersistenceManager.USERS_TEMPLATE_FILE), templateJson, StandardCharsets.UTF_8);

        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        assertEquals(existingUsersJson,
                Files.readString(root.resolve(JsonPersistenceManager.USERS_FILE), StandardCharsets.UTF_8));
    }

    @Test
    void committedUsersTemplate_containsDualSeededAdminsWithDemoPasswordPolicy() throws Exception {
        Path templatePath = Path.of("data", JsonPersistenceManager.USERS_TEMPLATE_FILE);
        String json = Files.readString(templatePath, StandardCharsets.UTF_8);
        JsonArray users = JsonParser.parseString(json).getAsJsonArray();

        JsonObject qmulAdmin = findUserByEmail(users, "admin@qmul.ac.uk");
        JsonObject buptAdmin = findUserByEmail(users, "admin@bupt.edu.cn");

        assertSeededAdminTemplate(qmulAdmin);
        assertSeededAdminTemplate(buptAdmin);
    }

    private JsonObject findUserByEmail(JsonArray users, String email) {
        for (int i = 0; i < users.size(); i++) {
            JsonObject user = users.get(i).getAsJsonObject();
            if (email.equals(user.get("email").getAsString())) {
                return user;
            }
        }
        fail("Missing seeded admin in users.template.json: " + email);
        return null;
    }

    private void assertSeededAdminTemplate(JsonObject user) {
        assertEquals("ADMIN", user.get("role").getAsString());
        assertEquals("ACTIVE", user.get("status").getAsString());
        assertFalse(user.get("mustChangePassword").getAsBoolean());
        assertEquals(0, user.get("failedLoginCount").getAsInt());
        assertTrue(user.get("lockedUntil").isJsonNull());
        assertTrue(PasswordService.verify("111111", user.get("passwordHash").getAsString()));
    }

    @Test
    void writeList_readList_roundTrip_applications(@TempDir Path root) {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        Application a = new Application();
        a.setApplicationId(10L);
        a.setTaUserId(1L);
        a.setJobId(2L);
        a.setStatus("PENDING_REVIEW");
        a.setAppliedAt(LocalDateTime.of(2026, 5, 10, 12, 0));

        pm.writeList(JsonPersistenceManager.TA_APPLICATIONS_FILE, List.of(a));

        List<Application> back = pm.readList(JsonPersistenceManager.TA_APPLICATIONS_FILE, Application.class);
        assertEquals(1, back.size());
        assertEquals(10L, back.get(0).getApplicationId());
        assertEquals("PENDING_REVIEW", back.get(0).getStatus());
    }

    @Test
    void writeObject_readObject_roundTrip_systemConfig(@TempDir Path root) {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        SystemConfig cfg = new SystemConfig();
        cfg.setApplicationStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        cfg.setApplicationEnd(LocalDateTime.of(2026, 12, 31, 23, 59));
        cfg.setUpdatedBy("admin@test");

        pm.writeObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, cfg);

        SystemConfig loaded = pm.readObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, SystemConfig.class);
        assertTrue(loaded.isConfigured());
        assertTrue(loaded.isValidRange());
        assertEquals("admin@test", loaded.getUpdatedBy());
    }

    @Test
    void loadMapFromFile_buildsMapFromList(@TempDir Path root) {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        Application a1 = new Application();
        a1.setApplicationId(1L);
        Application a2 = new Application();
        a2.setApplicationId(2L);
        pm.writeList(JsonPersistenceManager.TA_APPLICATIONS_FILE, List.of(a1, a2));

        Map<Long, Application> map = pm.loadMapFromFile(
                JsonPersistenceManager.TA_APPLICATIONS_FILE,
                Application.class,
                Application::getApplicationId);

        assertEquals(2, map.size());
        assertEquals(1L, map.get(1L).getApplicationId());
        assertEquals(2L, map.get(2L).getApplicationId());
    }

    @Test
    void syncMapToFile_nullWritesEmptyArray(@TempDir Path root) throws Exception {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        pm.syncMapToFile(JsonPersistenceManager.TA_APPLICATIONS_FILE, null);

        assertEquals("[]", Files.readString(root.resolve(JsonPersistenceManager.TA_APPLICATIONS_FILE), StandardCharsets.UTF_8).trim());
    }

    @Test
    void syncMapToFile_preservesValuesOrder(@TempDir Path root) {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        Application a1 = new Application();
        a1.setApplicationId(1L);
        Application a2 = new Application();
        a2.setApplicationId(2L);
        Map<Long, Application> map = new LinkedHashMap<>();
        map.put(2L, a2);
        map.put(1L, a1);

        pm.syncMapToFile(JsonPersistenceManager.TA_APPLICATIONS_FILE, map);

        List<Application> list = pm.readList(JsonPersistenceManager.TA_APPLICATIONS_FILE, Application.class);
        assertEquals(2, list.size());
        assertEquals(2L, list.get(0).getApplicationId());
        assertEquals(1L, list.get(1).getApplicationId());
    }

    @Test
    void readList_invalidJson_throws(@TempDir Path root) throws Exception {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        Path path = root.resolve(JsonPersistenceManager.USERS_FILE);
        Files.writeString(path, "{not-json", StandardCharsets.UTF_8);

        assertThrows(IllegalStateException.class,
                () -> pm.readList(JsonPersistenceManager.USERS_FILE, Application.class));
    }

    @Test
    void writeJsonArray_readJsonArray_roundTrip(@TempDir Path root) {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        JsonArray arr = new JsonArray();
        arr.add("x");
        arr.add(42);

        pm.writeJsonArray(JsonPersistenceManager.NOTIFICATIONS_FILE, arr);

        JsonArray read = pm.readJsonArray(JsonPersistenceManager.NOTIFICATIONS_FILE);
        assertEquals(2, read.size());
        assertEquals("x", read.get(0).getAsString());
        assertEquals(42, read.get(1).getAsInt());
    }

    @Test
    void notificationMessage_roundTrip_viaWriteList(@TempDir Path root) {
        JsonPersistenceManager pm = new JsonPersistenceManager(root.toString());
        pm.initializeBaseFiles();

        NotificationMessage n = new NotificationMessage();
        n.setNotificationId(99L);
        n.setRecipientUserId(7L);
        n.setRecipientRole(UserRole.TA);
        n.setTitle("t");
        n.setContent("c");
        n.setType("TYPE");
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.of(2026, 5, 10, 15, 30));

        pm.writeList(JsonPersistenceManager.NOTIFICATIONS_FILE, List.of(n));

        List<NotificationMessage> list = pm.readList(JsonPersistenceManager.NOTIFICATIONS_FILE, NotificationMessage.class);
        assertEquals(1, list.size());
        assertEquals(99L, list.get(0).getNotificationId());
        assertEquals(UserRole.TA, list.get(0).getRecipientRole());
    }
}
