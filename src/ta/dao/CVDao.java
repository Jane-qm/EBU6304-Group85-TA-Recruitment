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
import java.nio.file.Paths;
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
 * @version 1.0
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
            Type listType = new TypeToken<List<CVInfo>>(){}.getType();
            List<CVInfo> list;
            try {
                list = gson.fromJson(reader, listType);
            } catch (Exception ex) {
                // JSON 文件可能被写入中断导致截断，此时避免整个应用直接崩溃
                System.err.println("加载 CV 数据失败(JSON 解析): " + ex.getMessage());
                nextCvId = 1;
                // 立即修复为合法 JSON，避免每次启动都重复报错
                saveAll();
                return;
            }
            
            if (list != null) {
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
        } catch (IOException e) {
            System.err.println("保存 CV 数据失败: " + e.getMessage());
        }
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
        }
        return manager;
    }
    
    /**
     * 保存 CV 信息
     */
    public void saveCV(CVInfo cvInfo) {
        if (cvInfo == null) {
            return;
        }
        
        if (cvInfo.getCvId() == null) {
            cvInfo.setCvId(nextCvId++);
        }
        
        CVManager manager = getOrCreateCVManager(cvInfo.getTaId(), cvInfo.getTaEmail(), cvInfo.getTaName());
        
        // 检查是否已存在同名 CV
        if (manager.isCvNameExists(cvInfo.getCvName())) {
            throw new IllegalArgumentException("CV name already exists: " + cvInfo.getCvName());
        }
        
        manager.addCV(cvInfo);
        saveAll();
    }
    
    /**
     * 删除 CV
     */
    public boolean deleteCV(Long taId, Long cvId) {
        CVManager manager = cvManagers.get(taId);
        if (manager == null) {
            return false;
        }
        
        CVInfo cv = manager.getCVById(cvId);
        if (cv == null) {
            return false;
        }
        
        // 删除物理文件
        deletePhysicalFile(cv.getFilePath());
        
        boolean removed = manager.removeCV(cvId);
        if (removed) {
            saveAll();
        }
        return removed;
    }
    
    /**
     * 设置默认 CV
     */
    public boolean setDefaultCV(Long taId, Long cvId) {
        CVManager manager = cvManagers.get(taId);
        if (manager == null) {
            return false;
        }
        
        boolean success = manager.setDefaultCV(cvId);
        if (success) {
            saveAll();
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
            String safeName = cvName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
            String savedFileName = String.format("ta_%d_%s_%s.pdf", taId, safeName, timestamp);
            String filePath = CV_DIR + savedFileName;
            
            Path path = Paths.get(filePath);
            Files.write(path, data);
            return filePath;
        } catch (IOException e) {
            System.err.println("保存 CV 文件失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 读取 CV 文件
     */
    public byte[] readCVFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("读取 CV 文件失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 删除物理文件
     */
    private boolean deletePhysicalFile(String filePath) {
        if (filePath == null) {
            return true;
        }
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("删除 CV 文件失败: " + e.getMessage());
            return false;
        }
    }
}