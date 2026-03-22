package common.dao;

import common.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserFileDAO {
    public void save(User user) {
        throw new UnsupportedOperationException("File persistence will be implemented in next iteration.");
    }

    public void update(User user) {
        throw new UnsupportedOperationException("File persistence will be implemented in next iteration.");
    }

    public User findByEmail(String email) {
        return null;
    }

    public User findById(Long id) {
        return null;
    }

    public List<User> findAll() {
        return new ArrayList<>();
    }
}
