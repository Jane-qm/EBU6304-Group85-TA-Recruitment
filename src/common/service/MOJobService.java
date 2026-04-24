package common.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import common.dao.MOJobDAO;
import common.entity.MOJob;
import ta.dao.TAApplicationDAO;
import ta.entity.TAApplication;
import ta.service.TAApplicationService;

/**
 * MO job service.
 *
 * @version 3.2 - 修复 autoCloseExpiredJobs 中的异常处理，避免解析失败导致启动中断
 */
public class MOJobService {
    private final MOJobDAO dao = new MOJobDAO();
    private final TAApplicationDAO appDao = new TAApplicationDAO();
    private final SystemConfigService systemConfigService = new SystemConfigService();
    private TAApplicationService applicationService;

    private TAApplicationService getApplicationService() {
        if (applicationService == null) {
            applicationService = new TAApplicationService();
        }
        return applicationService;
    }

    /**
     * 从 description 中解析并设置 applicationDeadline 字段
     */
    private void syncApplicationDeadline(MOJob job) {
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
    private LocalDate extractDeadlineSilently(MOJob job) {
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

    public MOJob createOrUpdate(MOJob job) {
        if (job == null) {
            throw new IllegalArgumentException("Job must not be null.");
        }
        if (job.getCreatedAt() == null) {
            job.setCreatedAt(LocalDateTime.now());
        }

        // 同步 deadline 字段
        syncApplicationDeadline(job);
        
        // 验证 deadline 格式（会抛出异常，阻止保存格式错误的职位）
        validateDeadlineWithinCycle(job);
        return dao.save(job);
    }

    public List<MOJob> listAll() {
        List<MOJob> jobs = dao.findAll();
        // 确保每个职位的 deadline 字段与 description 同步
        for (MOJob job : jobs) {
            syncApplicationDeadline(job);
        }
        return jobs;
    }

    /**
     * 获取已发布的职位（TA 可见）
     * 只返回 PUBLISHED/OPEN 且未过截止日期的职位
     */
    public List<MOJob> listPublishedJobs() {
        List<MOJob> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (MOJob job : dao.findAll()) {
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

    public MOJob getPublishedJob(Long jobId) {
        for (MOJob job : listPublishedJobs()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                return job;
            }
        }
        return null;
    }
    
    public MOJob getJobById(Long jobId) {
        for (MOJob job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                syncApplicationDeadline(job);
                return job;
            }
        }
        return null;
    }

    public MOJob publishJob(Long jobId) {
        for (MOJob job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                syncApplicationDeadline(job);
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
    public MOJob closeJob(Long jobId) {
        for (MOJob job : dao.findAll()) {
            if (jobId != null && jobId.equals(job.getJobId())) {
                String current = job.getStatus();
                if ("CLOSED".equalsIgnoreCase(current)) {
                    throw new IllegalArgumentException("This job is already closed.");
                }
                job.setStatus("CLOSED");
                MOJob saved = dao.save(job);
                
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
                    throw new IllegalArgumentException("Job deadline must use format YYYY-MM-DD. Got: '" + value + "'");
                }
            }
        }
        return null;
    }

    /**
     * 验证截止日期在申请周期内
     */
    public void validateDeadlineWithinCycle(MOJob job) {
        LocalDate deadline = extractDeadline(job);
        if (deadline == null) {
            throw new IllegalArgumentException("Job deadline is required and must use format YYYY-MM-DD.");
        }
        systemConfigService.validateDateWithinApplicationCycle(deadline);
    }

    /**
     * 自动关闭过期职位
     */
    public int autoCloseExpiredJobs() {
        LocalDate today = LocalDate.now();
        int closedCount = 0;

        for (MOJob job : dao.findAll()) {
            String status = job.getStatus();

            if (!"OPEN".equalsIgnoreCase(status) && !"PUBLISHED".equalsIgnoreCase(status)) {
                continue;
            }

            LocalDate deadline = extractDeadlineSilently(job);
            if (deadline == null) {
                // 无法解析 deadline 的职位，跳过自动关闭（但输出警告）
                System.err.println("[MOJobService] autoCloseExpiredJobs: skipping job " + job.getJobId() 
                        + " (" + job.getModuleCode() + ") — deadline missing or invalid format");
                continue;
            }

            if (deadline.isBefore(today)) {
                job.setStatus("CLOSED");
                dao.save(job);
                
                // 处理关联的未完成申请
                getApplicationService().processExpiredApplicationsForJob(job.getJobId());
                
                closedCount++;
                System.out.println("[MOJobService] Auto-closed expired job #" + job.getJobId()
                        + " (" + job.getModuleCode() + " – " + job.getTitle()
                        + ") deadline was " + deadline);
            }
        }

        if (closedCount > 0) {
            System.out.println("[MOJobService] autoCloseExpiredJobs: closed " + closedCount + " job(s).");
        }
        return closedCount;
    }

    public List<TAApplication> listAllApplications() {
        return appDao.findAll();
    }

    public void updateApplication(TAApplication app) {
        appDao.save(app);
    }
}