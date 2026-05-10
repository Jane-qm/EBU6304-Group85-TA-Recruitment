package modules.job;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JobTest {

    @Test
    void draft_isNotApplicable() {
        Job job = new Job();
        job.setStatus("DRAFT");
        job.setApplicationDeadline(LocalDateTime.now().plusDays(30));
        assertFalse(job.isApplicable());
    }

    @Test
    void published_beforeDeadline_isApplicable() {
        Job job = new Job();
        job.setStatus("PUBLISHED");
        job.setApplicationDeadline(LocalDateTime.now().plusDays(1));
        assertTrue(job.isApplicable());
    }

    @Test
    void open_beforeDeadline_isApplicable() {
        Job job = new Job();
        job.setStatus("OPEN");
        job.setApplicationDeadline(LocalDateTime.now().plusHours(2));
        assertTrue(job.isApplicable());
    }

    @Test
    void published_afterDeadline_isNotApplicable_andExpired() {
        Job job = new Job();
        job.setStatus("PUBLISHED");
        job.setApplicationDeadline(LocalDateTime.now().minusMinutes(1));
        assertFalse(job.isApplicable());
        assertTrue(job.isExpired());
    }

    @Test
    void unknownStatus_isNotApplicable() {
        Job job = new Job();
        job.setStatus("CLOSED");
        job.setApplicationDeadline(LocalDateTime.now().plusDays(1));
        assertFalse(job.isApplicable());
    }

    @Test
    void headcount_negative_becomesZero() {
        Job job = new Job();
        job.setHeadcount(-3);
        assertEquals(0, job.getHeadcount());
    }

    @Test
    void status_case_insensitive_forDraft() {
        Job job = new Job();
        job.setStatus("draft");
        job.setApplicationDeadline(LocalDateTime.now().plusDays(1));
        assertFalse(job.isApplicable());
    }
}
