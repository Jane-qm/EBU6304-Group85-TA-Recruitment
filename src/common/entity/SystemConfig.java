package common.entity;

import java.time.LocalDateTime;

/**
 * Global system configuration.
 * Stores application cycle settings controlled by the administrator.
 *
 * Contributor: Jiaze Wang
 */
public class SystemConfig {
    private LocalDateTime applicationStart;
    private LocalDateTime applicationEnd;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public LocalDateTime getApplicationStart() {
        return applicationStart;
    }

    public void setApplicationStart(LocalDateTime applicationStart) {
        this.applicationStart = applicationStart;
    }

    public LocalDateTime getApplicationEnd() {
        return applicationEnd;
    }

    public void setApplicationEnd(LocalDateTime applicationEnd) {
        this.applicationEnd = applicationEnd;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Returns true if both start and end time have been configured.
     */
    public boolean isConfigured() {
        return applicationStart != null && applicationEnd != null;
    }

    /**
     * Returns true if the configured date range is valid.
     */
    public boolean isValidRange() {
        return applicationStart != null
                && applicationEnd != null
                && !applicationEnd.isBefore(applicationStart);
    }
}