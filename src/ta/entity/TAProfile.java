package ta.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TA 个人信息实体类
 * 根据实际 TA 招募问卷设计
 * 
 * @author Can Chen
 * @version 1.1 - 添加校区字段
 */
public class TAProfile {
    
    // 性别枚举
    public enum Gender {
        FEMALE("Female", "女"),
        MALE("Male", "男");
        
        private final String englishName;
        private final String chineseName;
        
        Gender(String englishName, String chineseName) {
            this.englishName = englishName;
            this.chineseName = chineseName;
        }
        
        public String getEnglishName() { return englishName; }
        public String getChineseName() { return chineseName; }
        
        public static Gender fromEnglishName(String name) {
            for (Gender gender : values()) {
                if (gender.englishName.equals(name)) {
                    return gender;
                }
            }
            return null;
        }
    }
    
    // 学生类型枚举
    public enum StudentType {
        MASTER("Masters/MSc", "硕士"),
        PHD("PhD", "博士");
        
        private final String englishName;
        private final String chineseName;
        
        StudentType(String englishName, String chineseName) {
            this.englishName = englishName;
            this.chineseName = chineseName;
        }
        
        public String getEnglishName() { return englishName; }
        public String getChineseName() { return chineseName; }
        
        public static StudentType fromEnglishName(String name) {
            for (StudentType type : values()) {
                if (type.englishName.equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    // 年级枚举
    public enum Year {
        YEAR_1("Year 1", "一年级"),
        YEAR_2("Year 2", "二年级"),
        YEAR_3("Year 3", "三年级"),
        YEAR_4("Year 4", "四年级"),
        YEAR_5("Year 5", "五年级");
        
        private final String englishName;
        private final String chineseName;
        
        Year(String englishName, String chineseName) {
            this.englishName = englishName;
            this.chineseName = chineseName;
        }
        
        public String getEnglishName() { return englishName; }
        public String getChineseName() { return chineseName; }
        
        public static Year fromEnglishName(String name) {
            for (Year year : values()) {
                if (year.englishName.equals(name)) {
                    return year;
                }
            }
            return YEAR_1;
        }
        
        public static Year fromDisplayName(String displayName) {
            for (Year year : values()) {
                if (year.getChineseName().equals(displayName)) {
                    return year;
                }
            }
            return YEAR_1;
        }
    }
    
    // 校区枚举
    public enum Campus {
        XITUCHENG("XituCheng", "西土城校区"),
        SHAHE("ShaHe", "沙河校区");
        
        private final String englishName;
        private final String chineseName;
        
        Campus(String englishName, String chineseName) {
            this.englishName = englishName;
            this.chineseName = chineseName;
        }
        
        public String getEnglishName() { return englishName; }
        public String getChineseName() { return chineseName; }
        
        public static Campus fromEnglishName(String name) {
            for (Campus campus : values()) {
                if (campus.englishName.equals(name)) {
                    return campus;
                }
            }
            return XITUCHENG;
        }
    }
    
    private Long taId;
    private String email;
    private String studentId;
    private String surname;
    private String forename;
    private String chineseName;
    private String phone;
    private Gender gender;
    private String school;
    private String supervisor;
    private StudentType studentType;
    private Year currentYear;
    private Campus campus;
    private String previousExperience;
    private List<String> skillTags;
    private String major;
    private int availableWorkingHours;
    private boolean profileCompleted;
    private LocalDateTime profileLastUpdated;
    private LocalDateTime createdAt;
    
    public TAProfile() {
        this.skillTags = new ArrayList<>();
        this.profileCompleted = false;
        this.createdAt = LocalDateTime.now();
        this.profileLastUpdated = LocalDateTime.now();
        this.campus = Campus.XITUCHENG;
    }
    
    public TAProfile(Long taId, String email) {
        this();
        this.taId = taId;
        this.email = email;
    }
    
    // ==================== Getters and Setters ====================
    
    public Long getTaId() {
        return taId;
    }
    
    public void setTaId(Long taId) {
        this.taId = taId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        markAsEdited();
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        markAsEdited();
    }
    
    public String getSurname() {
        return surname;
    }
    
    public void setSurname(String surname) {
        this.surname = surname;
        markAsEdited();
    }
    
    public String getForename() {
        return forename;
    }
    
    public void setForename(String forename) {
        this.forename = forename;
        markAsEdited();
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
        markAsEdited();
    }
    
    public String getFullName() {
        if (surname != null && forename != null) {
            return surname + " " + forename;
        }
        return chineseName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
        markAsEdited();
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
        markAsEdited();
    }
    
    public String getGenderDisplay() {
        return gender != null ? gender.getEnglishName() : "";
    }
    
    public String getSchool() {
        return school;
    }
    
    public void setSchool(String school) {
        this.school = school;
        markAsEdited();
    }
    
    public String getSupervisor() {
        return supervisor;
    }
    
    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
        markAsEdited();
    }
    
    public StudentType getStudentType() {
        return studentType;
    }
    
    public void setStudentType(StudentType studentType) {
        this.studentType = studentType;
        markAsEdited();
    }
    
    public String getStudentTypeDisplay() {
        return studentType != null ? studentType.getEnglishName() : "";
    }
    
    public Year getCurrentYear() {
        return currentYear;
    }
    
    public void setCurrentYear(Year currentYear) {
        this.currentYear = currentYear;
        markAsEdited();
    }
    
    public String getCurrentYearDisplay() {
        return currentYear != null ? currentYear.getEnglishName() : "";
    }
    
    public Campus getCampus() {
        return campus;
    }
    
    public void setCampus(Campus campus) {
        this.campus = campus;
        markAsEdited();
    }
    
    public String getCampusDisplay() {
        return campus != null ? campus.getChineseName() : "";
    }
    
    public String getGradeDisplayName() {
        return currentYear != null ? currentYear.getChineseName() : "";
    }
    
    public void setGradeByDisplayName(String gradeDisplayName) {
        this.currentYear = Year.fromDisplayName(gradeDisplayName);
        markAsEdited();
    }
    
    public String getPreviousExperience() {
        return previousExperience;
    }
    
    public void setPreviousExperience(String previousExperience) {
        this.previousExperience = previousExperience;
        markAsEdited();
    }
    
    public List<String> getSkillTags() {
        return skillTags;
    }
    
    public void setSkillTags(List<String> skillTags) {
        this.skillTags = skillTags;
        markAsEdited();
    }
    
    public void addSkillTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !skillTags.contains(tag.trim())) {
            skillTags.add(tag.trim());
            markAsEdited();
        }
    }
    
    public void removeSkillTag(String tag) {
        if (skillTags.remove(tag)) {
            markAsEdited();
        }
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
        markAsEdited();
    }
    
    public int getAvailableWorkingHours() {
        return availableWorkingHours;
    }
    
    public void setAvailableWorkingHours(int availableWorkingHours) {
        if (availableWorkingHours < 0) {
            throw new IllegalArgumentException("Available working hours cannot be negative");
        }
        this.availableWorkingHours = availableWorkingHours;
        markAsEdited();
    }
    
    public boolean isProfileCompleted() {
        return profileCompleted;
    }
    
    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }
    
    public LocalDateTime getProfileLastUpdated() {
        return profileLastUpdated;
    }
    
    public void setProfileLastUpdated(LocalDateTime profileLastUpdated) {
        this.profileLastUpdated = profileLastUpdated;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    private void markAsEdited() {
        this.profileCompleted = false;
        this.profileLastUpdated = LocalDateTime.now();
    }
    
    public void saveProfile() {
        boolean isComplete = isEmailValid() && isStudentIdValid() && isSurnameValid() 
                && isForenameValid() && isPhoneValid() && gender != null 
                && school != null && !school.trim().isEmpty()
                && supervisor != null && !supervisor.trim().isEmpty()
                && studentType != null && currentYear != null && campus != null;
        this.profileCompleted = isComplete;
        this.profileLastUpdated = LocalDateTime.now();
    }
    
    // ==================== Validation Methods ====================
    
    public boolean isEmailValid() {
        return email != null && !email.trim().isEmpty() && email.contains("@");
    }
    
    public boolean isStudentIdValid() {
        return studentId != null && !studentId.trim().isEmpty();
    }
    
    public boolean isSurnameValid() {
        return surname != null && !surname.trim().isEmpty();
    }
    
    public boolean isForenameValid() {
        return forename != null && !forename.trim().isEmpty();
    }
    
    public boolean isPhoneValid() {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String phoneNum = phone.trim();
        return phoneNum.matches("1[3-9]\\d{9}") || phoneNum.matches("\\d{8,12}");
    }
    
    public int getCompletionPercentage() {
        int total = 13;
        int completed = 0;
        
        if (isEmailValid()) completed++;
        if (isStudentIdValid()) completed++;
        if (isSurnameValid()) completed++;
        if (isForenameValid()) completed++;
        if (chineseName != null && !chineseName.trim().isEmpty()) completed++;
        if (isPhoneValid()) completed++;
        if (gender != null) completed++;
        if (school != null && !school.trim().isEmpty()) completed++;
        if (supervisor != null && !supervisor.trim().isEmpty()) completed++;
        if (studentType != null) completed++;
        if (currentYear != null) completed++;
        if (campus != null) completed++;
        if (previousExperience != null && !previousExperience.trim().isEmpty()) completed++;
        
        return (completed * 100) / total;
    }
    
    public List<String> getMissingFields() {
        List<String> missing = new ArrayList<>();
        
        if (!isEmailValid()) missing.add("Email (QMplus account)");
        if (!isStudentIdValid()) missing.add("Student ID");
        if (!isSurnameValid()) missing.add("Surname");
        if (!isForenameValid()) missing.add("Forename");
        if (chineseName == null || chineseName.trim().isEmpty()) missing.add("Chinese Name");
        if (!isPhoneValid()) missing.add("Phone Number");
        if (gender == null) missing.add("Gender");
        if (school == null || school.trim().isEmpty()) missing.add("School");
        if (supervisor == null || supervisor.trim().isEmpty()) missing.add("Supervisor Name");
        if (studentType == null) missing.add("Student Type (MSc/PhD)");
        if (currentYear == null) missing.add("Current Year");
        if (campus == null) missing.add("Campus");
        
        return missing;
    }
    
    @Override
    public String toString() {
        return "TAProfile{" +
                "taId=" + taId +
                ", email='" + email + '\'' +
                ", studentId='" + studentId + '\'' +
                ", surname='" + surname + '\'' +
                ", forename='" + forename + '\'' +
                ", chineseName='" + chineseName + '\'' +
                ", phone='" + phone + '\'' +
                ", gender=" + gender +
                ", school='" + school + '\'' +
                ", supervisor='" + supervisor + '\'' +
                ", studentType=" + studentType +
                ", currentYear=" + currentYear +
                ", campus=" + campus +
                ", profileCompleted=" + profileCompleted +
                '}';
    }
}