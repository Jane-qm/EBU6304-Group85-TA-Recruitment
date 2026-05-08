package modules.user;

public class MO extends User {
    public MO(String email, String password) {
        super(email, password, UserRole.MO);
    }
}
