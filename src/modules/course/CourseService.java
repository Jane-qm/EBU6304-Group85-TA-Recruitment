package modules.course;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * 课程业务服务
 *
 * @version 1.0
 */
public class CourseService {
    private final CourseDAO dao = new CourseDAO();

    /**
     * 获取所有课程
     */
    public List<Course> getAllCourses() {
        return dao.findAll();
    }

    /**
     * 根据课程编号查找课程
     */
    public Course findByModuleCode(String moduleCode) {
        if (moduleCode == null) return null;
        return dao.findAll().stream()
                .filter(c -> moduleCode.equalsIgnoreCase(c.getModuleCode()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加单门课程
     */
    public Course addCourse(String moduleCode, String title) {
        if (moduleCode == null || moduleCode.isBlank()) {
            throw new IllegalArgumentException("课程编号不能为空");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }

        // 检查是否已存在
        if (findByModuleCode(moduleCode) != null) {
            throw new IllegalArgumentException("课程编号已存在: " + moduleCode);
        }

        Course course = new Course(moduleCode, title);
        return dao.save(course);
    }

    /**
     * 批量导入课程
     */
    public int importCourses(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return 0;
        }

        int added = 0;
        for (Course course : courses) {
            try {
                addCourse(course.getModuleCode(), course.getTitle());
                added++;
            } catch (IllegalArgumentException e) {
                System.err.println("导入课程失败: " + e.getMessage());
            }
        }
        return added;
    }

    /**
     * 清空所有课程
     */
    public void clearAllCourses() {
        dao.deleteAll();
    }

    /**
     * 获取课程数量
     */
    public int getCourseCount() {
        return dao.findAll().size();
    }

    // modules/course/CourseService.java

    /**
     * 批量导入课程的结果
     */
    public static class CourseImportResult {
        public final int successCount;
        public final int failCount;
        public final List<String> errors;

        public CourseImportResult(int successCount, int failCount, List<String> errors) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.errors = errors;
        }
    }

    /**
     * 从 CSV 文件路径批量导入课程
     * CSV 格式: moduleCode,title
     */
    public CourseImportResult importCoursesFromCSV(String filePath) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // 跳过表头
                if (isFirstLine && (line.toLowerCase().contains("module") || line.toLowerCase().contains("code"))) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    failCount++;
                    errors.add("格式错误: " + line);
                    continue;
                }

                String moduleCode = parts[0].trim();
                String title = parts[1].trim();

                if (moduleCode.isEmpty() || title.isEmpty()) {
                    failCount++;
                    errors.add("课程编号或名称为空: " + line);
                    continue;
                }

                try {
                    addCourse(moduleCode, title);
                    successCount++;
                } catch (IllegalArgumentException ex) {
                    failCount++;
                    errors.add(moduleCode + " - " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            return new CourseImportResult(0, 1, List.of("读取文件失败: " + e.getMessage()));
        }

        return new CourseImportResult(successCount, failCount, errors);
    }
}