package common.service;

import common.dao.JsonPersistenceManager;
import common.entity.SystemConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service class for global system configuration.
 *
 * Contributor: Jiaze Wang
 *
 * @version 2.0
 * @contributor Jiaze Wang
 * @update
 * - Added date-based validation helpers for MO job deadline enforcement
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

    /**
     * Checks whether a date-only deadline falls within the configured application cycle.
     * The end of the deadline day is used so that the whole date remains valid.
     */
    public boolean isDateWithinApplicationCycle(LocalDate date) {
        if (date == null) {
            return false;
        }
        LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MAX);
        return isWithinApplicationCycle(dateTime);
    }

    /**
     * Validates a date-only deadline against the configured application cycle.
     */
    public void validateDateWithinApplicationCycle(LocalDate date) {
        if (!isDateWithinApplicationCycle(date)) {
            throw new IllegalArgumentException(
                    "Job deadline must fall within the configured application cycle."
            );
        }
    }
}