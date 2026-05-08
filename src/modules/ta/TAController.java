package modules.ta;

import modules.application.ApplicationController;
import modules.auth.TAAuthController;
import modules.profile.TAProfileController;

/**
 * TA 模块统一控制器
 * 聚合所有 TA 相关的子控制器
 * 
 * @version 3.0
 */
public class TAController {
    
    private final TAProfileController profileController;
    private final ApplicationController applicationController;
    private final TAAuthController authController;
    
    public TAController() {
        this.profileController = new TAProfileController();
        this.applicationController = new ApplicationController();
        this.authController = new TAAuthController();
    }
    
    /**
     * 获取个人资料控制器
     */
    public TAProfileController profile() {
        return profileController;
    }
    
    /**
     * 获取申请控制器（包含 Offer 功能）
     */
    public ApplicationController application() {
        return applicationController;
    }
    
    /**
     * 获取认证控制器
     */
    public TAAuthController auth() {
        return authController;
    }
}