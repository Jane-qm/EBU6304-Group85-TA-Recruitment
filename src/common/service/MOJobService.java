package common.service;

import common.dao.MOJobDAO;
import common.entity.MOJob;

import java.time.LocalDateTime;
import java.util.List;

public class MOJobService {
    private final MOJobDAO dao = new MOJobDAO();

    public MOJob createOrUpdate(MOJob job) {
        if (job.getCreatedAt() == null) {
            job.setCreatedAt(LocalDateTime.now());
        }
        return dao.save(job);
    }

    public List<MOJob> listAll() {
        return dao.findAll();
    }
}
