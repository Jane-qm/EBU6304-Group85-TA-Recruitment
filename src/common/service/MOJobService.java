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

    /**
     * Iteration 2 listing filters (time filter = deadline after now can be added by teammates).
     *
     * @param modulePrefix match start of module code (case-insensitive), blank = any
     * @param requirementKeyword substring match in {@link MOJob#getRequiredSkills()}, blank = any
     */
    public List<MOJob> filterPublishedJobs(String modulePrefix, String requirementKeyword) {
        List<MOJob> out = new ArrayList<>(listPublishedJobs());
        if (modulePrefix != null && !modulePrefix.isBlank()) {
            String p = modulePrefix.trim().toLowerCase();
            out.removeIf(j -> j.getModuleCode() == null
                    || !j.getModuleCode().toLowerCase().startsWith(p));
        }
        if (requirementKeyword != null && !requirementKeyword.isBlank()) {
            String k = requirementKeyword.trim().toLowerCase();
            out.removeIf(j -> {
                if (j.getRequiredSkills() == null || j.getRequiredSkills().isEmpty()) {
                    return true;
                }
                return j.getRequiredSkills().stream()
                        .noneMatch(s -> s != null && s.toLowerCase().contains(k));
            });
        }
        return out;
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
