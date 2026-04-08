package ta.service;

import java.util.List;

import common.entity.TA;
import common.entity.User;
import common.service.UserService;
import ta.dao.TAProfileDAO;
import ta.entity.TAProfile;

/**
 * TA 个人信息业务服务
 * 负责 TA 个人信息的业务逻辑处理
 * 
 * @author Can Chen
 * @version 2.0 - 移除对 TA 对象的冗余同步
 */
public class TAProfileService {
    
    private final TAProfileDAO profileDAO;
    private final UserService userService;
    
    public TAProfileService() {
        this.profileDAO = new TAProfileDAO();
        this.userService = new UserService();
    }
    
    /**
     * 根据 TA ID 获取个人信息
     */
    public TAProfile getProfileByTaId(Long taId) {
        return profileDAO.findByTaId(taId);
    }
    
    /**
     * 根据邮箱获取 TA 个人信息
     */
    public TAProfile getProfileByEmail(String email) {
        return profileDAO.findByEmail(email);
    }
    
    /**
     * 根据 User 对象获取 TA 个人信息
     */
    public TAProfile getProfileByUser(User user) {
        if (user == null || user.getUserId() == null) {
            return null;
        }
        return profileDAO.findByTaId(user.getUserId());
    }
    
    /**
     * 保存 TA 个人信息
     * 修改：不再同步更新 TA 对象中的冗余字段
     */
    public void saveProfile(TAProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }
        
        // 验证用户是否存在且为 TA
        User user = userService.findById(profile.getTaId());
        if (user == null || !(user instanceof TA)) {
            throw new IllegalArgumentException("Invalid TA ID: " + profile.getTaId());
        }
        
        // 验证数据
        validateProfile(profile);
        
        // 保存资料
        profile.saveProfile();
        profileDAO.save(profile);
        
        // 移除：不再同步更新 TA 对象中的冗余字段
        // TA 对象只保留账户信息，个人资料统一从 TAProfile 获取
    }
    
    /**
     * 更新 TA 个人信息（不验证完整性）
     */
    public void updateProfile(TAProfile profile) {
        if (profile == null || profile.getTaId() == null) {
            throw new IllegalArgumentException("Invalid profile data");
        }
        
        TAProfile existing = profileDAO.findByTaId(profile.getTaId());
        if (existing == null) {
            throw new IllegalArgumentException("Profile not found for TA ID: " + profile.getTaId());
        }
        
        // 保留创建时间
        profile.setCreatedAt(existing.getCreatedAt());
        
        // 保存更新
        profileDAO.save(profile);
    }
    
    /**
     * 验证个人信息
     */
    private void validateProfile(TAProfile profile) {
        if (profile.getTaId() == null) {
            throw new IllegalArgumentException("TA ID cannot be null");
        }
        
        // 验证必填字段
        if (!profile.isEmailValid()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!profile.isStudentIdValid()) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (!profile.isSurnameValid()) {
            throw new IllegalArgumentException("Surname is required");
        }
        if (!profile.isForenameValid()) {
            throw new IllegalArgumentException("Forename is required");
        }
        if (!profile.isPhoneValid()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (profile.getGender() == null) {
            throw new IllegalArgumentException("Gender is required");
        }
        if (profile.getSchool() == null || profile.getSchool().trim().isEmpty()) {
            throw new IllegalArgumentException("School is required");
        }
        if (profile.getSupervisor() == null || profile.getSupervisor().trim().isEmpty()) {
            throw new IllegalArgumentException("Supervisor name is required");
        }
        if (profile.getStudentType() == null) {
            throw new IllegalArgumentException("Student type is required");
        }
        if (profile.getCurrentYear() == null) {
            throw new IllegalArgumentException("Current year is required");
        }
    }
    
    /**
     * 检查 TA 个人信息是否完整
     */
    public boolean isProfileComplete(Long taId) {
        TAProfile profile = profileDAO.findByTaId(taId);
        return profile != null && profile.isProfileCompleted();
    }
    
    /**
     * 获取资料完整度百分比
     */
    public int getProfileCompletion(Long taId) {
        TAProfile profile = profileDAO.findByTaId(taId);
        if (profile == null) {
            return 0;
        }
        return profile.getCompletionPercentage();
    }
    
    /**
     * 获取缺失字段列表
     */
    public List<String> getMissingFields(Long taId) {
        TAProfile profile = profileDAO.findByTaId(taId);
        if (profile == null) {
            return java.util.Arrays.asList("Profile not found");
        }
        return profile.getMissingFields();
    }
    
    /**
     * 初始化 TA 个人信息（注册时调用）
     */
    public TAProfile initializeProfile(Long taId, String email) {
        if (taId == null || email == null) {
            throw new IllegalArgumentException("TA ID and email cannot be null");
        }
        
        TAProfile profile = new TAProfile(taId, email);
        profileDAO.save(profile);
        return profile;
    }
    
    /**
     * 删除 TA 个人信息
     */
    public void deleteProfile(Long taId) {
        profileDAO.delete(taId);
    }
    
    /**
     * 获取所有 TA 个人信息
     */
    public List<TAProfile> getAllProfiles() {
        return profileDAO.findAll();
    }
    
    /**
     * 获取资料完整的 TA 列表
     */
    public List<TAProfile> getCompletedProfiles() {
        return profileDAO.findCompletedProfiles();
    }
}