package modules.course;

import infrastructure.persistence.JsonPersistenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class CourseDAO {
    private final JsonPersistenceManager persistenceManager;
    private final AtomicLong idGenerator = new AtomicLong(4000L);

    public CourseDAO() {
        this.persistenceManager = new JsonPersistenceManager();
        this.persistenceManager.initializeBaseFiles();

        // 初始化 ID 生成器
        for (Course course : findAll()) {
            if (course.getCourseId() != null && course.getCourseId() > idGenerator.get()) {
                idGenerator.set(course.getCourseId());
            }
        }
    }

    /**
     * 获取所有课程
     */
    public List<Course> findAll() {
        return new ArrayList<>(persistenceManager.readList(
                JsonPersistenceManager.COURSES_FILE,
                Course.class));
    }

    /**
     * 保存课程（新增或更新）
     */
    public Course save(Course course) {
        List<Course> all = findAll();

        if (course.getCourseId() == null) {
            // 新增
            course.setCourseId(idGenerator.incrementAndGet());
            all.add(course);
        } else {
            // 更新
            boolean updated = false;
            for (int i = 0; i < all.size(); i++) {
                if (course.getCourseId().equals(all.get(i).getCourseId())) {
                    all.set(i, course);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                all.add(course);
            }
        }

        persistenceManager.writeList(JsonPersistenceManager.COURSES_FILE, all);
        return course;
    }

    /**
     * 批量保存课程
     */
    public void saveAll(List<Course> courses) {
        for (Course course : courses) {
            if (course.getCourseId() == null) {
                course.setCourseId(idGenerator.incrementAndGet());
            }
        }
        persistenceManager.writeList(JsonPersistenceManager.COURSES_FILE, courses);
    }

    /**
     * 清空所有课程
     */
    public void deleteAll() {
        persistenceManager.writeList(JsonPersistenceManager.COURSES_FILE, new ArrayList<>());
    }
}