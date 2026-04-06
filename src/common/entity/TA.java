package common.entity;

public class TA extends User {
    // 注意：以下字段已移至 ta.entity.TAProfile
    // 这里只保留账户核心字段，不再存储个人资料
    
    public TA(String email, String password) {
        super(email, password, UserRole.TA);
    }

    // 移除所有个人资料相关字段和方法
    // 包括：name, major, grade, skillTags, availableWorkingHours, 
    // profileSaved, profileLastUpdated 及其 getter/setter
    
    @Override
    public String toString() {
        return "TA{" +
                "userId=" + getUserId() +
                ", email='" + getEmail() + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}