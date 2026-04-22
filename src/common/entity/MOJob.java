package common.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MO 职位实体
 * 
 * @version 3.0 - 新增申请截止日期字段
 */
public class MOJob {
    private Long jobId;
    private Long moUserId;
    private String moduleCode;
    private String title;
    private String description;
    private List<String> requiredSkills = new ArrayList<>();
    private int weeklyHours;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime applicationDeadline;  // 新增：申请截止日期

    // ==================== Getters and Setters ====================
    
    public Long getJobId() { 
        return jobId; 
    }
    
    public void setJobId(Long jobId) { 
        this.jobId = jobId; 
    }
    
    public Long getMoUserId() { 
        return moUserId; 
    }
    
    public void setMoUserId(Long moUserId) { 
        this.moUserId = moUserId; 
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
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public List<String> getRequiredSkills() { 
        return requiredSkills; 
    }
    
    public void setRequiredSkills(List<String> requiredSkills) { 
        this.requiredSkills = requiredSkills; 
    }
    
    public int getWeeklyHours() { 
        return weeklyHours; 
    }
    
    public void setWeeklyHours(int weeklyHours) { 
        this.weeklyHours = weeklyHours; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public LocalDateTime getApplicationDeadline() { 
        return applicationDeadline; 
    }
    
    public void setApplicationDeadline(LocalDateTime applicationDeadline) { 
        this.applicationDeadline = applicationDeadline; 
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 判断职位是否可申请
     */
    public boolean isApplicable() {
        if (!"PUBLISHED".equals(status) && !"OPEN".equals(status)) {
            return false;
        }
        if (applicationDeadline != null && LocalDateTime.now().isAfter(applicationDeadline)) {
            return false;
        }
        return true;
    }
    
    /**
     * 判断职位是否已过期
     */
    public boolean isExpired() {
        return applicationDeadline != null && LocalDateTime.now().isAfter(applicationDeadline);
    }
    
    @Override
    public String toString() {
        return "MOJob{" +
                "jobId=" + jobId +
                ", moduleCode='" + moduleCode + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", applicationDeadline=" + applicationDeadline +
                '}';
    }
}