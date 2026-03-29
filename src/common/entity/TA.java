package common.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TA extends User {
    private String name;
    private String major;
    private String grade;
    private final List<String> skillTags;
    private int availableWorkingHours;
    private boolean profileSaved;
    private LocalDateTime profileLastUpdated;

    public TA(String email, String password) {
        super(email, password, UserRole.TA);
        this.skillTags = new ArrayList<>();
        this.availableWorkingHours = 0;
        this.profileSaved = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalize(name);
        markEdited();
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = normalize(major);
        markEdited();
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = normalize(grade);
        markEdited();
    }

    public List<String> getSkillTags() {
        return Collections.unmodifiableList(skillTags);
    }

    public void setSkillTags(List<String> tags) {
        skillTags.clear();
        if (tags != null) {
            for (String tag : tags) {
                addSkillTag(tag);
            }
        }
        markEdited();
    }

    public void addSkillTag(String tag) {
        String normalized = normalize(tag);
        if (normalized == null || normalized.isEmpty()) {
            return;
        }
        if (!skillTags.contains(normalized)) {
            skillTags.add(normalized);
            markEdited();
        }
    }

    public void removeSkillTag(String tag) {
        if (tag == null) {
            return;
        }
        if (skillTags.remove(tag.trim())) {
            markEdited();
        }
    }

    public int getAvailableWorkingHours() {
        return availableWorkingHours;
    }

    public void setAvailableWorkingHours(int availableWorkingHours) {
        if (availableWorkingHours < 0) {
            throw new IllegalArgumentException("Available working hours cannot be negative.");
        }
        this.availableWorkingHours = availableWorkingHours;
        markEdited();
    }

    public boolean isProfileSaved() {
        return profileSaved;
    }

    public LocalDateTime getProfileLastUpdated() {
        return profileLastUpdated;
    }

    public void saveProfile() {
        this.profileSaved = true;
        this.profileLastUpdated = LocalDateTime.now();
    }

    /**
     * 仅用于持久化恢复。
     */
    public void restoreProfileState(boolean profileSaved, LocalDateTime profileLastUpdated) {
        this.profileSaved = profileSaved;
        this.profileLastUpdated = profileLastUpdated;
    }

    private void markEdited() {
        this.profileSaved = false;
        this.profileLastUpdated = LocalDateTime.now();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
