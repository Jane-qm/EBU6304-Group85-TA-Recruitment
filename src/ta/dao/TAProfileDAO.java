package ta.dao;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.dao.LocalDateTimeAdapter;
import ta.entity.TAProfile;

/**
 * TA 个人信息数据访问对象
 * 负责 TA 个人信息的 JSON 文件读写操作
 * 
 * @author Can Chen
 * @version 1.0
 *
 * @version 1.1
 * @contributor Jiaze Wang
 * @update
 * - Reconciled TA profile save behavior when taId/email mappings change
 * - Prevented stale email index entries from pointing to old profile data
 */
public class TAProfileDAO {
    
    private static final String DATA_FILE = "data/ta_profiles.json";
    private final Gson gson;
    private final Map<Long, TAProfile> profilesByTaId;
    private final Map<String, TAProfile> profilesByEmail;
    
    public TAProfileDAO() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.profilesByTaId = new ConcurrentHashMap<>();
        this.profilesByEmail = new ConcurrentHashMap<>();
        ensureDataDirectoryExists();
        loadFromFile();
    }
    
    /**
     * 确保 data 目录存在
     */
    private void ensureDataDirectoryExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
    }
    
    /**
     * 从文件加载所有 TA 个人信息
     */
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<TAProfile>>(){}.getType();
            List<TAProfile> list;
            try {
                list = gson.fromJson(reader, listType);
            } catch (Exception ex) {
                // JSON 文件可能被写入中断导致截断（例如只剩 '['），此时避免整个应用直接崩溃
                System.err.println("加载 TA 个人信息失败(JSON 解析): " + ex.getMessage());
                // 立即修复为合法 JSON，避免每次启动都重复报错
                saveAll();
                return;
            }
            
            if (list != null) {
                for (TAProfile profile : list) {
                    profilesByTaId.put(profile.getTaId(), profile);
                    if (profile.getEmail() != null) {
                        profilesByEmail.put(profile.getEmail().toLowerCase(), profile);
                    }
                }
            }
            System.out.println("加载 TA 个人信息成功，共 " + profilesByTaId.size() + " 个");
        } catch (IOException e) {
            System.err.println("加载 TA 个人信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存所有 TA 个人信息到文件
     */
    public void saveAll() {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            List<TAProfile> list = new ArrayList<>(profilesByTaId.values());
            gson.toJson(list, writer);
        } catch (IOException e) {
            System.err.println("保存 TA 个人信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据 TA ID 查找个人信息
     */
    public TAProfile findByTaId(Long taId) {
        return profilesByTaId.get(taId);
    }
    
    /**
     * 根据邮箱查找个人信息
     */
    public TAProfile findByEmail(String email) {
        if (email == null) {
            return null;
        }
        return profilesByEmail.get(email.toLowerCase());
    }
    
    /**
     * 保存或更新 TA 个人信息
     */
    public void save(TAProfile profile) {
        if (profile == null || profile.getTaId() == null) {
            return;
        }

        TAProfile existingById = profilesByTaId.get(profile.getTaId());
        if (existingById != null && existingById.getEmail() != null) {
            String oldEmailKey = existingById.getEmail().toLowerCase();
            String newEmailKey = profile.getEmail() == null ? null : profile.getEmail().toLowerCase();
            if (newEmailKey == null || !oldEmailKey.equals(newEmailKey)) {
                profilesByEmail.remove(oldEmailKey);
            }
        }

        if (profile.getEmail() != null) {
            String newEmailKey = profile.getEmail().toLowerCase();
            TAProfile existingByEmail = profilesByEmail.get(newEmailKey);
            if (existingByEmail != null
                    && existingByEmail.getTaId() != null
                    && !existingByEmail.getTaId().equals(profile.getTaId())) {
                profilesByTaId.remove(existingByEmail.getTaId());
            }
            profilesByEmail.put(newEmailKey, profile);
        }

        profilesByTaId.put(profile.getTaId(), profile);
        saveAll();
    }
    
    /**
     * 删除 TA 个人信息
     */
    public boolean delete(Long taId) {
        TAProfile removed = profilesByTaId.remove(taId);
        if (removed != null && removed.getEmail() != null) {
            profilesByEmail.remove(removed.getEmail().toLowerCase());
            saveAll();
            return true;
        }
        return false;
    }
    
    /**
     * 检查 TA 个人信息是否存在
     */
    public boolean exists(Long taId) {
        return profilesByTaId.containsKey(taId);
    }
    
    /**
     * 获取所有 TA 个人信息
     */
    public List<TAProfile> findAll() {
        return new ArrayList<>(profilesByTaId.values());
    }
    
    /**
     * 获取资料完整的 TA 列表
     */
    public List<TAProfile> findCompletedProfiles() {
        List<TAProfile> completed = new ArrayList<>();
        for (TAProfile profile : profilesByTaId.values()) {
            if (profile.isProfileCompleted()) {
                completed.add(profile);
            }
        }
        return completed;
    }
}