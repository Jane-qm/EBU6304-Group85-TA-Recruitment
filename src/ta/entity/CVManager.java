package ta.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CV 管理器
 * 管理单个 TA 的多个 CV 文件
 */
public class CVManager {
    
    private Long taId;
    private String taEmail;
    private String taName;
    private List<CVInfo> cvList;
    
    public CVManager(Long taId, String taEmail, String taName) {
        this.taId = taId;
        this.taEmail = taEmail;
        this.taName = taName;
        this.cvList = new ArrayList<>();
    }
    
    public Long getTaId() {
        return taId;
    }
    
    public String getTaEmail() {
        return taEmail;
    }
    
    public String getTaName() {
        return taName;
    }
    
    public List<CVInfo> getAllCVs() {
        return cvList;
    }
    
    public List<CVInfo> getAllCVsSorted() {
        return cvList.stream()
                .sorted((c1, c2) -> c2.getUploadedAt().compareTo(c1.getUploadedAt()))
                .collect(Collectors.toList());
    }
    
    public CVInfo getDefaultCV() {
        for (CVInfo cv : cvList) {
            if (cv.isDefault()) {
                return cv;
            }
        }
        // 如果没有默认 CV，返回第一个
        return cvList.isEmpty() ? null : cvList.get(0);
    }
    
    public CVInfo getCVByName(String cvName) {
        for (CVInfo cv : cvList) {
            if (cv.getCvName().equals(cvName)) {
                return cv;
            }
        }
        return null;
    }
    
    public CVInfo getCVById(Long cvId) {
        for (CVInfo cv : cvList) {
            if (cv.getCvId().equals(cvId)) {
                return cv;
            }
        }
        return null;
    }
    
    /**
     * 添加 CV
     */
    public void addCV(CVInfo cv) {
        cvList.add(cv);
    }
    
    /**
     * 删除 CV
     */
    public boolean removeCV(Long cvId) {
        CVInfo toRemove = getCVById(cvId);
        if (toRemove != null) {
            cvList.remove(toRemove);
            
            // 如果删除的是默认 CV，且还有其他 CV，则设最新的为默认
            if (toRemove.isDefault() && !cvList.isEmpty()) {
                CVInfo latest = cvList.stream()
                        .max((c1, c2) -> c2.getUploadedAt().compareTo(c1.getUploadedAt()))
                        .orElse(null);
                if (latest != null) {
                    latest.setDefault(true);
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * 设置默认 CV
     */
    public boolean setDefaultCV(Long cvId) {
        CVInfo target = getCVById(cvId);
        if (target == null) {
            return false;
        }
        
        // 将所有 CV 设为非默认
        for (CVInfo cv : cvList) {
            cv.setDefault(false);
        }
        
        target.setDefault(true);
        return true;
    }
    
    /**
     * 检查 CV 名称是否已存在
     */
    public boolean isCvNameExists(String cvName) {
        for (CVInfo cv : cvList) {
            if (cv.getCvName().equals(cvName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取 CV 数量
     */
    public int getCVCount() {
        return cvList.size();
    }
    
    /**
     * 是否有 CV
     */
    public boolean hasCV() {
        return !cvList.isEmpty();
    }
    
    /**
     * 获取所有 CV 名称列表
     */
    public List<String> getCVNames() {
        return cvList.stream()
                .map(CVInfo::getCvName)
                .collect(Collectors.toList());
    }
}