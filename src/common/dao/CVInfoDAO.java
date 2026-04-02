package common.dao;

import common.entity.CVInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class CVInfoDAO {
    private final JsonPersistenceManager persistenceManager = new JsonPersistenceManager();
    private final AtomicLong idGenerator = new AtomicLong(4000L);

    public CVInfoDAO() {
        persistenceManager.initializeBaseFiles();
        for (CVInfo cvInfo : findAll()) {
            if (cvInfo.getCvId() != null && cvInfo.getCvId() > idGenerator.get()) {
                idGenerator.set(cvInfo.getCvId());
            }
        }
    }

    public List<CVInfo> findAll() {
        return new ArrayList<>(persistenceManager.readList(JsonPersistenceManager.CV_INFOS_FILE, CVInfo.class));
    }

    public CVInfo save(CVInfo cvInfo) {
        List<CVInfo> all = findAll();
        if (cvInfo.getCvId() == null) {
            cvInfo.setCvId(idGenerator.incrementAndGet());
            all.add(cvInfo);
        } else {
            boolean updated = false;
            for (int i = 0; i < all.size(); i++) {
                if (cvInfo.getCvId().equals(all.get(i).getCvId())) {
                    all.set(i, cvInfo);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                all.add(cvInfo);
            }
        }
        persistenceManager.writeList(JsonPersistenceManager.CV_INFOS_FILE, all);
        return cvInfo;
    }
}
