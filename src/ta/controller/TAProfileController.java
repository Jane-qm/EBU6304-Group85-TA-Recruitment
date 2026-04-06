package ta.controller;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import common.entity.User;
import ta.entity.TAProfile;
import ta.service.TAProfileService;


public class TAProfileController {
    
    private final TAProfileService profileService;
    
    public TAProfileController() {
        this.profileService = new TAProfileService();
    }
    
    /**
     * 获取 TA 个人资料（为 UI 准备，自动处理 null）
     */
    public TAProfile getProfileForUI(User user) {
        if (user == null || user.getUserId() == null) {
            return null;
        }
        TAProfile profile = profileService.getProfileByTaId(user.getUserId());
        if (profile == null) {
            profile = new TAProfile(user.getUserId(), user.getEmail());
        }
        return profile;
    }
    
    /**
     * 保存个人资料（带用户反馈）
     */
    public boolean saveProfileWithFeedback(TAProfile profile, JFrame parent) {
        try {
            profileService.saveProfile(profile);
            JOptionPane.showMessageDialog(parent, "Profile saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "System error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * 检查个人资料是否完整
     */
    public boolean isProfileComplete(Long taId) {
        return profileService.isProfileComplete(taId);
    }
    
    /**
     * 获取资料完整度百分比
     */
    public int getProfileCompletion(Long taId) {
        return profileService.getProfileCompletion(taId);
    }
    
    /**
     * 获取资料完整状态显示文字
     */
    public String getProfileStatusText(Long taId) {
        boolean isComplete = profileService.isProfileComplete(taId);
        if (isComplete) {
            return "✓ Profile Complete";
        } else {
            int percentage = profileService.getProfileCompletion(taId);
            return "⚠ Profile " + percentage + "% Complete";
        }
    }
    
    /**
     * 获取缺失字段列表
     */
    public java.util.List<String> getMissingFields(Long taId) {
        return profileService.getMissingFields(taId);
    }
}