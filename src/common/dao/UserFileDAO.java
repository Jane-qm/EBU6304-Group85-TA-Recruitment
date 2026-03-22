package common.dao;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;

import common.entity.User;
import common.entity.TA;
import common.entity.MO;
import common.entity.Admin;

import com.google.gson.*;
import common.entity.User;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;  
import com.google.gson.stream.JsonToken; 

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象
 * 负责用户数据的 JSON 文件读写操作
 * 
 * @author Can Chen
 * @version 1.0
 */
public class UserFileDAO {
    
    /** 用户数据文件路径 */
    private static final String DATA_FILE = "data/users.json";
    
    /** 备份文件路径 */
    private static final String BACKUP_FILE = "data/users_backup.json";
    
    /** Gson 实例（用于 JSON 序列化/反序列化） */
    private Gson gson;
    
    /**
     * 构造函数
     * 初始化 Gson 并确保 data 目录存在
     */
    public UserFileDAO() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())

                // 2. 新增：解决 User 抽象类无法实例化的问题
                .registerTypeAdapter(User.class, new JsonDeserializer<User>() {
                    @Override
                    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        JsonObject jsonObject = json.getAsJsonObject();

                        // 根据 role 字段判断具体的子类
                        if (!jsonObject.has("role")) {
                            throw new JsonParseException("JSON 中缺少 role 字段，无法判断具体类型！");
                        }

                        String role = jsonObject.get("role").getAsString();
                        switch (role) {
                            case "TA":
                                return context.deserialize(jsonObject, TA.class);
                            case "MO":
                                return context.deserialize(jsonObject, MO.class);
                            case "ADMIN":
                                return context.deserialize(jsonObject, Admin.class);
                            default:
                                throw new JsonParseException("未知的用户角色: " + role);
                        }
                    }
                })

                .create();
        ensureDataDirectoryExists();
    }
    
    /**
     * 确保 data 目录存在
     */
    private void ensureDataDirectoryExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdir();
            if (!created) {
                throw new RuntimeException("无法创建 data 目录");
            }
        }
    }
    
    /**
     * 保存用户
     * 
     * @param user 要保存的用户
     */
    public void save(User user) {
        try {
            List<User> users = findAll();
            users.add(user);
            saveToFile(users);
        } catch (Exception e) {
            throw new RuntimeException("保存用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新用户
     * 
     * @param updatedUser 更新后的用户
     */
    public void update(User updatedUser) {
        try {
            List<User> users = findAll();
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserId().equals(updatedUser.getUserId())) {
                    users.set(i, updatedUser);
                    saveToFile(users);
                    return;
                }
            }
            throw new RuntimeException("用户不存在: " + updatedUser.getUserId());
        } catch (Exception e) {
            throw new RuntimeException("更新用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 找到的用户，不存在返回 null
     */
    public User findByEmail(String email) {
        try {
            for (User user : findAll()) {
                if (user.getEmail().equalsIgnoreCase(email)) {
                    return user;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("查找用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据ID查找用户
     * 
     * @param id 用户ID
     * @return 找到的用户，不存在返回 null
     */
    public User findById(Long id) {
        try {
            for (User user : findAll()) {
                if (user.getUserId().equals(id)) {
                    return user;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("查找用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 查找所有用户
     * 
     * @return 用户列表（不会返回 null）
     */
    public List<User> findAll() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<User>>(){}.getType();
            List<User> users = gson.fromJson(reader, listType);
            return users != null ? users : new ArrayList<>();
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("读取用户文件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("解析用户数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存到文件（带备份）
     * 
     * @param users 用户列表
     */
    private void saveToFile(List<User> users) {
        // 1. 备份现有文件
        backupExistingFile();
        
        // 2. 写入新文件
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            // 写入失败时尝试恢复备份
            restoreFromBackup();
            throw new RuntimeException("写入用户数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 备份现有文件
     */
    private void backupExistingFile() {
        File currentFile = new File(DATA_FILE);
        if (currentFile.exists()) {
            File backupFile = new File(BACKUP_FILE);
            try (FileInputStream fis = new FileInputStream(currentFile);
                 FileOutputStream fos = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            } catch (IOException e) {
                System.err.println("备份文件失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 从备份恢复
     */
    private void restoreFromBackup() {
        File backupFile = new File(BACKUP_FILE);
        if (backupFile.exists()) {
            try (FileInputStream fis = new FileInputStream(backupFile);
                 FileOutputStream fos = new FileOutputStream(DATA_FILE)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            } catch (IOException e) {
                System.err.println("恢复备份失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * LocalDateTime 序列化适配器
     */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }
        
        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {  // 现在能正确识别 JsonToken
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }
}