package common.service;

import common.dao.MOJobDAO;
import common.entity.MOJob;
import ta.dao.TAApplicationDAO;
import ta.entity.TAApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * MO job service.
 *
 * @version 2.0
 * @contributor Jiaze Wang
 * @update
 * - Added deadline parsing from formatted job description
 * - Enforced application cycle validation when saving and publishing jobs
 */
public class MOJobService {
    private final MOJobDAO dao = new MOJobDAO();
    private final TAApplicationDAO appDao = new TAApplicationDAO();
    private final SystemConfigService systemConfigService = new SystemConfigService();

    public MOJob createOrUpdate(MOJob job) {
        if (job == null) {
            throw new IllegalArgumentException("Job must not be null.");
        }
        if (job.getCreatedAt() == null) {
            job.setCreatedAt(LocalDateTime.now());
        }

        validateDeadlineWithinCycle(job);
        return dao.save(job);
    }

    public List<MOJob> listAll() {
        return dao.findAll();
    }

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
                validateDeadlineWithinCycle(job);
                job.setStatus("PUBLISHED");
                return dao.save(job);
            }
        }
        throw new IllegalArgumentException("Job not found.");
    }

    /**
     * MO-009.1: Closes recruitment for a job.
     *
     * Sets status to "CLOSED" and persists the change. Once closed:
     * - listPublishedJobs() will no longer return this job (it only returns OPEN/PUBLISHED),
     *   so TAs cannot see or apply for it.
     * - The job remains visible in the MO's own Manage Jobs panel for record-keeping.
     *
     * @param jobId the job to close
     * @return the updated MOJob
     * @throws IllegalArgumentException if the job is not found or is already CLOSED/WITHDRAWN
     */
    public MOJob closeJob(Long jobId) {
        for (MOJob job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                String current = job.getStatus();
                if ("CLOSED".equalsIgnoreCase(current)) {
                    throw new IllegalArgumentException("This job is already closed.");
                }
                if ("WITHDRAWN".equalsIgnoreCase(current)) {
                    throw new IllegalArgumentException(
                            "This job has been withdrawn and cannot be closed.");
                }
                job.setStatus("CLOSED");
                return dao.save(job);
            }
        }
        throw new IllegalArgumentException("Job not found.");
    }

    /**
     * Returns the parsed deadline from the formatted job description.
     * Expected line format: Deadline: YYYY-MM-DD
     */
    public LocalDate extractDeadline(MOJob job) {
        if (job == null || job.getDescription() == null || job.getDescription().isBlank()) {
            return null;
        }

        String[] lines = job.getDescription().split("\\R");
        for (String line : lines) {
            if (line.startsWith("Deadline:")) {
                String value = line.substring("Deadline:".length()).trim();
                if (value.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(value);
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Job deadline must use format YYYY-MM-DD.");
                }
            }
        }
        return null;
    }

    /**
     * Validates that the job deadline falls within the configured application cycle.
     * If no deadline is provided, the job is rejected because the requirement explicitly
     * depends on comparing the deadline with the global cycle.
     */
    public void validateDeadlineWithinCycle(MOJob job) {
        LocalDate deadline = extractDeadline(job);
        if (deadline == null) {
            throw new IllegalArgumentException("Job deadline is required and must use format YYYY-MM-DD.");
        }
        systemConfigService.validateDateWithinApplicationCycle(deadline);
    }

    /**
     * MO-009.2: Scans every OPEN/PUBLISHED job and closes those whose deadline
     * has already passed (deadline strictly before today).
     *
     * Design decisions:
     * - "Today" is determined once per call so a batch run is consistent.
     * - A job whose deadline IS today is still open (last day to apply).
     * - Already CLOSED or WITHDRAWN jobs are silently skipped → no double-processing.
     * - Jobs with a missing or unparseable deadline are skipped with a warning.
     * - Each closure is persisted immediately via dao.save() so the change
     *   survives even if the JVM is killed mid-batch.
     *
     * @return the number of jobs that were newly closed
     */
    public int autoCloseExpiredJobs() {
        LocalDate today = LocalDate.now();
        int closedCount = 0;

        for (MOJob job : dao.findAll()) {
            String status = job.getStatus();

            // Only act on jobs that are currently accepting applications
            if (!"OPEN".equalsIgnoreCase(status) && !"PUBLISHED".equalsIgnoreCase(status)) {
                continue;
            }

            LocalDate deadline;
            try {
                deadline = extractDeadline(job);
            } catch (IllegalArgumentException e) {
                System.err.println("[MOJobService] autoCloseExpiredJobs: skipping job "
                        + job.getJobId() + " — " + e.getMessage());
                continue;
            }

            if (deadline == null) {
                continue;
            }

            // Close if deadline is strictly before today (deadline day itself is still open)
            if (deadline.isBefore(today)) {
                job.setStatus("CLOSED");
                dao.save(job);
                closedCount++;
                System.out.println("[MOJobService] Auto-closed expired job #" + job.getJobId()
                        + " (" + job.getModuleCode() + " \u2013 " + job.getTitle()
                        + ") deadline was " + deadline);
            }
        }

        if (closedCount > 0) {
            System.out.println("[MOJobService] autoCloseExpiredJobs: closed " + closedCount + " job(s).");
        }
        return closedCount;
    }

    // [新增方法：供 UI 调用获取所有申请]
    public List<TAApplication> listAllApplications() {
        return appDao.findAll();
    }

    // [新增方法：供 UI 调用更新申请状态]
    public void updateApplication(TAApplication app) {
        appDao.save(app);
    }
}