package modules.user;

public class MO extends User {
    private String name;  // 新增：MO姓名
    
    public MO(String email, String password) {
        super(email, password, UserRole.MO);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "MO{" +
                "userId=" + getUserId() +
                ", email='" + getEmail() + '\'' +
                ", name='" + name + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}