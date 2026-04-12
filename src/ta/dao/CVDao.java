package ta.dao;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.dao.LocalDateTimeAdapter;
import ta.entity.CVInfo;
import ta.entity.CVManager;

/**
 * CV 数据访问对象
 * 负责 CV 元数据的 JSON 文件读写和 CV 文件存储
 *
 * @author System
 * @version 2.0 - 修复缓存同步问题
 */
public class CVDao {

    private static final String DATA_FILE = "data/ta_cvs.json";
    private static final String CV_DIR = "data/cvs/";

    private final Gson gson;
    private final Map<Long, CVManager> cvManagers;  // key: taId
    private long nextCvId;

    public CVDao() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.cvManagers = new ConcurrentHashMap<>();
        ensureDirectoryExists();
        loadFromFile();
    }

    /**
     * 确保目录存在
     */
    private void ensureDirectoryExists() {
        File cvDir = new File(CV_DIR);
        if (!cvDir.exists()) {
            cvDir.mkdirs();
        }
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
    }

    /**
     * 从文件加载 CV 元数据
     */
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            nextCvId = 1;
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<CVInfo>>() {}.getType();
            List<CVInfo> list;
            try {
                list = gson.fromJson(reader, listType);
            } catch (Exception ex) {
                System.err.println("加载 CV 数据失败(JSON 解析): " + ex.getMessage());
                nextCvId = 1;
                saveAll();
                return;
            }

            if (list != null) {
                // 清空现有缓存，重新加载
                cvManagers.clear();
                
                for (CVInfo cv : list) {
                    CVManager manager = cvManagers.get(cv.getTaId());
                    if (manager == null) {
                        manager = new CVManager(cv.getTaId(), cv.getTaEmail(), cv.getTaName());
                        cvManagers.put(cv.getTaId(), manager);
                    }
                    manager.addCV(cv);

                    if (cv.getCvId() != null && cv.getCvId() >= nextCvId) {
                        nextCvId = cv.getCvId() + 1;
                    }
                }
                
                System.out.println("加载 CV 数据成功，共 " + cvManagers.size() + " 个 TA 有 CV 记录");
            }
        } catch (IOException e) {
            System.err.println("加载 CV 数据失败: " + e.getMessage());
        }

        if (nextCvId == 0) {
            nextCvId = 1;
        }
    }

    /**
     * 保存所有 CV 元数据到文件
     */
    public void saveAll() {
        List<CVInfo> allCVs = new ArrayList<>();
        for (CVManager manager : cvManagers.values()) {
            allCVs.addAll(manager.getAllCVs());
        }

        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(allCVs, writer);
            System.out.println("保存 CV 数据成功，共 " + allCVs.size() + " 条记录");
        } catch (IOException e) {
            System.err.println("保存 CV 数据失败: " + e.getMessage());
        }
    }

    /**
     * Returns all CV metadata records across all TAs.
     */
    public List<CVInfo> findAll() {
        List<CVInfo> allCVs = new ArrayList<>();
        for (CVManager manager : cvManagers.values()) {
            allCVs.addAll(manager.getAllCVs());
        }
        return allCVs;
    }

    /**
     * 获取 TA 的 CV 管理器
     */
    public CVManager getCVManager(Long taId) {
        return cvManagers.get(taId);
    }

    /**
     * 获取或创建 CV 管理器
     */
    public CVManager getOrCreateCVManager(Long taId, String taEmail, String taName) {
        CVManager manager = cvManagers.get(taId);
        if (manager == null) {
            manager = new CVManager(taId, taEmail, taName);
            cvManagers.put(taId, manager);
            System.out.println("创建新的 CVManager for taId=" + taId);
        } else {
            // 更新冗余信息
            if (taEmail != null && !taEmail.equals(manager.getTaEmail())) {
                // 需要更新所有 CV 的邮箱
                for (CVInfo cv : manager.getAllCVs()) {
                    cv.setTaEmail(taEmail);
                }
            }
            if (taName != null && !taName.equals(manager.getTaName())) {
                for (CVInfo cv : manager.getAllCVs()) {
                    cv.setTaName(taName);
                }
            }
        }
        return manager;
    }

    /**
     * 保存 CV 信息（修复缓存同步问题）
     */
    public void saveCV(CVInfo cvInfo) {
        if (cvInfo == null) {
            return;
        }

        System.out.println("=== CVDao.saveCV 开始 ===");
        System.out.println("taId=" + cvInfo.getTaId() + ", cvName=" + cvInfo.getCvName() + ", cvId=" + cvInfo.getCvId());

        // 生成新的 CV ID
        if (cvInfo.getCvId() == null) {
            cvInfo.setCvId(nextCvId++);
            System.out.println("生成新 CV ID: " + cvInfo.getCvId());
        }

        // 获取或创建 CVManager（确保使用最新的 manager）
        CVManager manager = getOrCreateCVManager(cvInfo.getTaId(), cvInfo.getTaEmail(), cvInfo.getTaName());

        // 检查是否已存在同名 CV
        if (manager.isCvNameExists(cvInfo.getCvName())) {
            throw new IllegalArgumentException("CV name already exists: " + cvInfo.getCvName());
        }

        // 添加到 manager
        manager.addCV(cvInfo);
        
        // 关键修复：确保 cvManagers Map 中有最新的 manager
        cvManagers.put(cvInfo.getTaId(), manager);
        
        // 保存到文件
        saveAll();
        
        // 验证缓存是否更新成功
        CVManager verify = cvManagers.get(cvInfo.getTaId());
        System.out.println("缓存验证: TA " + cvInfo.getTaId() + " 现有 " + verify.getCVCount() + " 个 CV");
        System.out.println("=== CVDao.saveCV 结束 ===");
    }

    /**
     * 删除 CV
     */
    public boolean deleteCV(Long taId, Long cvId) {
        System.out.println("=== CVDao.deleteCV: taId=" + taId + ", cvId=" + cvId + " ===");
        
        CVManager manager = cvManagers.get(taId);
        if (manager == null) {
            System.out.println("CVManager not found for taId=" + taId);
            return false;
        }

        CVInfo cv = manager.getCVById(cvId);
        if (cv == null) {
            System.out.println("CV not found for cvId=" + cvId);
            return false;
        }

        // 删除物理文件
        deletePhysicalFile(cv.getFilePath());

        boolean removed = manager.removeCV(cvId);
        if (removed) {
            // 如果 manager 中不再有 CV，可以选择保留空 manager 或删除
            // 这里保留空 manager
            saveAll();
            System.out.println("CV 删除成功");
        }
        return removed;
    }

    /**
     * 设置默认 CV
     */
    public boolean setDefaultCV(Long taId, Long cvId) {
        System.out.println("=== CVDao.setDefaultCV: taId=" + taId + ", cvId=" + cvId + " ===");
        
        CVManager manager = cvManagers.get(taId);
        if (manager == null) {
            return false;
        }

        boolean success = manager.setDefaultCV(cvId);
        if (success) {
            saveAll();
            System.out.println("设置默认 CV 成功");
        }
        return success;
    }

    /**
     * 保存 CV 文件到磁盘
     */
    public String saveCVFile(Long taId, String cvName, byte[] data) {
        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String safeName = cvName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = "ta_" + taId + "_" + safeName + "_" + timestamp + ".pdf";
            Path filePath = Path.of(CV_DIR, fileName);
            Files.write(filePath, data);
            return filePath.toString().replace("\\", "/");
        } catch (IOException e) {
            System.err.println("保存 CV 文件失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 读取 CV 文件
     */
    public byte[] readCVFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }
        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            System.err.println("读取 CV 文件失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 删除物理文件
     */
    private void deletePhysicalFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(filePath));
            System.out.println("删除物理文件: " + filePath);
        } catch (IOException e) {
            System.err.println("删除 CV 物理文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 强制刷新缓存（从文件重新加载）
     * 用于解决缓存不同步问题
     * 
     * @param taId TA 用户 ID（如果为 null 则重新加载所有）
     */
    public void refreshFromFile(Long taId) {
        System.out.println("=== CVDao.refreshFromFile: taId=" + taId + " ===");
        
        // 保存当前文件状态，然后重新加载
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("CV 文件不存在，无需刷新");
            return;
        }
        
        // 临时存储重新加载的数据
        Map<Long, CVManager> tempManagers = new ConcurrentHashMap<>();
        long maxCvId = 1;
        
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<CVInfo>>() {}.getType();
            List<CVInfo> list = gson.fromJson(reader, listType);
            
            if (list != null) {
                for (CVInfo cv : list) {
                    CVManager manager = tempManagers.get(cv.getTaId());
                    if (manager == null) {
                        manager = new CVManager(cv.getTaId(), cv.getTaEmail(), cv.getTaName());
                        tempManagers.put(cv.getTaId(), manager);
                    }
                    manager.addCV(cv);
                    
                    if (cv.getCvId() != null && cv.getCvId() > maxCvId) {
                        maxCvId = cv.getCvId();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("refreshFromFile 失败: " + e.getMessage());
            return;
        }
        
        // 更新缓存
        if (taId == null) {
            // 刷新所有
            cvManagers.clear();
            cvManagers.putAll(tempManagers);
            nextCvId = maxCvId + 1;
            System.out.println("刷新所有 CV 缓存完成，共 " + cvManagers.size() + " 个 TA 有 CV");
        } else {
            // 只刷新指定的 TA
            CVManager refreshed = tempManagers.get(taId);
            if (refreshed != null) {
                cvManagers.put(taId, refreshed);
                System.out.println("刷新 TA " + taId + " 的 CV 缓存完成，共 " + refreshed.getCVCount() + " 个 CV");
            } else {
                // 如果文件中没有该 TA 的 CV，但缓存中有，则清除缓存
                CVManager removed = cvManagers.remove(taId);
                if (removed != null) {
                    System.out.println("清除 TA " + taId + " 的 CV 缓存（文件中不存在）");
                }
            }
        }
    }
}