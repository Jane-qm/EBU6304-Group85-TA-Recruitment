package common.entity;

import java.time.LocalDateTime;

public class MOOffer {
    private Long offerId;
    private Long applicationId;
    private Long moUserId;
    private Long taUserId;
    private String moduleCode;
    private int offeredHours;
    private String status;
    private LocalDateTime offeredAt;
    private LocalDateTime respondedAt;

    public Long getOfferId() { return offerId; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getMoUserId() { return moUserId; }
    public void setMoUserId(Long moUserId) { this.moUserId = moUserId; }
    public Long getTaUserId() { return taUserId; }
    public void setTaUserId(Long taUserId) { this.taUserId = taUserId; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public int getOfferedHours() { return offeredHours; }
    public void setOfferedHours(int offeredHours) { this.offeredHours = offeredHours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getOfferedAt() { return offeredAt; }
    public void setOfferedAt(LocalDateTime offeredAt) { this.offeredAt = offeredAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
}
