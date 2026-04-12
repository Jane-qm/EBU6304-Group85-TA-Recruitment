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
 * @version 2.0 - 修复缓存同步问题，确保保存后内存缓存与文件一致
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
            System.out.println("TA profiles file not found, will create new one when saving.");
            return;
        }
        
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<TAProfile>>(){}.getType();
            List<TAProfile> list;
            try {
                list = gson.fromJson(reader, listType);
            } catch (Exception ex) {
                System.err.println("加载 TA 个人信息失败(JSON 解析): " + ex.getMessage());
                saveAll();
                return;
            }
            
            if (list != null) {
                for (TAProfile profile : list) {
                    if (profile.getTaId() != null) {
                        profilesByTaId.put(profile.getTaId(), profile);
                        if (profile.getEmail() != null) {
                            profilesByEmail.put(profile.getEmail().toLowerCase(), profile);
                        }
                        // 调试日志：确认加载的数据完整
                        System.out.println("加载 Profile: taId=" + profile.getTaId() + 
                                           ", studentId=" + profile.getStudentId() +
                                           ", surname=" + profile.getSurname() +
                                           ", profileCompleted=" + profile.isProfileCompleted());
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
            System.out.println("保存 TA 个人信息成功，共 " + list.size() + " 个");
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
     * 保存或更新 TA 个人信息（修复缓存同步问题）
     */
    public void save(TAProfile profile) {
        if (profile == null || profile.getTaId() == null) {
            System.err.println("save: profile is null or taId is null");
            return;
        }
        
        System.out.println("=== save 开始 ===");
        System.out.println("保存的 profile: taId=" + profile.getTaId() + 
                           ", studentId=" + profile.getStudentId() +
                           ", surname=" + profile.getSurname() +
                           ", email=" + profile.getEmail() +
                           ", profileCompleted=" + profile.isProfileCompleted());
        
        // 1. 处理旧的邮箱索引（如果邮箱变更）
        TAProfile existingById = profilesByTaId.get(profile.getTaId());
        if (existingById != null && existingById.getEmail() != null) {
            String oldEmailKey = existingById.getEmail().toLowerCase();
            String newEmailKey = profile.getEmail() == null ? null : profile.getEmail().toLowerCase();
            if (newEmailKey == null || !oldEmailKey.equals(newEmailKey)) {
                System.out.println("移除旧邮箱索引: " + oldEmailKey);
                profilesByEmail.remove(oldEmailKey);
            }
        }
        
        // 2. 处理邮箱索引冲突
        if (profile.getEmail() != null) {
            String newEmailKey = profile.getEmail().toLowerCase();
            TAProfile existingByEmail = profilesByEmail.get(newEmailKey);
            if (existingByEmail != null
                    && existingByEmail.getTaId() != null
                    && !existingByEmail.getTaId().equals(profile.getTaId())) {
                System.out.println("邮箱冲突，移除旧的 TA: " + existingByEmail.getTaId());
                profilesByTaId.remove(existingByEmail.getTaId());
            }
            // 更新邮箱索引（关键修复：确保使用最新的 profile 对象）
            profilesByEmail.put(newEmailKey, profile);
        }
        
        // 3. 更新 ID 索引（关键修复：使用完整的 profile 对象覆盖缓存）
        profilesByTaId.put(profile.getTaId(), profile);
        
        // 4. 保存到文件
        saveAll();
        
        // 5. 验证缓存是否更新成功
        TAProfile verify = profilesByTaId.get(profile.getTaId());
        System.out.println("缓存验证: taId=" + verify.getTaId() + 
                           ", studentId=" + verify.getStudentId() +
                           ", surname=" + verify.getSurname());
        System.out.println("=== save 结束 ===");
    }
    
    /**
     * 强制刷新缓存（从文件重新加载指定 TA 的 Profile）
     * 用于解决缓存不同步问题
     */
    public void refreshFromFile(Long taId) {
        System.out.println("=== refreshFromFile: taId=" + taId + " ===");
        
        // 重新加载整个文件
        Map<Long, TAProfile> tempById = new ConcurrentHashMap<>();
        Map<String, TAProfile> tempByEmail = new ConcurrentHashMap<>();
        
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<TAProfile>>(){}.getType();
                List<TAProfile> list = gson.fromJson(reader, listType);
                if (list != null) {
                    for (TAProfile profile : list) {
                        if (profile.getTaId() != null) {
                            tempById.put(profile.getTaId(), profile);
                            if (profile.getEmail() != null) {
                                tempByEmail.put(profile.getEmail().toLowerCase(), profile);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("refreshFromFile 失败: " + e.getMessage());
                return;
            }
        }
        
        // 更新缓存
        profilesByTaId.clear();
        profilesByEmail.clear();
        profilesByTaId.putAll(tempById);
        profilesByEmail.putAll(tempByEmail);
        
        System.out.println("refreshFromFile 完成，共 " + profilesByTaId.size() + " 个 Profile");
        
        // 验证指定的 TA 是否加载成功
        TAProfile refreshed = profilesByTaId.get(taId);
        if (refreshed != null) {
            System.out.println("刷新后的 Profile: studentId=" + refreshed.getStudentId() +
                               ", surname=" + refreshed.getSurname() +
                               ", profileCompleted=" + refreshed.isProfileCompleted());
        } else {
            System.out.println("警告: taId=" + taId + " 在刷新后的缓存中不存在");
        }
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
    
    /**
     * 获取缓存大小（用于调试）
     */
    public int getCacheSize() {
        return profilesByTaId.size();
    }
    
    /**
     * 清空缓存（用于测试）
     */
    public void clearCache() {
        profilesByTaId.clear();
        profilesByEmail.clear();
    }
    
}