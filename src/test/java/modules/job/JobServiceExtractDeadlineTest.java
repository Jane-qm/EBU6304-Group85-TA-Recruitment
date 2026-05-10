package modules.job;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link JobService#extractDeadline(Job)} — deadline resolution from persisted field or legacy description lines.
 */
class JobServiceExtractDeadlineTest {

    private final JobService jobService = new JobService();

    @Test
    void extractDeadline_nullJob_returnsNull() {
        assertNull(jobService.extractDeadline(null));
    }

    @Test
    void extractDeadline_prefersApplicationDeadlineField() {
        Job job = new Job();
        LocalDate d = LocalDate.of(2026, 8, 15);
        job.setApplicationDeadline(d.atTime(12, 0));
        job.setDescription("Deadline: 2020-01-01\nOther text");

        assertEquals(d, jobService.extractDeadline(job));
    }

    @Test
    void extractDeadline_fromDescriptionDeadlineLine() {
        Job job = new Job();
        job.setDescription("Intro line\nDeadline: 2026-12-31\nFooter");

        assertEquals(LocalDate.of(2026, 12, 31), jobService.extractDeadline(job));
    }

    @Test
    void extractDeadline_invalidFormatInDescription_throws() {
        Job job = new Job();
        job.setDescription("Deadline: not-a-date");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jobService.extractDeadline(job));
        assertTrue(ex.getMessage().contains("YYYY-MM-DD"), ex.getMessage());
    }

    @Test
    void extractDeadline_blankDescription_returnsNull() {
        Job job = new Job();
        job.setDescription("   ");

        assertNull(jobService.extractDeadline(job));
    }

    @Test
    void extractDeadline_deadlineLineEmptyValue_returnsNull() {
        Job job = new Job();
        job.setDescription("Deadline:\nNext line");

        assertNull(jobService.extractDeadline(job));
    }

    @Test
    void extractDeadline_noDeadlineLine_returnsNull() {
        Job job = new Job();
        job.setDescription("Only narrative text without marker.");

        assertNull(jobService.extractDeadline(job));
    }

    @Test
    void extractDeadline_windowsStyleNewlines_findsDeadline() {
        Job job = new Job();
        job.setDescription("Header\r\nDeadline: 2027-01-02\r\n");

        assertEquals(LocalDate.of(2027, 1, 2), jobService.extractDeadline(job));
    }
}
