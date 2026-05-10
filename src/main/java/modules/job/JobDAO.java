package modules.job;

import infrastructure.persistence.JsonPersistenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class JobDAO {
    private final JsonPersistenceManager persistenceManager = new JsonPersistenceManager();
    private final AtomicLong idGenerator = new AtomicLong(2000L);

    public JobDAO() {
        persistenceManager.initializeBaseFiles();
        for (Job job : findAll()) {
            if (job.getJobId() != null && job.getJobId() > idGenerator.get()) {
                idGenerator.set(job.getJobId());
            }
        }
    }

    public List<Job> findAll() {
        return new ArrayList<>(persistenceManager.readList(JsonPersistenceManager.MO_JOBS_FILE, Job.class));
    }

    public Job save(Job job) {
        List<Job> all = findAll();
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
