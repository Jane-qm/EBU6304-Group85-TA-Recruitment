package modules.config;

import infrastructure.persistence.JsonPersistenceManager;

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
     * Updates the global recruitment window (stored as applicationStart / applicationEnd for compatibility).
     * Both dates must be strictly after today's local date; the window spans whole days (start 00:00, end 23:59:59).
     */
    public SystemConfig updateApplicationCycle(LocalDateTime start, LocalDateTime end, String adminEmail) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end time must not be null.");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDay = start.toLocalDate();
        LocalDate endDay = end.toLocalDate();

        if (!startDay.isAfter(today)) {
            throw new IllegalArgumentException(
                    "Recruitment start date must be after today's date (system local date).");
        }
        if (!endDay.isAfter(today)) {
            throw new IllegalArgumentException(
                    "Recruitment end date must be after today's date (system local date).");
        }
        if (endDay.isBefore(startDay)) {
            throw new IllegalArgumentException("Recruitment end date must be on or after the start date.");
        }

        LocalDateTime startLdt = startDay.atStartOfDay();
        LocalDateTime endLdt = endDay.atTime(23, 59, 59);

        SystemConfig config = new SystemConfig();
        config.setApplicationStart(startLdt);
        config.setApplicationEnd(endLdt);
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(adminEmail);

        persistenceManager.writeObject(JsonPersistenceManager.SYSTEM_CONFIG_FILE, config);
        return config;
    }

    /**
     * True if {@code now} is inside the configured recruitment window (inclusive).
     * If not configured, returns true so demos still work.
     */
    public boolean isNowWithinRecruitmentWindow(LocalDateTime now) {
        if (now == null) {
            return false;
        }
        SystemConfig config = getConfig();
        if (!config.isConfigured()) {
            return true;
        }
        return !now.isBefore(config.getApplicationStart()) && !now.isAfter(config.getApplicationEnd());
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
                    "Job deadline must fall within the configured recruitment period."
            );
        }
    }

    /**
     * Job deadline (end of that local day) must be strictly after the current time.
     */
    public void validateDeadlineAfterNow(LocalDate deadlineDate) {
        if (deadlineDate == null) {
            throw new IllegalArgumentException("Deadline date is required.");
        }
        LocalDateTime endOfDeadlineDay = LocalDateTime.of(deadlineDate, LocalTime.MAX);
        if (!endOfDeadlineDay.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Job deadline must be after the current date and time.");
        }
    }

    /**
     * MO may publish only while the current time lies in the recruitment window.
     */
    public void requireOpenRecruitmentWindowForPublish() {
        SystemConfig config = getConfig();
        if (!config.isConfigured()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(config.getApplicationStart()) || now.isAfter(config.getApplicationEnd())) {
            throw new IllegalStateException(
                    "Jobs can only be published during the recruitment period set by an administrator "
                            + "(from " + config.getApplicationStart().toLocalDate()
                            + " to " + config.getApplicationEnd().toLocalDate() + ").");
        }
    }
}