package common.entity;

public class Admin extends User {
    public Admin(String email, String password) {
        super(email, password, UserRole.ADMIN);
    }
}
