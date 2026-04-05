package common.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MOJob {
    private Long jobId;
    private Long moUserId;
    /** Shown to TA on detail view; optional until MO profile is wired. */
    private String moDisplayName;
    private String moduleCode;
    private String title;
    private String description;
    private List<String> requiredSkills = new ArrayList<>();
    private int weeklyHours;
    /** Iteration 2: close date for applications (optional). */
    private LocalDateTime applicationDeadline;
    /** Iteration 2: target headcount / quota (optional). */
    private Integer recruitmentQuota;
    private String status;
    private LocalDateTime createdAt;

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public Long getMoUserId() { return moUserId; }
    public void setMoUserId(Long moUserId) { this.moUserId = moUserId; }
    public String getMoDisplayName() { return moDisplayName; }
    public void setMoDisplayName(String moDisplayName) { this.moDisplayName = moDisplayName; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    public int getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }
    public LocalDateTime getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDateTime applicationDeadline) { this.applicationDeadline = applicationDeadline; }
    public Integer getRecruitmentQuota() { return recruitmentQuota; }
    public void setRecruitmentQuota(Integer recruitmentQuota) { this.recruitmentQuota = recruitmentQuota; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
