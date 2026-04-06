package ta.controller;


public class TAController {
    
    private final TAProfileController profileController;
    private final TAApplicationController applicationController;
    private final TAOfferController offerController;
    private final TAAuthController authController;
    
    public TAController() {
        this.profileController = new TAProfileController();
        this.applicationController = new TAApplicationController();
        this.offerController = new TAOfferController();
        this.authController = new TAAuthController();
    }
    
    public TAProfileController profile() {
        return profileController;
    }
    
    public TAApplicationController application() {
        return applicationController;
    }
    
    public TAOfferController offer() {
        return offerController;
    }
    
    public TAAuthController auth() {
        return authController;
    }
}