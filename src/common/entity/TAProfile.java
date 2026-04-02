package common.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TAProfile {
    private Long profileId;
    private Long userId;
    private String name;
    private String major;
    private String grade;
    private List<String> skillTags = new ArrayList<>();
    private int availableWorkingHours;
    private String selfIntroduction;
    private boolean published;
    private LocalDateTime updatedAt;

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public List<String> getSkillTags() { return skillTags; }
    public void setSkillTags(List<String> skillTags) { this.skillTags = skillTags; }
    public int getAvailableWorkingHours() { return availableWorkingHours; }
    public void setAvailableWorkingHours(int availableWorkingHours) { this.availableWorkingHours = availableWorkingHours; }
    public String getSelfIntroduction() { return selfIntroduction; }
    public void setSelfIntroduction(String selfIntroduction) { this.selfIntroduction = selfIntroduction; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
