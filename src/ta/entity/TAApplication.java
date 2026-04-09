package ta.entity;

import java.time.LocalDateTime;

public class TAApplication {
    private Long applicationId;
    private Long taUserId;
    private Long jobId;
    private Long cvId;          // 新增：关联的CV ID
    private String statement;
    private String status;
    private LocalDateTime appliedAt;

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getTaUserId() { return taUserId; }
    public void setTaUserId(Long taUserId) { this.taUserId = taUserId; }
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public Long getCvId() { return cvId; }
    public void setCvId(Long cvId) { this.cvId = cvId; }
    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
}