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
 *
 * @version 2.1
 * @contributor Jiaze Wang
 * @update
 * - Reconciled profile lookup by taId and email for newly registered TA accounts
 * - Created a fresh profile when an old taId record belonged to a different email
 * - Kept profile initialization compatible with the current TA-only profile model
 *
 * @version 2.2
 * @update
 * - Added refreshProfile method to force reload from file, fixing cache sync issues
 */
public class TAProfileService {
    
    private final TAProfileDAO profileDAO;
    private final UserService userService;
    
    public TAProfileService() {
        this.profileDAO = new TAProfileDAO();
        this.userService = new UserService();
    }
    
    public TAProfile getProfileByTaId(Long taId) {
        return profileDAO.findByTaId(taId);
    }
    
    public TAProfile getProfileByEmail(String email) {
        return profileDAO.findByEmail(email);
    }
    
    public TAProfile getProfileByUser(User user) {
        if (user == null || user.getUserId() == null) {
            return null;
        }

        TAProfile profileById = profileDAO.findByTaId(user.getUserId());
        if (profileById != null) {
            return profileById;
        }

        TAProfile profileByEmail = profileDAO.findByEmail(user.getEmail());
        if (profileByEmail != null) {
            if (profileByEmail.getTaId() == null || !user.getUserId().equals(profileByEmail.getTaId())) {
                profileDAO.delete(profileByEmail.getTaId());
                profileByEmail.setTaId(user.getUserId());
                profileDAO.save(profileByEmail);
            }
            return profileByEmail;
        }

        TAProfile profile = new TAProfile(user.getUserId(), user.getEmail());
        profileDAO.save(profile);
        return profile;
    }
    
    public void saveProfile(TAProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }
        
        User user = userService.findById(profile.getTaId());
        if (user == null || !(user instanceof TA)) {
            throw new IllegalArgumentException("Invalid TA ID: " + profile.getTaId());
        }
        
        validateProfile(profile);
        
        profile.setEmail(user.getEmail());

        profile.saveProfile();
        profileDAO.save(profile);
    }
    
    public void updateProfile(TAProfile profile) {
        if (profile == null || profile.getTaId() == null) {
            throw new IllegalArgumentException("Invalid profile data");
        }
        
        TAProfile existing = profileDAO.findByTaId(profile.getTaId());
        if (existing == null) {
            throw new IllegalArgumentException("Profile not found for TA ID: " + profile.getTaId());
        }
        
        profile.setCreatedAt(existing.getCreatedAt());
        
        profileDAO.save(profile);
    }
    
    private void validateProfile(TAProfile profile) {
        if (profile.getTaId() == null) {
            throw new IllegalArgumentException("TA ID cannot be null");
        }
        
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
    
    public boolean isProfileComplete(Long taId) {
        TAProfile profile = profileDAO.findByTaId(taId);
        return profile != null && profile.isProfileCompleted();
    }
    
    public int getProfileCompletion(Long taId) {
        TAProfile profile = profileDAO.findByTaId(taId);
        if (profile == null) {
            return 0;
        }
        return profile.getCompletionPercentage();
    }
    
    public List<String> getMissingFields(Long taId) {
        TAProfile profile = profileDAO.findByTaId(taId);
        if (profile == null) {
            return java.util.Arrays.asList("Profile not found");
        }
        return profile.getMissingFields();
    }
    
    public TAProfile initializeProfile(Long taId, String email) {
        if (taId == null || email == null) {
            throw new IllegalArgumentException("TA ID and email cannot be null");
        }

        TAProfile existing = profileDAO.findByTaId(taId);
        if (existing != null) {
            if (existing.getEmail() == null || existing.getEmail().isBlank()) {
                existing.setEmail(email);
                profileDAO.save(existing);
            }
            return existing;
        }

        TAProfile existingByEmail = profileDAO.findByEmail(email);
        if (existingByEmail != null) {
            if (!taId.equals(existingByEmail.getTaId())) {
                profileDAO.delete(existingByEmail.getTaId());
                existingByEmail.setTaId(taId);
                profileDAO.save(existingByEmail);
            }
            return existingByEmail;
        }
        
        TAProfile profile = new TAProfile(taId, email);
        profileDAO.save(profile);
        return profile;
    }
    
    /**
     * 强制刷新缓存（从文件重新加载指定 TA 的 Profile）
     * 用于解决缓存不同步问题，确保获取最新的 Profile 数据
     * 
     * @param taId TA 用户 ID
     */
    public void refreshProfile(Long taId) {
        System.out.println("=== TAProfileService.refreshProfile: taId=" + taId + " ===");
        profileDAO.refreshFromFile(taId);
    }
    
    /**
     * 获取并刷新 Profile（确保数据最新）
     * 
     * @param taId TA 用户 ID
     * @return 最新的 TAProfile 对象
     */
    public TAProfile getProfileAndRefresh(Long taId) {
        refreshProfile(taId);
        return profileDAO.findByTaId(taId);
    }
    
    public void deleteProfile(Long taId) {
        profileDAO.delete(taId);
    }
    
    public List<TAProfile> getAllProfiles() {
        return profileDAO.findAll();
    }
    
    public List<TAProfile> getCompletedProfiles() {
        return profileDAO.findCompletedProfiles();
    }
}