package common.service;

import common.dao.JsonPersistenceManager;
import common.entity.SystemConfig;

import java.time.LocalDateTime;

/**
 * Service class for global system configuration.
 *
 * Contributor: Jiaze Wang
 */
public class SystemConfigService {
    private final JsonPersistenceManager persistenceManager = new JsonPersistenceManager();

    public SystemConfigService() {
        persistenceManager.initializeBaseFiles();
    }

    /**
     * Loads the current system configuration from JSON.
     */
    public SystemConfig getConfig() {
        SystemConfig config = persistenceManager.readObject(
                JsonPersistenceManager.SYSTEM_CONFIG_FILE,
                SystemConfig.class
        );
        return config == null ? new SystemConfig() : config;
    }

    /**
     * Updates the global application cycle.
     */
    public SystemConfig updateApplicationCycle(LocalDateTime start, LocalDateTime end, String adminEmail) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end time must not be null.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        SystemConfig config = new SystemConfig();
        config.setApplicationStart(start);
        config.setApplicationEnd(end);
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(adminEmail);

        persistenceManager.writeObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, config);
        return config;
    }

    /**
     * Checks whether a given datetime is within the configured application cycle.
     * If the cycle is not configured yet, the method returns true to avoid blocking demo flow.
     */
    public boolean isWithinApplicationCycle(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }

        SystemConfig config = getConfig();
        if (!config.isConfigured()) {
            return true;
        }

        return !dateTime.isBefore(config.getApplicationStart())
                && !dateTime.isAfter(config.getApplicationEnd());
    }
}