package common.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 助教实体类
 * 继承 User，代表申请助教岗位的学生
 *
 * <p>包含 Requirement C 所需的全部申请人画像字段：
 * 专业、年级、技能标签、工时，以及辅助字段：学号、简历路径。</p>
 *
 * @author Can Chen
 * @author Zhixuan Guo
 * @version 2.0
 */
public class TA extends User {

    private static final long serialVersionUID = 2L;

    // ==================== Requirement C Fields ====================

    /** 所在专业，例如 "Computer Science"、"Software Engineering" */
    private String major;

    /**
     * 年级 / 在读阶段。
     * 建议取值：YEAR_1 / YEAR_2 / YEAR_3 / YEAR_4 / POSTGRADUATE
     * 使用字符串以保持持久化兼容性。
     */
    private String grade;

    /**
     * 技能标签列表，例如 ["Java", "Python", "Data Structures"]。
     * 初始化为空列表，避免 NPE。
     */
    private List<String> skillTags;

    /**
     * 每周可投入工时（小时/周）。
     * Requirement C 中定义的最大可用工时限制。
     */
    private int weeklyHours;

    // ==================== Additional TA Profile Fields ====================

    /** 学号，用于与学校信息系统对接 */
    private String studentId;

    /** 简历文件路径或 URL，供 MO 审阅 */
    private String cvPath;

    // ==================== Constructors ====================

    /**
     * 最简构造函数，兼容注册流程（Requirement C 字段后续通过 setter 填写）。
     *
     * @param email    注册邮箱
     * @param password 明文密码（由父类加密存储）
     */
    public TA(String email, String password) {
        super(email, password, UserRole.TA);
        this.skillTags = new ArrayList<>();
    }

    /**
     * 完整构造函数，一次性设置全部 Requirement C 字段。
     *
     * @param email       注册邮箱
     * @param password    明文密码
     * @param major       所在专业
     * @param grade       年级（如 "YEAR_2"）
     * @param skillTags   技能标签列表，传入 null 时自动初始化为空列表
     * @param weeklyHours 每周可投入工时（小时）
     * @author Zhixuan Guo
     */
    public TA(String email, String password,
              String major, String grade,
              List<String> skillTags, int weeklyHours) {
        super(email, password, UserRole.TA);
        this.major       = major;
        this.grade       = grade;
        this.skillTags   = (skillTags != null) ? new ArrayList<>(skillTags) : new ArrayList<>();
        this.weeklyHours = weeklyHours;
    }

    // ==================== Skill Tag Helpers ====================

    /**
     * 向技能标签列表追加一个标签（自动去重）。
     *
     * @param tag 技能标签，例如 "Python"
     * @author Zhixuan Guo
     */
    public void addSkillTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !skillTags.contains(tag.trim())) {
            skillTags.add(tag.trim());
        }
    }

    /**
     * 从技能标签列表中移除一个标签。
     *
     * @param tag 要移除的标签
     * @author Zhixuan Guo
     */
    public void removeSkillTag(String tag) {
        skillTags.remove(tag);
    }

    // ==================== Getters and Setters ====================

    /** @author Zhixuan Guo */
    public String getMajor() {
        return major;
    }

    /** @author Zhixuan Guo */
    public void setMajor(String major) {
        this.major = major;
    }

    /** @author Zhixuan Guo */
    public String getGrade() {
        return grade;
    }

    /** @author Zhixuan Guo */
    public void setGrade(String grade) {
        this.grade = grade;
    }

    /** @author Zhixuan Guo */
    public List<String> getSkillTags() {
        return new ArrayList<>(skillTags);
    }

    /**
     * 替换整个技能标签列表（防御性拷贝）。
     *
     * @param skillTags 新标签列表，传入 null 时清空列表
     * @author Zhixuan Guo
     */
    public void setSkillTags(List<String> skillTags) {
        this.skillTags = (skillTags != null) ? new ArrayList<>(skillTags) : new ArrayList<>();
    }

    /** @author Zhixuan Guo */
    public int getWeeklyHours() {
        return weeklyHours;
    }

    /**
     * 设置每周可投入工时。
     *
     * @param weeklyHours 工时（小时），必须 &gt; 0
     * @throws IllegalArgumentException 工时不合法时抛出
     * @author Zhixuan Guo
     */
    public void setWeeklyHours(int weeklyHours) {
        if (weeklyHours < 0) {
            throw new IllegalArgumentException("工时不能为负数");
        }
        this.weeklyHours = weeklyHours;
    }

    /** @author Zhixuan Guo */
    public String getStudentId() {
        return studentId;
    }

    /** @author Zhixuan Guo */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /** @author Zhixuan Guo */
    public String getCvPath() {
        return cvPath;
    }

    /** @author Zhixuan Guo */
    public void setCvPath(String cvPath) {
        this.cvPath = cvPath;
    }

    // ==================== Object Overrides ====================

    /**
     * @author Zhixuan Guo
     */
    @Override
    public String toString() {
        return "TA{" +
                "userId=" + getUserId() +
                ", email='" + getEmail() + '\'' +
                ", status=" + getStatus() +
                ", studentId='" + studentId + '\'' +
                ", major='" + major + '\'' +
                ", grade='" + grade + '\'' +
                ", skillTags=" + skillTags +
                ", weeklyHours=" + weeklyHours +
                ", cvPath='" + cvPath + '\'' +
                '}';
    }
}
