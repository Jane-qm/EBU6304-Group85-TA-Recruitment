package common.service;

import common.entity.AccountStatus;
import common.entity.Admin;
import common.entity.MO;
import common.entity.TA;
import common.entity.User;
import common.entity.UserRole;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100000L);

    public UserService() {
        seedDemoUsers();
    }

    public User register(String email, String password, UserRole role) {
        String normalizedEmail = normalizeEmail(email);
        if (usersByEmail.containsKey(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User user = createUser(normalizedEmail, password, role);
        user.setUserId(idGenerator.incrementAndGet());
        user.setStatus(AccountStatus.PENDING);
        usersByEmail.put(normalizedEmail, user);
        return user;
    }

    public User login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        User user = usersByEmail.get(normalizedEmail);
        if (user == null || !user.checkPassword(password)) {
            return null;
        }
        user.setLastLogin(LocalDateTime.now());
        return user;
    }

    public User findByEmail(String email) {
        return usersByEmail.get(normalizeEmail(email));
    }

    public boolean emailExists(String email) {
        return usersByEmail.containsKey(normalizeEmail(email));
    }

    public void updatePassword(String email, String newPassword) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Email is not registered.");
        }
        user.setPassword(newPassword);
    }

    private User createUser(String email, String password, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }
        return switch (role) {
            case TA -> new TA(email, password);
            case MO -> new MO(email, password);
            case ADMIN -> new Admin(email, password);
        };
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank.");
        }
        return email.trim().toLowerCase();
    }

    private void seedDemoUsers() {
        User activeTa = new TA("ta@test.com", "123456");
        activeTa.setUserId(idGenerator.incrementAndGet());
        activeTa.setStatus(AccountStatus.ACTIVE);
        usersByEmail.put(activeTa.getEmail(), activeTa);

        User pendingMo = new MO("mo@test.com", "123456");
        pendingMo.setUserId(idGenerator.incrementAndGet());
        pendingMo.setStatus(AccountStatus.PENDING);
        usersByEmail.put(pendingMo.getEmail(), pendingMo);
    }
}
