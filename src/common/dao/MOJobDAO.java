package common.dao;

import common.entity.MOJob;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MOJobDAO {
    private final JsonPersistenceManager persistenceManager = new JsonPersistenceManager();
    private final AtomicLong idGenerator = new AtomicLong(2000L);

    public MOJobDAO() {
        persistenceManager.initializeBaseFiles();
        for (MOJob job : findAll()) {
            if (job.getJobId() != null && job.getJobId() > idGenerator.get()) {
                idGenerator.set(job.getJobId());
            }
        }
    }

    public List<MOJob> findAll() {
        return new ArrayList<>(persistenceManager.readList(JsonPersistenceManager.MO_JOBS_FILE, MOJob.class));
    }

    public MOJob save(MOJob job) {
        List<MOJob> all = findAll();
        if (job.getJobId() == null) {
            job.setJobId(idGenerator.incrementAndGet());
            all.add(job);
        } else {
            boolean updated = false;
            for (int i = 0; i < all.size(); i++) {
                if (job.getJobId().equals(all.get(i).getJobId())) {
                    all.set(i, job);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                all.add(job);
            }
        }
        persistenceManager.writeList(JsonPersistenceManager.MO_JOBS_FILE, all);
        return job;
    }
}
