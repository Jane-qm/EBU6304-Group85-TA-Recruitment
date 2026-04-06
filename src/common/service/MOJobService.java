package common.service;

import common.dao.MOJobDAO;
import common.entity.MOJob;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /***
     * Unpublished positions will also be blocked from viewing and application.
     *  The corresponding position release capability will be supplemented.
     */

    public List<MOJob> listPublishedJobs() {
        List<MOJob> result = new ArrayList<>();
        for (MOJob job : dao.findAll()) {
            if (job.getStatus() != null && (
                    job.getStatus().equalsIgnoreCase("OPEN")
                            || job.getStatus().equalsIgnoreCase("PUBLISHED"))) {
                result.add(job);
            }
        }
        return result;
    }

    public MOJob getPublishedJob(Long jobId) {
        for (MOJob job : listPublishedJobs()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                return job;
            }
        }
        return null;
    }

    public MOJob publishJob(Long jobId) {
        for (MOJob job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                job.setStatus("PUBLISHED");
                return dao.save(job);
            }
        }
        throw new IllegalArgumentException("Job not found.");
    }
}
