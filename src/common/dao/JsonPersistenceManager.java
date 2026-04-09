package common.dao;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import common.util.GsonUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Central JSON persistence manager.
 * Responsible for initializing and reading/writing all JSON files under the data directory.
 *
 * @version 2.0
 * @contributor Jiaze Wang
 * @update
 * - Added support for object-based JSON persistence
 * - Added system_config.json initialization and read/write support
 */
public class JsonPersistenceManager {
    public static final String USERS_FILE = "users.json";
    public static final String MO_JOBS_FILE = "mo_jobs.json";
    public static final String TA_APPLICATIONS_FILE = "ta_applications.json";
    public static final String MO_OFFERS_FILE = "mo_offers.json";

    public static final String NOTIFICATIONS_FILE = "notifications.json";
    /** CV 元数据列表（与 {@code ta.dao.CVDao} 使用的路径一致，相对 {@code data/}） */
    public static final String TA_CVS_FILE = "ta_cvs.json";


    // 注意：以下文件已移除，使用独立模块的 DAO：
    // - TA_PROFILES_FILE: 使用 ta.dao.TAProfileDAO (文件: data/ta_profiles.json)
    // - CV_INFOS_FILE: 使用 ta.dao.CVDao (文件: data/ta_cvs.json)


    private static final List<String> ARRAY_FILES = Arrays.asList(
            USERS_FILE,
            MO_JOBS_FILE,
            TA_APPLICATIONS_FILE,

            MO_OFFERS_FILE,
            NOTIFICATIONS_FILE,
            TA_CVS_FILE
    );

    private final Path dataDirectory;

    public JsonPersistenceManager() {
        this("data");
    }

    public JsonPersistenceManager(String dataDirectory) {
        this.dataDirectory = Path.of(dataDirectory);
    }

    public void initializeBaseFiles() {
        try {
            Files.createDirectories(dataDirectory);
            Path cvsDir = dataDirectory.resolve("cvs");
            Files.createDirectories(cvsDir);
            for (String file : ALL_FILES) {
                Path path = dataDirectory.resolve(file);
                if (!Files.exists(path)) {
                    Files.writeString(path, "[]", StandardCharsets.UTF_8);
                }
            }

            Path configPath = dataDirectory.resolve(SYSTEM_CONFIG_FILE);
            if (!Files.exists(configPath)) {
                Files.writeString(configPath, "{}", StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize JSON data files.", e);
        }
    }

    public <T> List<T> readList(String fileName, Type type) {
        String json = readRaw(fileName);
        if (!GsonUtils.isValidJson(json)) {
            throw new IllegalStateException("Invalid JSON format in file: " + fileName);
        }
        List<T> result = GsonUtils.fromJson(json, type);
        return result == null ? List.of() : result;
    }

    public <T> List<T> readList(String fileName, Class<T> clazz) {
        Type listType = TypeToken.getParameterized(List.class, clazz).getType();
        return readList(fileName, listType);
    }

    public void writeList(String fileName, List<?> values) {
        writeRaw(fileName, GsonUtils.toJson(values == null ? List.of() : values));
    }

    public <T> T readObject(String fileName, Class<T> clazz) {
        String json = readRaw(fileName);
        if (!GsonUtils.isValidJson(json)) {
            throw new IllegalStateException("Invalid JSON format in file: " + fileName);
        }
        return GsonUtils.fromJson(json, clazz);
    }

    public void writeObject(String fileName, Object value) {
        writeRaw(fileName, GsonUtils.toJson(value == null ? Map.of() : value));
    }

    public <K, V> Map<K, V> loadMapFromFile(String fileName, Class<V> valueClass, Function<V, K> keyExtractor) {
        List<V> values = readList(fileName, valueClass);
        Map<K, V> result = new LinkedHashMap<>();
        for (V value : values) {
            result.put(keyExtractor.apply(value), value);
        }
        return result;
    }

    public <K, V> void syncMapToFile(String fileName, Map<K, V> mapValues) {
        if (mapValues == null) {
            writeList(fileName, List.of());
            return;
        }
        writeList(fileName, mapValues.values().stream().toList());
    }

    public JsonArray readJsonArray(String fileName) {
        String json = readRaw(fileName);
        if (!GsonUtils.isValidJson(json)) {
            throw new IllegalStateException("Invalid JSON format in file: " + fileName);
        }
        JsonArray array = GsonUtils.fromJson(json, JsonArray.class);
        return array == null ? new JsonArray() : array;
    }

    public void writeJsonArray(String fileName, JsonArray array) {
        writeRaw(fileName, GsonUtils.toJson(array == null ? new JsonArray() : array));
    }

    private String readRaw(String fileName) {
        Path path = dataDirectory.resolve(fileName);
        try {
            if (!Files.exists(path)) {
                if (SYSTEM_CONFIG_FILE.equals(fileName)) {
                    Files.writeString(path, "{}", StandardCharsets.UTF_8);
                } else {
                    Files.writeString(path, "[]", StandardCharsets.UTF_8);
                }
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file: " + path, e);
        }
    }

    private void writeRaw(String fileName, String content) {
        Path path = dataDirectory.resolve(fileName);
        try {
            Files.createDirectories(dataDirectory);
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write file: " + path, e);
        }
    }
}