package common.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import common.entity.AccountStatus;
import common.entity.Admin;
import common.entity.MO;
import common.entity.TA;
import common.entity.User;
import common.entity.UserRole;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            List<PersistedUser> userList = new ArrayList<>();
            for (User user : users.values()) {
                userList.add(PersistedUser.fromEntity(user));
            }
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
    public List<User> loadAll() throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<PersistedUser>>(){}.getType();
            List<PersistedUser> rows = gson.fromJson(reader, listType);
            List<User> users = new ArrayList<>();
            if (rows == null) {
                return users;
            }

            for (PersistedUser row : rows) {
                users.add(row.toEntity());
            }
            return users;
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
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String dateStr = in.nextString();
            if (dateStr == null || dateStr.isBlank()) {
                return null;
            }
            return LocalDateTime.parse(dateStr, FORMATTER);
        }
    }

    /**
     * 用于 JSON 存储的中间结构，避免抽象类 User 直接反序列化。
     */
    private static class PersistedUser {
        private Long userId;
        private String email;
        private String passwordHash;
        private UserRole role;
        private AccountStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime lastLogin;

        // TA profile fields
        private String name;
        private String major;
        private String grade;
        private List<String> skillTags;
        private Integer availableWorkingHours;
        private Boolean profileSaved;
        private LocalDateTime profileLastUpdated;

        static PersistedUser fromEntity(User user) {
            PersistedUser row = new PersistedUser();
            row.userId = user.getUserId();
            row.email = user.getEmail();
            row.passwordHash = user.getPasswordHash();
            row.role = user.getRole();
            row.status = user.getStatus();
            row.createdAt = user.getCreatedAt();
            row.lastLogin = user.getLastLogin();

            if (user instanceof TA ta) {
                row.name = ta.getName();
                row.major = ta.getMajor();
                row.grade = ta.getGrade();
                row.skillTags = new ArrayList<>(ta.getSkillTags());
                row.availableWorkingHours = ta.getAvailableWorkingHours();
                row.profileSaved = ta.isProfileSaved();
                row.profileLastUpdated = ta.getProfileLastUpdated();
            }
            return row;
        }

        User toEntity() {
            if (role == null) {
                throw new IllegalStateException("Invalid user data: role is missing.");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("Invalid user data: email is missing.");
            }
            if (passwordHash == null || passwordHash.isBlank()) {
                throw new IllegalStateException("Invalid user data: password hash is missing.");
            }

            User user = switch (role) {
                case TA -> {
                    TA ta = new TA(email, "temp_password");
                    ta.setName(name);
                    ta.setMajor(major);
                    ta.setGrade(grade);
                    if (skillTags != null) {
                        ta.setSkillTags(skillTags);
                    }
                    if (availableWorkingHours != null) {
                        ta.setAvailableWorkingHours(availableWorkingHours);
                    }
                    ta.restoreProfileState(Boolean.TRUE.equals(profileSaved), profileLastUpdated);
                    yield ta;
                }
                case MO -> new MO(email, "temp_password");
                case ADMIN -> new Admin(email, "temp_password");
            };

            user.setPasswordHash(passwordHash);
            if (userId != null) {
                user.setUserId(userId);
            }
            if (status != null) {
                user.setStatus(status);
            }
            if (createdAt != null) {
                user.setCreatedAt(createdAt);
            }
            if (lastLogin != null) {
                user.setLastLogin(lastLogin);
            }
            return user;
        }
    }
}