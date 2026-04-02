package common.service;

import common.dao.TAProfileDAO;
import common.entity.TAProfile;

import java.time.LocalDateTime;
import java.util.List;

public class TAProfileService {
    private final TAProfileDAO dao = new TAProfileDAO();

    public TAProfile createOrUpdate(TAProfile profile) {
        profile.setUpdatedAt(LocalDateTime.now());
        return dao.save(profile);
    }

    public List<TAProfile> listAll() {
        return dao.findAll();
    }
}
