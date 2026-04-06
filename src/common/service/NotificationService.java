package common.service;

import common.dao.NotificationDAO;
import common.entity.NotificationMessage;
import common.entity.User;
import common.entity.UserRole;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/***
 * 
 * @author Yiping Zheng
 * @version 1.0
 * 
 * Generate/display the global notification pop-up window. 
 * When MO sends an offer or approves/rejects an application, it will notify TA. 
 * When TA rejects the offer, it will notify MO.
 */


public class NotificationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NotificationDAO dao = new NotificationDAO();

    public NotificationMessage notifyUser(Long recipientUserId, UserRole recipientRole,
                                          String title, String content, String type) {
        NotificationMessage notification = new NotificationMessage();
        notification.setRecipientUserId(recipientUserId);
        notification.setRecipientRole(recipientRole);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return dao.save(notification);
    }

    public List<NotificationMessage> listByUser(Long userId) {
        List<NotificationMessage> result = new ArrayList<>();
        for (NotificationMessage notification : dao.findAll()) {
            if (userId != null && userId.equals(notification.getRecipientUserId())) {
                result.add(notification);
            }
        }
        result.sort(Comparator.comparing(NotificationMessage::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return result;
    }

    public List<NotificationMessage> listUnreadByUser(Long userId) {
        List<NotificationMessage> result = new ArrayList<>();
        for (NotificationMessage notification : listByUser(userId)) {
            if (!notification.isRead()) {
                result.add(notification);
            }
        }
        return result;
    }

    public void markAllAsRead(Long userId) {
        for (NotificationMessage notification : listUnreadByUser(userId)) {
            notification.setRead(true);
            dao.save(notification);
        }
    }

    public String buildPopupMessage(User user, List<NotificationMessage> notifications) {
        if (user == null || notifications == null || notifications.isEmpty()) {
            return "No new notifications.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Hello ").append(user.getEmail()).append('\n');
        builder.append("You have ").append(notifications.size()).append(" new notification(s):\n\n");

        for (NotificationMessage notification : notifications) {
            builder.append("[").append(notification.getTitle()).append("] ");
            builder.append(notification.getContent());
            if (notification.getCreatedAt() != null) {
                builder.append("\nTime: ").append(notification.getCreatedAt().format(FORMATTER));
            }
            builder.append("\n\n");
        }
        return builder.toString().trim();
    }

    public String buildHistoryMessage(User user, List<NotificationMessage> notifications) {
        if (user == null) {
            return "No notifications.";
        }
        if (notifications == null || notifications.isEmpty()) {
            return "No notifications yet.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Notification history for ").append(user.getEmail()).append(":\n\n");

        for (NotificationMessage notification : notifications) {
            builder.append(notification.isRead() ? "[Read] " : "[Unread] ");
            builder.append("[").append(notification.getTitle()).append("] ");
            builder.append(notification.getContent());
            if (notification.getCreatedAt() != null) {
                builder.append("\nTime: ").append(notification.getCreatedAt().format(FORMATTER));
            }
            builder.append("\n\n");
        }
        return builder.toString().trim();
    }
}