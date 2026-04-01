package ta.entity;

import java.time.LocalDateTime;

/**
 * CV 信息实体类
 * 支持多个 CV 文件，用自定义名称区分
 */
public class CVInfo {
    
    // 文件类型枚举
    public enum FileType {
        PDF("pdf"),
        DOC("doc"),
        DOCX("docx");
        
        private final String extension;
        
        FileType(String extension) {
            this.extension = extension;
        }
        
        public String getExtension() { return extension; }
        
        public static FileType fromExtension(String extension) {
            for (FileType type : values()) {
                if (type.extension.equalsIgnoreCase(extension)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    private Long cvId;                      // CV ID
    private Long taId;                      // TA ID
    private String taEmail;                 // TA 邮箱（冗余）
    private String taName;                  // TA 姓名（冗余）
    private String cvName;                  // CV 名称（用户自定义，用于区分不同CV）
    private String originalFileName;        // 原始文件名
    private String savedFileName;           // 保存的文件名
    private String filePath;                // 文件存储路径
    private FileType fileType;              // 文件类型
    private long fileSize;                  // 文件大小（bytes）
    private String description;             // CV 描述（可选）
    private boolean isDefault;              // 是否为默认CV（用于申请时自动选择）
    private LocalDateTime uploadedAt;       // 上传时间
    private LocalDateTime updatedAt;        // 更新时间
    
    // 文件大小常量
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;  // 5MB
    
    public CVInfo() {
        this.isDefault = false;
        this.uploadedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public CVInfo(Long taId, String taEmail, String taName) {
        this();
        this.taId = taId;
        this.taEmail = taEmail;
        this.taName = taName;
    }
    
    // ==================== Getters and Setters ====================
    
    public Long getCvId() {
        return cvId;
    }
    
    public void setCvId(Long cvId) {
        this.cvId = cvId;
    }
    
    public Long getTaId() {
        return taId;
    }
    
    public void setTaId(Long taId) {
        this.taId = taId;
    }
    
    public String getTaEmail() {
        return taEmail;
    }
    
    public void setTaEmail(String taEmail) {
        this.taEmail = taEmail;
    }
    
    public String getTaName() {
        return taName;
    }
    
    public void setTaName(String taName) {
        this.taName = taName;
    }
    
    public String getCvName() {
        return cvName;
    }
    
    public void setCvName(String cvName) {
        this.cvName = cvName;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public String getSavedFileName() {
        return savedFileName;
    }
    
    public void setSavedFileName(String savedFileName) {
        this.savedFileName = savedFileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public FileType getFileType() {
        return fileType;
    }
    
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileSizeDisplay() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getUploadedAtDisplay() {
        return uploadedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * 生成保存文件名
     * 格式: ta_{taId}_{cvName}_{timestamp}.{extension}
     */
    public String generateSavedFileName() {
        String timestamp = uploadedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = fileType != null ? fileType.getExtension() : "pdf";
        
        // 使用 CV 名称，去除特殊字符
        String safeName = cvName != null ? cvName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_") : "cv";
        return String.format("ta_%d_%s_%s.%s", taId, safeName, timestamp, extension);
    }
    
    /**
     * 检查文件大小是否有效
     */
    public static boolean isFileSizeValid(long size) {
        return size > 0 && size <= MAX_FILE_SIZE;
    }
    
    /**
     * 检查文件类型是否支持
     */
    public static boolean isFileTypeSupported(String fileName) {
        if (fileName == null) {
            return false;
        }
        String extension = getFileExtension(fileName);
        return FileType.fromExtension(extension) != null;
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * 获取支持的文件类型列表
     */
    public static String getSupportedFileTypes() {
        return "PDF files (*.pdf), DOC files (*.doc), DOCX files (*.docx)";
    }
    
    /**
     * 获取支持的文件扩展名数组
     */
    public static String[] getSupportedExtensions() {
        return new String[]{"pdf", "doc", "docx"};
    }
    
    /**
     * 获取文件大小限制显示
     */
    public static String getMaxFileSizeDisplay() {
        return "5 MB";
    }
    
    /**
     * 验证 CV 名称是否有效
     */
    public static boolean isCvNameValid(String cvName) {
        return cvName != null && !cvName.trim().isEmpty() && cvName.length() <= 50;
    }
    
    @Override
    public String toString() {
        return "CVInfo{" +
                "cvId=" + cvId +
                ", taId=" + taId +
                ", cvName='" + cvName + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileType=" + fileType +
                ", fileSize=" + getFileSizeDisplay() +
                ", isDefault=" + isDefault +
                '}';
    }
}