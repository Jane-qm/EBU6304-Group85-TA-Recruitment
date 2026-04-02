package common.service;

import common.dao.CVInfoDAO;
import common.entity.CVInfo;

import java.time.LocalDateTime;
import java.util.List;

public class CVInfoService {
    private final CVInfoDAO dao = new CVInfoDAO();

    public CVInfo createOrUpdate(CVInfo cvInfo) {
        cvInfo.setUpdatedAt(LocalDateTime.now());
        return dao.save(cvInfo);
    }

    public List<CVInfo> listAll() {
        return dao.findAll();
    }
}
