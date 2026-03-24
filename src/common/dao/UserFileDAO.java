package common.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import common.entity.User;
import common.entity.TA;
import common.entity.MO;
import common.entity.Admin;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户数据访问对象
 * 负责用户数据的 JSON 文件读写操作
 * 
 * @author Can Chen
 * @version 2.0
 */
public class UserFileDAO {
    
    private static final String DATA_FILE = "data/users.json";
    private final Gson gson;
    
    public UserFileDAO() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        ensureDataDirectoryExists();
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
     * 保存所有用户到文件
     * 
     * @param users 用户 Map
     */
    public void saveAll(Map<String, User> users) {
        if (users == null) {
            return;
        }
        
        try (Writer writer = new FileWriter(DATA_FILE)) {
            List<User> userList = new ArrayList<>(users.values());
            gson.toJson(userList, writer);
        } catch (IOException e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从文件加载所有用户
     * 
     * @return 用户列表
     */
    public List<User> loadAll() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<User>>(){}.getType();
            List<User> users = gson.fromJson(reader, listType);
            return users != null ? users : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("读取用户数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * LocalDateTime 自定义适配器
     */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(FORMATTER));
            }
        }
        
        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String dateStr = in.nextString();
            if (dateStr == null || dateStr.equals("null")) {
                return null;
            }
            return LocalDateTime.parse(dateStr, FORMATTER);
        }
    }
}