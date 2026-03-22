package common.dao;

import common.entity.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Data Access Object
 * Responsible for JSON file read and write operations of user data
 */
public class UserFileDAO {

    /** User data file path */
    private static final String DATA_FILE = "data/users.json";

    /** Backup file path */
    private static final String BACKUP_FILE = "data/users_backup.json";

    /** Gson instance for JSON serialization/deserialization */
    private Gson gson;

    /**
     * Constructor
     * Initialize Gson and ensure the data directory exists
     */
    public UserFileDAO() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        ensureDataDirectoryExists();
    }

    /**
     * Ensure the data directory exists
     */
    private void ensureDataDirectoryExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdir();
            if (!created) {
                throw new RuntimeException("Failed to create data directory");
            }
        }
    }

    /**
     * Save a user
     * @param user The user to save
     */
    public void save(User user) {
        try {
            List<User> users = findAll();
            users.add(user);
            saveToFile(users);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    /**
     * Update a user
     * @param updatedUser The updated user
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
            throw new RuntimeException("User does not exist: " + updatedUser.getUserId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    /**
     * Find user by email
     * @param email User email
     * @return The found user, or null if not exists
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
            throw new RuntimeException("Failed to find user: " + e.getMessage(), e);
        }
    }

    /**
     * Find user by ID
     * @param id User ID
     * @return The found user, or null if not exists
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
            throw new RuntimeException("Failed to find user: " + e.getMessage(), e);
        }
    }

    /**
     * Find all users
     * @return User list (never null)
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
            throw new RuntimeException("Failed to read user file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user data: " + e.getMessage(), e);
        }
    }

    /**
     * Save to file with backup
     * @param users User list
     */
    private void saveToFile(List<User> users) {
        // 1. Backup existing file
        backupExistingFile();

        // 2. Write new file
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            // Restore from backup if write fails
            restoreFromBackup();
            throw new RuntimeException("Failed to write user data: " + e.getMessage(), e);
        }
    }

    /**
     * Backup existing file
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
                System.err.println("Failed to backup file: " + e.getMessage());
            }
        }
    }

    /**
     * Restore from backup
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
                System.err.println("Failed to restore from backup: " + e.getMessage());
            }
        }
    }

    /**
     * LocalDateTime serialization adapter
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
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }
}
