package modules.course;

public class Course {
    private Long courseId;
    private String moduleCode;    // 课程编号，如 EBU6304
    private String title;         // 课程名称，如 Software Engineering

    public Course() {}

    public Course(String moduleCode, String title) {
        this.moduleCode = moduleCode;
        this.title = title;
    }

    // ==================== Getters and Setters ====================

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return moduleCode + " - " + title;
    }
}