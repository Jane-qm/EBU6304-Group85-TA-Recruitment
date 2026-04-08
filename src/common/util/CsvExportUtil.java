package common.util;

import common.entity.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for exporting object lists to CSV files.
 *
 * Contributor: Jiaze Wang
 */
public final class CsvExportUtil {

    private CsvExportUtil() {
    }

    /**
     * Exports a list of objects to a CSV file using reflection.
     * This method is suitable for homogeneous object lists.
     */
    public static <T> Path exportObjects(String fileName, List<T> rows) throws IOException {
        Path exportDir = Path.of("exports");
        Files.createDirectories(exportDir);

        Path filePath = exportDir.resolve(fileName);

        if (rows == null || rows.isEmpty()) {
            Files.writeString(filePath, "No Data\n", StandardCharsets.UTF_8);
            return filePath;
        }

        Class<?> clazz = rows.get(0).getClass();
        Field[] fields = clazz.getDeclaredFields();

        StringBuilder builder = new StringBuilder();

        List<String> headers = new ArrayList<>();
        for (Field field : fields) {
            headers.add(escape(field.getName()));
        }
        builder.append(String.join(",", headers)).append("\n");

        for (T row : rows) {
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(row);
                    values.add(escape(value == null ? "" : String.valueOf(value)));
                } catch (IllegalAccessException e) {
                    values.add("\"\"");
                }
            }
            builder.append(String.join(",", values)).append("\n");
        }

        Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
        return filePath;
    }

    /**
     * Exports user data using common fields only.
     * This avoids reflection issues caused by mixed subclasses such as TA, MO, and Admin.
     */
    public static Path exportUsers(String fileName, List<User> users) throws IOException {
        Path exportDir = Path.of("exports");
        Files.createDirectories(exportDir);

        Path filePath = exportDir.resolve(fileName);

        StringBuilder builder = new StringBuilder();
        builder.append("\"userId\",\"email\",\"role\",\"status\",\"createdAt\",\"lastLogin\"\n");

        if (users != null) {
            for (User user : users) {
                builder.append(escape(user.getUserId() == null ? "" : String.valueOf(user.getUserId()))).append(",");
                builder.append(escape(user.getEmail())).append(",");
                builder.append(escape(user.getRole() == null ? "" : String.valueOf(user.getRole()))).append(",");
                builder.append(escape(user.getStatus() == null ? "" : String.valueOf(user.getStatus()))).append(",");
                builder.append(escape(user.getCreatedAt() == null ? "" : String.valueOf(user.getCreatedAt()))).append(",");
                builder.append(escape(user.getLastLogin() == null ? "" : String.valueOf(user.getLastLogin()))).append("\n");
            }
        }

        Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
        return filePath;
    }

    /**
     * Escapes a CSV field.
     */
    private static String escape(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}