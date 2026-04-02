package common.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CVInfo {
    private Long cvId;
    private Long userId;
    private String educationSummary;
    private List<String> projectHighlights = new ArrayList<>();
    private List<String> certificates = new ArrayList<>();
    private String filePath;
    private LocalDateTime updatedAt;

    public Long getCvId() { return cvId; }
    public void setCvId(Long cvId) { this.cvId = cvId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEducationSummary() { return educationSummary; }
    public void setEducationSummary(String educationSummary) { this.educationSummary = educationSummary; }
    public List<String> getProjectHighlights() { return projectHighlights; }
    public void setProjectHighlights(List<String> projectHighlights) { this.projectHighlights = projectHighlights; }
    public List<String> getCertificates() { return certificates; }
    public void setCertificates(List<String> certificates) { this.certificates = certificates; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
