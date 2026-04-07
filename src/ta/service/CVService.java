package ta.service;

import java.util.List;

import ta.dao.CVDao;
import ta.entity.CVInfo;
import ta.entity.CVManager;

/**
 * CV 业务服务类
 * 负责 CV 的业务逻辑处理
 * 
 * @author System
 * @version 1.0
 */
public class CVService {
    
    private final CVDao cvDao;
    
    public CVService() {
        this.cvDao = new CVDao();
    }
    
    /**
     * 获取 TA 的 CV 管理器
     */
    public CVManager getCVManager(Long taId) {
        return cvDao.getCVManager(taId);
    }
    
    /**
     * 上传 CV
     */
    public CVInfo uploadCV(Long taId, String taEmail, String taName, 
                           String cvName, String description, 
                           String originalFileName, byte[] fileData) {
        
        // 验证参数
        if (taId == null) {
            throw new IllegalArgumentException("TA ID cannot be null");
        }
        if (!CVInfo.isCvNameValid(cvName)) {
            throw new IllegalArgumentException("CV name must be 1-50 characters");
        }
        if (!CVInfo.isFileSizeValid(fileData.length)) {
            throw new IllegalArgumentException("File size exceeds " + CVInfo.getMaxFileSizeDisplay());
        }
        if (!CVInfo.isFileTypeSupported(originalFileName)) {
            throw new IllegalArgumentException("File type not supported. " + CVInfo.getSupportedFileTypes());
        }
        
        // 获取或创建 CV 管理器
        CVManager manager = cvDao.getOrCreateCVManager(taId, taEmail, taName);
        
        // 检查同名 CV 是否存在
        if (manager.isCvNameExists(cvName)) {
            throw new IllegalArgumentException("CV name already exists: " + cvName);
        }
        
        // 保存文件
        String filePath = cvDao.saveCVFile(taId, cvName, fileData);
        if (filePath == null) {
            throw new RuntimeException("Failed to save CV file");
        }
        
        // 创建 CV 信息
        CVInfo cvInfo = new CVInfo(taId, taEmail, taName);
        cvInfo.setCvName(cvName);
        cvInfo.setOriginalFileName(originalFileName);
        cvInfo.setSavedFileName(new java.io.File(filePath).getName());
        cvInfo.setFilePath(filePath);
        cvInfo.setFileType(CVInfo.FileType.fromExtension(CVInfo.getFileExtension(originalFileName)));
        cvInfo.setFileSize(fileData.length);
        cvInfo.setDescription(description);
        
        // 如果是第一个 CV，设为默认
        if (!manager.hasCV()) {
            cvInfo.setDefault(true);
        }
        
        // 保存
        cvDao.saveCV(cvInfo);
        
        return cvInfo;
    }
    
    /**
     * 下载 CV 文件
     */
    public byte[] downloadCV(Long taId, Long cvId) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            throw new IllegalArgumentException("No CV found for TA ID: " + taId);
        }
        
        CVInfo cv = manager.getCVById(cvId);
        if (cv == null) {
            throw new IllegalArgumentException("CV not found with ID: " + cvId);
        }
        
        return cvDao.readCVFile(cv.getFilePath());
    }
    
    /**
     * 下载默认 CV
     */
    public byte[] downloadDefaultCV(Long taId) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            throw new IllegalArgumentException("No CV found for TA ID: " + taId);
        }
        
        CVInfo defaultCV = manager.getDefaultCV();
        if (defaultCV == null) {
            throw new IllegalArgumentException("No default CV found");
        }
        
        return cvDao.readCVFile(defaultCV.getFilePath());
    }
    
    /**
     * 删除 CV
     */
    public boolean deleteCV(Long taId, Long cvId) {
        return cvDao.deleteCV(taId, cvId);
    }
    
    /**
     * 设置默认 CV
     */
    public boolean setDefaultCV(Long taId, Long cvId) {
        return cvDao.setDefaultCV(taId, cvId);
    }
    
    /**
     * 获取所有 CV 名称列表
     */
    public List<String> getCVNames(Long taId) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            return new java.util.ArrayList<>();
        }
        return manager.getCVNames();
    }
    
    /**
     * 根据 CV 名称获取 CV 信息
     */
    public CVInfo getCVByName(Long taId, String cvName) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            return null;
        }
        return manager.getCVByName(cvName);
    }
    
    /**
     * 检查 TA 是否有 CV
     */
    public boolean hasCV(Long taId) {
        CVManager manager = cvDao.getCVManager(taId);
        return manager != null && manager.hasCV();
    }
    
    /**
     * 获取 CV 数量
     */
    public int getCVCount(Long taId) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            return 0;
        }
        return manager.getCVCount();
    }
    
    /**
     * 获取默认 CV 信息
     */
    public CVInfo getDefaultCV(Long taId) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            return null;
        }
        return manager.getDefaultCV();
    }
    
    /**
     * 获取所有 CV 信息
     */
    public List<CVInfo> getAllCVs(Long taId) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            return new java.util.ArrayList<>();
        }
        return manager.getAllCVs();
    }
    
    /**
     * 获取所有 CV 信息（按时间排序）
     */
    public List<CVInfo> getAllCVsSorted(Long taId) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            return new java.util.ArrayList<>();
        }
        return manager.getAllCVsSorted();
    }

    /**
     * 根据 CV ID 获取 CV 信息（需要验证属于该 TA）
     * @param taId TA用户ID
     * @param cvId CV ID
     * @return CVInfo 对象，如果不属于该 TA 则返回 null
     */
    public CVInfo getCVById(Long taId, Long cvId) {
        CVManager manager = cvDao.getCVManager(taId);
        if (manager == null) {
            return null;
        }
        CVInfo cv = manager.getCVById(cvId);
        // 验证 CV 属于该 TA
        if (cv != null && cv.getTaId().equals(taId)) {
            return cv;
        }
        return null;
    }
}