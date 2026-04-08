package ta.controller;

import common.entity.TA;
import common.entity.User;
import common.service.UserService;
import ta.entity.TAProfile;
import ta.service.TAProfileService;


public class TAAuthController {
    
    private final UserService userService;
    private final TAProfileService profileService;
    
    public TAAuthController() {
        this.userService = new UserService();
        this.profileService = new TAProfileService();
    }
    
    /**
     * 获取 TA 用户对象
     */
    public TA getTAUser(User user) {
        if (user instanceof TA) {
            return (TA) user;
        }
        return null;
    }
    
    /**
     * 获取 TA 完整信息（包括个人资料）
     */
    public TAInfo getTAInfo(Long userId) {
        User user = userService.findById(userId);
        if (user == null || !(user instanceof TA)) {
            return null;
        }
        
        TAProfile profile = profileService.getProfileByTaId(userId);
        if (profile == null) {
            profile = new TAProfile(userId, user.getEmail());
        }
        
        return new TAInfo((TA) user, profile);
    }
    
    /**
     * 获取显示名称（优先使用个人资料中的姓名）
     */
    public String getDisplayName(Long userId) {
        TAProfile profile = profileService.getProfileByTaId(userId);
        if (profile != null && profile.getFullName() != null && !profile.getFullName().isEmpty()) {
            return profile.getFullName();
        }
        User user = userService.findById(userId);
        return user != null ? user.getEmail() : "TA User";
    }
    
    /**
     * 获取显示名称（从 User 对象）
     */
    public String getDisplayName(User user) {
        if (user == null) {
            return "TA User";
        }
        return getDisplayName(user.getUserId());
    }
    
    /**
     * 获取角色显示文字
     */
    public String getRoleDisplayText() {
        return "Teaching Assistant";
    }
    
    /**
     * 检查用户是否是 TA
     */
    public boolean isTA(User user) {
        return user != null && user instanceof TA;
    }
    
    /**
     * TA 完整信息内部类
     */
    public static class TAInfo {
        public final TA ta;
        public final TAProfile profile;
        
        public TAInfo(TA ta, TAProfile profile) {
            this.ta = ta;
            this.profile = profile;
        }
        
        public String getDisplayName() {
            if (profile != null && profile.getFullName() != null && !profile.getFullName().isEmpty()) {
                return profile.getFullName();
            }
            return ta.getEmail();
        }
    }
}