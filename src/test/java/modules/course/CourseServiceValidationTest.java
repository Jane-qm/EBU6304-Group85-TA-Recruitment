package modules.course;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation-only checks for {@link CourseService#addCourse}; avoids asserting on DB state.
 */
class CourseServiceValidationTest {

    private final CourseService service = new CourseService();

    @Test
    void importCourses_nullOrEmpty_returnsZero() {
        assertEquals(0, service.importCourses(null));
        assertEquals(0, service.importCourses(Collections.emptyList()));
    }

    @Test
    void addCourse_blankModuleCode_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.addCourse("", "Title"));
        assertThrows(IllegalArgumentException.class, () -> service.addCourse("   ", "Title"));
    }

    @Test
    void addCourse_blankTitle_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.addCourse("EBU9998", ""));
        assertThrows(IllegalArgumentException.class, () -> service.addCourse("EBU9998", "  "));
    }
}
