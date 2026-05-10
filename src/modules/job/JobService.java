package modules.job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import modules.config.SystemConfig;
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
     * 从 description 中解析并设置 applicationDeadline（仅当描述里仍有旧版 {@code Deadline:} 行时覆盖）。
     * 新数据以 JSON 中的 {@link Job#getApplicationDeadline()} 为准，不在此清空。
     */
    private void syncApplicationDeadline(Job job) {
        if (job == null) {
            return;
        }
        LocalDate deadlineDate = extractDeadlineSilently(job);
        if (deadlineDate != null) {
            job.setApplicationDeadline(deadlineDate.atTime(23, 59, 59));
        }
    }

    private void syncOfferResponseDeadlineFromDescription(Job job) {
        if (job == null) {
            return;
        }
        LocalDate offerDate = extractOfferResponseDateSilently(job);
        if (offerDate != null) {
            job.setOfferResponseDeadline(offerDate.atTime(23, 59, 59));
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

    private LocalDate extractOfferResponseDateSilently(Job job) {
        if (job == null || job.getDescription() == null || job.getDescription().isBlank()) {
            return null;
        }
        String[] lines = job.getDescription().split("\\R");
        for (String line : lines) {
            if (line.startsWith("Offer response due:")) {
                String value = line.substring("Offer response due:".length()).trim();
                if (value.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(value);
                } catch (DateTimeParseException ex) {
                    System.err.println("[MOJobService] Invalid offer response date for job " + job.getJobId()
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

    /**
     * At most one open/published posting per MO per module code. Drafts and closed jobs do not block.
     */
    private void assertSingleOpenJobPerMoCourse(Job job) {
        if (job == null || !isOpenOrPublished(job)) {
            return;
        }
        if (job.getMoUserId() == null || job.getModuleCode() == null || job.getModuleCode().isBlank()) {
            return;
        }
        Long excludeId = job.getJobId();
        String code = job.getModuleCode().trim();
        for (Job other : dao.findAll()) {
            if (excludeId != null && excludeId.equals(other.getJobId())) {
                continue;
            }
            if (other.getMoUserId() == null || !other.getMoUserId().equals(job.getMoUserId())) {
                continue;
            }
            if (other.getModuleCode() == null || !other.getModuleCode().trim().equalsIgnoreCase(code)) {
                continue;
            }
            if (isOpenOrPublished(other)) {
                throw new IllegalStateException(
                        "You already have an open posting for this course (" + code + "). "
                                + "Close it before publishing another job for the same module.");
            }
        }
    }

    public Job createOrUpdate(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job must not be null.");
        }
        if (job.getCreatedAt() == null) {
            job.setCreatedAt(LocalDateTime.now());
        }

        // 同步 deadline / offer 字段（与 description 一致）
        syncApplicationDeadline(job);
        syncOfferResponseDeadlineFromDescription(job);

        Job previous = job.getJobId() != null ? getJobById(job.getJobId()) : null;
        boolean wasAlreadyOpen = previous != null && isOpenOrPublished(previous);

        if (!isDraft(job)) {
            if (!wasAlreadyOpen) {
                systemConfigService.requireOpenRecruitmentWindowForPublish();
            }
            validateDeadlineWithinCycle(job);
            validateOfferResponseForPublishedJob(job);
        }
        assertSingleOpenJobPerMoCourse(job);
        return dao.save(job);
    }

    public List<Job> listAll() {
        List<Job> jobs = dao.findAll();
        // 确保每个职位的 deadline 字段与 description 同步
        for (Job job : jobs) {
            syncApplicationDeadline(job);
            syncOfferResponseDeadlineFromDescription(job);
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
            syncOfferResponseDeadlineFromDescription(job);
            
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
                syncOfferResponseDeadlineFromDescription(job);
                return job;
            }
        }
        return null;
    }

    public Job publishJob(Long jobId) {
        for (Job job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                syncApplicationDeadline(job);
                syncOfferResponseDeadlineFromDescription(job);
                systemConfigService.requireOpenRecruitmentWindowForPublish();
                validateDeadlineWithinCycle(job);
                validateOfferResponseForPublishedJob(job);
                assertSingleOpenJobPerMoCourse(job);
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
     * 解析申请截止日：优先使用已持久化的 {@link Job#getApplicationDeadline()}，否则从旧版 description 的 {@code Deadline:} 行读取。
     */
    public LocalDate extractDeadline(Job job) {
        if (job == null) {
            return null;
        }
        if (job.getApplicationDeadline() != null) {
            return job.getApplicationDeadline().toLocalDate();
        }
        if (job.getDescription() == null || job.getDescription().isBlank()) {
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
     * Required for any non-draft published/open job: absolute instant by which a TA must respond to an offer.
     */
    public void validateOfferResponseForPublishedJob(Job job) {
        if (job == null || isDraft(job)) {
            return;
        }
        LocalDateTime offerEnd = job.getOfferResponseDeadline();
        if (offerEnd == null) {
            throw new IllegalArgumentException(
                    "Offer response deadline is required when publishing. Choose the last date for TAs to accept or reject an offer.");
        }
        LocalDateTime appDl = job.getApplicationDeadline();
        if (appDl == null) {
            throw new IllegalArgumentException("Application deadline must be set before validating offer response deadline.");
        }
        if (offerEnd.toLocalDate().isBefore(appDl.toLocalDate())) {
            throw new IllegalArgumentException(
                    "Offer response date must be on or after the application deadline date (same day is allowed).");
        }
        LocalDate today = LocalDate.now();
        if (offerEnd.toLocalDate().isBefore(today)) {
            throw new IllegalArgumentException("Offer response date must be today or a future date.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(offerEnd)) {
            throw new IllegalArgumentException(
                    "That offer response deadline has already passed (end of that calendar day).");
        }
        SystemConfig cfg = systemConfigService.getConfig();
        if (cfg.isConfigured() && offerEnd.isAfter(cfg.getApplicationEnd())) {
            throw new IllegalArgumentException(
                    "Offer response deadline must be on or before the end of the administrator recruitment period ("
                            + cfg.getApplicationEnd().toLocalDate() + ").");
        }
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
            syncOfferResponseDeadlineFromDescription(job);
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