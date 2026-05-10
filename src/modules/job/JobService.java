package modules.job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import modules.config.SystemConfigService;
import modules.application.ApplicationDAO;
import modules.application.Application;
import modules.application.ApplicationService;

/**
 * MO job service.
 *
 * @version 3.2 - 修复 autoCloseExpiredJobs 中的异常处理，避免解析失败导致启动中断
 */
public class JobService {
    private final JobDAO dao = new JobDAO();
    private final ApplicationDAO appDao = new ApplicationDAO();
    private final SystemConfigService systemConfigService = new SystemConfigService();
    private ApplicationService applicationService;

    private ApplicationService getApplicationService() {
        if (applicationService == null) {
            applicationService = new ApplicationService();
        }
        return applicationService;
    }

    /**
     * 从 description 中解析并设置 applicationDeadline 字段
     */
    private void syncApplicationDeadline(Job job) {
        if (job == null) return;
        
        LocalDate deadlineDate = extractDeadlineSilently(job);
        if (deadlineDate != null) {
            job.setApplicationDeadline(deadlineDate.atTime(23, 59, 59));
        } else {
            job.setApplicationDeadline(null);
        }
    }
    
    /**
     * 静默解析 deadline，不抛出异常（用于自动关闭任务）
     */
    private LocalDate extractDeadlineSilently(Job job) {
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
                    // 静默返回 null，不抛出异常
                    System.err.println("[MOJobService] Invalid deadline format for job " + job.getJobId() 
                            + ": '" + value + "'. Expected format: YYYY-MM-DD");
                    return null;
                }
            }
        }
        return null;
    }

    private static boolean isDraft(Job job) {
        return job != null && job.getStatus() != null && "DRAFT".equalsIgnoreCase(job.getStatus());
    }

    private static boolean isOpenOrPublished(Job job) {
        if (job == null || job.getStatus() == null) {
            return false;
        }
        String s = job.getStatus();
        return "OPEN".equalsIgnoreCase(s) || "PUBLISHED".equalsIgnoreCase(s);
    }

    public Job createOrUpdate(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job must not be null.");
        }
        if (job.getCreatedAt() == null) {
            job.setCreatedAt(LocalDateTime.now());
        }

        // 同步 deadline 字段
        syncApplicationDeadline(job);

        Job previous = job.getJobId() != null ? getJobById(job.getJobId()) : null;
        boolean wasAlreadyOpen = previous != null && isOpenOrPublished(previous);

        if (!isDraft(job)) {
            if (!wasAlreadyOpen) {
                systemConfigService.requireOpenRecruitmentWindowForPublish();
            }
            validateDeadlineWithinCycle(job);
        }
        return dao.save(job);
    }

    public List<Job> listAll() {
        List<Job> jobs = dao.findAll();
        // 确保每个职位的 deadline 字段与 description 同步
        for (Job job : jobs) {
            syncApplicationDeadline(job);
        }
        return jobs;
    }

    /**
     * 获取已发布的职位（TA 可见）
     * 只返回 PUBLISHED/OPEN 且未过截止日期的职位
     */
    public List<Job> listPublishedJobs() {
        List<Job> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Job job : dao.findAll()) {
            // 确保 deadline 字段同步
            syncApplicationDeadline(job);
            
            String status = job.getStatus();
            if (status != null && ("OPEN".equalsIgnoreCase(status) || "PUBLISHED".equalsIgnoreCase(status))) {
                // 检查截止日期（如果 deadline 解析失败，则视为过期）
                if (job.getApplicationDeadline() == null || !now.isAfter(job.getApplicationDeadline())) {
                    result.add(job);
                }
            }
        }
        return result;
    }

    public Job getPublishedJob(Long jobId) {
        for (Job job : listPublishedJobs()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                return job;
            }
        }
        return null;
    }
    
    public Job getJobById(Long jobId) {
        for (Job job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                syncApplicationDeadline(job);
                return job;
            }
        }
        return null;
    }

    public Job publishJob(Long jobId) {
        for (Job job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                syncApplicationDeadline(job);
                systemConfigService.requireOpenRecruitmentWindowForPublish();
                validateDeadlineWithinCycle(job);
                job.setStatus("PUBLISHED");
                return dao.save(job);
            }
        }
        throw new IllegalArgumentException("Job not found.");
    }

    /**
     * 关闭职位
     */
    public Job closeJob(Long jobId) {
        for (Job job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                String current = job.getStatus();
                if ("CLOSED".equalsIgnoreCase(current)) {
                    throw new IllegalArgumentException("This job is already closed.");
                }
                job.setStatus("CLOSED");
                Job saved = dao.save(job);
                
                // 处理关联的未完成申请
                getApplicationService().processExpiredApplicationsForJob(jobId);
                
                return saved;
            }
        }
        throw new IllegalArgumentException("Job not found.");
    }

    /**
     * 解析截止日期（从描述中）
     * 抛出异常用于保存时的验证
     */
    public LocalDate extractDeadline(Job job) {
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
                    throw new IllegalArgumentException("Job deadline must use format YYYY-MM-DD. Got: '" + value + "'");
                }
            }
        }
        return null;
    }

    /**
     * 验证截止日期在申请周期内
     */
    public void validateDeadlineWithinCycle(Job job) {
        LocalDate deadline = extractDeadline(job);
        if (deadline == null) {
            throw new IllegalArgumentException("Job deadline is required and must use format YYYY-MM-DD.");
        }
        systemConfigService.validateDateWithinApplicationCycle(deadline);
        systemConfigService.validateDeadlineAfterNow(deadline);
    }

    /**
     * 自动关闭过期职位
     */
    public int autoCloseExpiredJobs() {
        LocalDateTime now = LocalDateTime.now();
        int closedCount = 0;

        for (Job job : dao.findAll()) {
            String status = job.getStatus();

            if (!"OPEN".equalsIgnoreCase(status) && !"PUBLISHED".equalsIgnoreCase(status)) {
                continue;
            }

            syncApplicationDeadline(job);
            LocalDateTime deadlineInstant = job.getApplicationDeadline();
            if (deadlineInstant == null) {
                System.err.println("[MOJobService] autoCloseExpiredJobs: skipping job " + job.getJobId()
                        + " (" + job.getModuleCode() + ") — deadline missing or invalid format");
                continue;
            }

            if (now.isAfter(deadlineInstant)) {
                job.setStatus("CLOSED");
                dao.save(job);

                // 处理关联的未完成申请
                getApplicationService().processExpiredApplicationsForJob(job.getJobId());

                closedCount++;
            }
        }

        return closedCount;
    }

    public List<Application> listAllApplications() {
        return appDao.findAll();
    }

    public void updateApplication(Application app) {
        appDao.save(app);
    }
}