package modules.notification;

import modules.user.TA;
import modules.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pure formatting helpers on {@link NotificationService} that do not touch persistence.
 */
class NotificationServiceTest {

    private final NotificationService service = new NotificationService();

    @Test
    void formatTimestamp_null_returnsUnknown() {
        assertEquals("Unknown time", service.formatTimestamp(null));
    }

    @Test
    void formatTimestamp_formatsWithExpectedPattern() {
        LocalDateTime t = LocalDateTime.of(2026, 5, 10, 14, 5);
        assertEquals("2026-05-10 14:05", service.formatTimestamp(t));
    }

    @Test
    void buildPopupMessage_nullUser_returnsNoNotifications() {
        assertEquals("No new notifications.", service.buildPopupMessage(null, sampleNotifications()));
    }

    @Test
    void buildPopupMessage_nullOrEmptyList_returnsNoNotifications() {
        User user = new TA("ta@test.local", "secret123");
        assertEquals("No new notifications.", service.buildPopupMessage(user, null));
        assertEquals("No new notifications.", service.buildPopupMessage(user, List.of()));
    }

    @Test
    void buildPopupMessage_includesEmailAndEntries() {
        User user = new TA("ta@test.local", "secret123");
        List<NotificationMessage> items = new ArrayList<>();
        NotificationMessage n = new NotificationMessage();
        n.setTitle("Hello");
        n.setContent("Body");
        n.setCreatedAt(LocalDateTime.of(2026, 5, 10, 9, 0));
        items.add(n);

        String msg = service.buildPopupMessage(user, items);
        assertTrue(msg.contains("ta@test.local"));
        assertTrue(msg.contains("Hello"));
        assertTrue(msg.contains("Body"));
        assertTrue(msg.contains("Time: 2026-05-10 09:00"));
    }

    @Test
    void buildHistoryMessage_nullUser_returnsPlaceholder() {
        assertEquals("No notifications.", service.buildHistoryMessage(null, sampleNotifications()));
    }

    @Test
    void buildHistoryMessage_empty_returnsYetPlaceholder() {
        User user = new TA("mo@test.local", "secret123");
        assertEquals("No notifications yet.", service.buildHistoryMessage(user, List.of()));
    }

    @Test
    void buildHistoryMessage_marksReadUnread_andIncludesTimes() {
        User user = new TA("u@test.local", "secret123");

        NotificationMessage read = new NotificationMessage();
        read.setTitle("T1");
        read.setContent("C1");
        read.setRead(true);
        read.setCreatedAt(LocalDateTime.of(2026, 1, 2, 3, 4));

        NotificationMessage unread = new NotificationMessage();
        unread.setTitle("T2");
        unread.setContent("C2");
        unread.setRead(false);

        String msg = service.buildHistoryMessage(user, List.of(read, unread));
        assertTrue(msg.contains("Notification history for u@test.local"));
        assertTrue(msg.contains("[Read]"));
        assertTrue(msg.contains("[Unread]"));
        assertTrue(msg.contains("Time: 2026-01-02 03:04"));
    }

    private static List<NotificationMessage> sampleNotifications() {
        NotificationMessage n = new NotificationMessage();
        n.setTitle("x");
        n.setContent("y");
        return List.of(n);
    }
}
