package common.service;

import common.dao.MOJobDAO;
import ta.dao.TAApplicationDAO; // [新增引用]
import common.entity.MOJob;
import ta.entity.TAApplication; // [新增引用]

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MOJobService {
    private final MOJobDAO dao = new MOJobDAO();
    private final TAApplicationDAO appDao = new TAApplicationDAO(); // [新增：直接持有申请DAO]
    public MOJob createOrUpdate(MOJob job) {
        if (job.getCreatedAt() == null) {
            job.setCreatedAt(LocalDateTime.now());
        }
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
                job.setStatus("PUBLISHED");
                return dao.save(job);
            }
        }
        throw new IllegalArgumentException("Job not found.");
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
