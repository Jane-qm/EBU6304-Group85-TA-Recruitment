package ta.entity;

import java.time.LocalDateTime;
import common.domain.ApplicationStatus;

/**
 * TA 申请实体（包含 Offer 信息）
 * 
 * @version 3.0 - 合并 Offer 功能
 */
public class TAApplication {
    
    // ==================== 基础字段 ====================
    
    private Long applicationId;
    private Long taUserId;
    private Long jobId;
    private Long cvId;
    private String statement;
    private String status;
    private LocalDateTime appliedAt;
    
    // ==================== Offer 相关字段 ====================
    
    private Integer offeredHours;           // 提供的工时
    private LocalDateTime offerSentAt;      // Offer 发送时间
    private LocalDateTime offerExpiryAt;    // Offer 有效期
    private LocalDateTime respondedAt;      // TA 响应时间
    
    // ==================== Getters and Setters ====================
    
    public Long getApplicationId() { 
        return applicationId; 
    }
    
    public void setApplicationId(Long applicationId) { 
        this.applicationId = applicationId; 
    }
    
    public Long getTaUserId() { 
        return taUserId; 
    }
    
    public void setTaUserId(Long taUserId) { 
        this.taUserId = taUserId; 
    }
    
    public Long getJobId() { 
        return jobId; 
    }
    
    public void setJobId(Long jobId) { 
        this.jobId = jobId; 
    }
    
    public Long getCvId() { 
        return cvId; 
    }
    
    public void setCvId(Long cvId) { 
        this.cvId = cvId; 
    }
    
    public String getStatement() { 
        return statement; 
    }
    
    public void setStatement(String statement) { 
        this.statement = statement; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
    
    public LocalDateTime getAppliedAt() { 
        return appliedAt; 
    }
    
    public void setAppliedAt(LocalDateTime appliedAt) { 
        this.appliedAt = appliedAt; 
    }
    
    public Integer getOfferedHours() { 
        return offeredHours; 
    }
    
    public void setOfferedHours(Integer offeredHours) { 
        this.offeredHours = offeredHours; 
    }
    
    public LocalDateTime getOfferSentAt() { 
        return offerSentAt; 
    }
    
    public void setOfferSentAt(LocalDateTime offerSentAt) { 
        this.offerSentAt = offerSentAt; 
    }
    
    public LocalDateTime getOfferExpiryAt() { 
        return offerExpiryAt; 
    }
    
    public void setOfferExpiryAt(LocalDateTime offerExpiryAt) { 
        this.offerExpiryAt = offerExpiryAt; 
    }
    
    public LocalDateTime getRespondedAt() { 
        return respondedAt; 
    }
    
    public void setRespondedAt(LocalDateTime respondedAt) { 
        this.respondedAt = respondedAt; 
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 判断 Offer 是否已过期
     */
    public boolean isOfferExpired() {
        return offerExpiryAt != null && LocalDateTime.now().isAfter(offerExpiryAt);
    }
    
    /**
     * 判断是否可以响应 Offer
     */
    public boolean canRespondToOffer() {
        return ApplicationStatus.OFFER_SENT.equals(status) && !isOfferExpired();
    }
    
    @Override
    public String toString() {
        return "TAApplication{" +
                "applicationId=" + applicationId +
                ", taUserId=" + taUserId +
                ", jobId=" + jobId +
                ", status='" + status + '\'' +
                ", offeredHours=" + offeredHours +
                '}';
    }
}