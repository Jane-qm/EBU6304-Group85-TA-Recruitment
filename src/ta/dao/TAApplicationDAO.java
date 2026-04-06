package ta.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import common.dao.JsonPersistenceManager;
import ta.entity.TAApplication;

public class TAApplicationDAO {
    private final JsonPersistenceManager persistenceManager = new JsonPersistenceManager();
    private final AtomicLong idGenerator = new AtomicLong(3000L);

    public TAApplicationDAO() {
        persistenceManager.initializeBaseFiles();
        for (TAApplication application : findAll()) {
            if (application.getApplicationId() != null && application.getApplicationId() > idGenerator.get()) {
                idGenerator.set(application.getApplicationId());
            }
        }
    }

    public List<TAApplication> findAll() {
        return new ArrayList<>(persistenceManager.readList(JsonPersistenceManager.TA_APPLICATIONS_FILE, TAApplication.class));
    }

    public TAApplication save(TAApplication application) {
        List<TAApplication> all = findAll();
        if (application.getApplicationId() == null) {
            application.setApplicationId(idGenerator.incrementAndGet());
            all.add(application);
        } else {
            boolean updated = false;
            for (int i = 0; i < all.size(); i++) {
                if (application.getApplicationId().equals(all.get(i).getApplicationId())) {
                    all.set(i, application);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                all.add(application);
            }
        }
        persistenceManager.writeList(JsonPersistenceManager.TA_APPLICATIONS_FILE, all);
        return application;
    }
}