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
     * Start and end dates must be today or later (inclusive); the window spans whole days (start 00:00, end 23:59:59).
     */
    public SystemConfig updateApplicationCycle(LocalDateTime start, LocalDateTime end, String adminEmail) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end time must not be null.");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDay = start.toLocalDate();
        LocalDate endDay = end.toLocalDate();

        if (startDay.isBefore(today)) {
            throw new IllegalArgumentException(
                    "Recruitment start date must be today or a future date (system local date).");
        }
        if (endDay.isBefore(today)) {
            throw new IllegalArgumentException(
                    "Recruitment end date must be today or a future date (system local date).");
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
     * If not configured, returns false (no active recruitment window).
     */
    public boolean isNowWithinRecruitmentWindow(LocalDateTime now) {
        if (now == null) {
            return false;
        }
        SystemConfig config = getConfig();
        if (!config.isConfigured()) {
            return false;
        }
        return !now.isBefore(config.getApplicationStart()) && !now.isAfter(config.getApplicationEnd());
    }

    /**
     * Checks whether a given datetime is within the configured application cycle.
     * If the cycle is not configured, returns false.
     */
    public boolean isWithinApplicationCycle(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }

        SystemConfig config = getConfig();
        if (!config.isConfigured()) {
            return false;
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
     * Job deadline must be a calendar day of today or later; if it is today, the day must not already have ended.
     */
    public void validateDeadlineAfterNow(LocalDate deadlineDate) {
        if (deadlineDate == null) {
            throw new IllegalArgumentException("Deadline date is required.");
        }
        LocalDate today = LocalDate.now();
        if (deadlineDate.isBefore(today)) {
            throw new IllegalArgumentException(
                    "Job deadline must be today or a future date.");
        }
        LocalDateTime endOfDeadlineDay = LocalDateTime.of(deadlineDate, LocalTime.MAX);
        if (LocalDateTime.now().isAfter(endOfDeadlineDay)) {
            throw new IllegalArgumentException(
                    "That deadline date has already ended (end of that calendar day).");
        }
    }

    /**
     * MO may publish only after an administrator has configured a recruitment window
     * and while the current time lies inside that window. Drafts are not restricted here.
     */
    public void requireOpenRecruitmentWindowForPublish() {
        SystemConfig config = getConfig();
        if (!config.isConfigured()) {
            throw new IllegalStateException(
                    "Recruitment period is not configured yet. Ask an administrator to set it in Admin → "
                            + "Recruitment Period. You can still save the job as a draft.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(config.getApplicationStart()) || now.isAfter(config.getApplicationEnd())) {
            throw new IllegalStateException(
                    "Jobs can only be published during the recruitment period set by an administrator "
                            + "(from " + config.getApplicationStart().toLocalDate()
                            + " to " + config.getApplicationEnd().toLocalDate() + "). "
                            + "You can still save changes as a draft.");
        }
    }
}