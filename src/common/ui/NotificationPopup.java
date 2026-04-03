package common.ui;

import common.entity.NotificationMessage;
import common.entity.User;
import common.service.NotificationService;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.List;

public final class NotificationPopup {
    private NotificationPopup() {
    }

    public static void showUnreadNotifications(JFrame parent, User user, NotificationService notificationService) {
        if (user == null || notificationService == null) {
            return;
        }

        List<NotificationMessage> unreadNotifications = notificationService.listUnreadByUser(user.getUserId());
        if (unreadNotifications.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "No unread notifications.",
                    "System Notifications",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
                parent,
                notificationService.buildPopupMessage(user, unreadNotifications),
                "System Notifications",
                JOptionPane.INFORMATION_MESSAGE
        );
        notificationService.markAllAsRead(user.getUserId());
    }

    public static void showAllNotifications(JFrame parent, User user, NotificationService notificationService) {
        if (user == null || notificationService == null) {
            return;
        }

        List<NotificationMessage> notifications = notificationService.listByUser(user.getUserId());
        JOptionPane.showMessageDialog(
                parent,
                notificationService.buildHistoryMessage(user, notifications),
                "System Notifications",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
