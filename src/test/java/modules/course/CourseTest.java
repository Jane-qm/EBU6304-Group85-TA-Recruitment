package modules.course;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void constructor_setsFields() {
        Course c = new Course("EBU6304", "Software Engineering");
        assertEquals("EBU6304", c.getModuleCode());
        assertEquals("Software Engineering", c.getTitle());
    }

    @Test
    void toString_joinsCodeAndTitle() {
        Course c = new Course("ABC123", "Title");
        assertEquals("ABC123 - Title", c.toString());
    }
}
