package common.service;

import common.dao.TAApplicationDAO;
import common.entity.TAApplication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TAApplicationService {
    private final TAApplicationDAO dao = new TAApplicationDAO();

    public TAApplication createOrUpdate(TAApplication application) {
        if (application.getAppliedAt() == null) {
            application.setAppliedAt(LocalDateTime.now());
        }
        return dao.save(application);
    }

    public List<TAApplication> listAll() {
        return dao.findAll();
    }

    public List<TAApplication> listByStatus(String status) {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication application : dao.findAll()) {
            if (application.getStatus() != null && application.getStatus().equalsIgnoreCase(status)) {
                result.add(application);
            }
        }
        return result;
    }

    public List<TAApplication> listByTaUserId(Long taUserId) {
        List<TAApplication> result = new ArrayList<>();
        for (TAApplication application : dao.findAll()) {
            if (taUserId != null && taUserId.equals(application.getTaUserId())) {
                result.add(application);
            }
        }
        return result;
    }

    public TAApplication markAsHired(Long applicationId) {
        List<TAApplication> all = dao.findAll();
        for (TAApplication application : all) {
            if (applicationId != null && applicationId.equals(application.getApplicationId())) {
                application.setStatus("HIRED");
                return dao.save(application);
            }
        }
        throw new IllegalArgumentException("Application not found.");
    }
}
