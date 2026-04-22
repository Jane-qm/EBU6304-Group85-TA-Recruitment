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
 * @version 3.0 - 新增截止日期处理
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

    /**
     * 获取已发布的职位（TA 可见）
     * 只返回 PUBLISHED/OPEN 且未过截止日期的职位
     */
    public List<MOJob> listPublishedJobs() {
        List<MOJob> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (MOJob job : dao.findAll()) {
            String status = job.getStatus();
            if (status != null && ("OPEN".equalsIgnoreCase(status) || "PUBLISHED".equalsIgnoreCase(status))) {
                // 检查截止日期
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