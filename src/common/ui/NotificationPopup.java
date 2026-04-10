package common.ui;

import common.entity.NotificationMessage;
import common.entity.User;
import common.service.NotificationService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public final class NotificationPopup {
    private static final Color PAGE_BG = new Color(248, 250, 252);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color MUTED_TEXT = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color UNREAD_BG = new Color(239, 246, 255);
    private static final Color READ_BG = new Color(248, 250, 252);

    private NotificationPopup() {
    }

    public static void showUnreadNotifications(JFrame parent, User user, NotificationService notificationService) {
        if (user == null || notificationService == null) {
            return;
        }

        List<NotificationMessage> unreadNotifications = notificationService.listUnreadByUser(user.getUserId());
        if (unreadNotifications.isEmpty()) {
            showNotificationDialog(parent, "Notifications", "You're all caught up.", unreadNotifications, notificationService);
            return;
        }

        showNotificationDialog(parent, "New Notifications", "Unread items for " + user.getEmail(),
                unreadNotifications, notificationService);
        notificationService.markAllAsRead(user.getUserId());
    }

    public static void showAllNotifications(JFrame parent, User user, NotificationService notificationService) {
        if (user == null || notificationService == null) {
            return;
        }

        List<NotificationMessage> notifications = notificationService.listByUser(user.getUserId());
        showNotificationDialog(parent, "Notification Center", user.getEmail(), notifications, notificationService);
    }

    private static void showNotificationDialog(JFrame parent, String title, String subtitle,
                                               List<NotificationMessage> notifications,
                                               NotificationService notificationService) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(620, 520);
        dialog.setMinimumSize(new Dimension(540, 420));
        dialog.setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(PAGE_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        root.add(createHeader(title, subtitle, notifications), BorderLayout.NORTH);
        root.add(createContent(notifications, notificationService), BorderLayout.CENTER);
        root.add(createFooter(dialog), BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private static JComponent createHeader(String title, String subtitle, List<NotificationMessage> notifications) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED_TEXT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);

        JLabel countLabel = new JLabel(String.valueOf(notifications.size()), SwingConstants.CENTER);
        countLabel.setOpaque(true);
        countLabel.setBackground(PRIMARY);
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        countLabel.setBorder(new EmptyBorder(8, 16, 8, 16));

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(countLabel, BorderLayout.EAST);
        return panel;
    }

    private static JComponent createContent(List<NotificationMessage> notifications,
                                            NotificationService notificationService) {
        if (notifications == null || notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(PANEL_BG);
            emptyPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    new EmptyBorder(36, 24, 36, 24)
            ));

            JLabel emptyLabel = new JLabel("No notifications yet.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
            emptyLabel.setForeground(MUTED_TEXT);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            return emptyPanel;
        }

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(PAGE_BG);

        for (NotificationMessage notification : notifications) {
            listPanel.add(createNotificationCard(notification, notificationService));
            listPanel.add(Box.createVerticalStrut(12));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(PAGE_BG);
        return scrollPane;
    }

    private static JComponent createNotificationCard(NotificationMessage notification,
                                                     NotificationService notificationService) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(notification.isRead() ? READ_BG : UNREAD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 18, 16, 18)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);

        JLabel titleLabel = new JLabel(safeText(notification.getTitle(), "System Notification"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel badge = new JLabel(notification.isRead() ? "Read" : "New", SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setForeground(notification.isRead() ? MUTED_TEXT : PRIMARY);
        badge.setBackground(notification.isRead() ? new Color(241, 245, 249) : new Color(219, 234, 254));
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(badge, BorderLayout.EAST);

        JLabel contentLabel = new JLabel(toHtml(safeText(notification.getContent(), "")));
        contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        contentLabel.setForeground(new Color(51, 65, 85));

        JLabel timeLabel = new JLabel(notificationService.formatTimestamp(notification.getCreatedAt()));
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        timeLabel.setForeground(MUTED_TEXT);

        card.add(topRow, BorderLayout.NORTH);
        card.add(contentLabel, BorderLayout.CENTER);
        card.add(timeLabel, BorderLayout.SOUTH);
        return card;
    }

    private static JComponent createFooter(JDialog dialog) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panel.setOpaque(false);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        closeButton.setBackground(PRIMARY);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setBorder(new EmptyBorder(10, 18, 10, 18));
        closeButton.addActionListener(e -> dialog.dispose());

        panel.add(closeButton);
        return panel;
    }

    private static String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String toHtml(String content) {
        String escaped = content
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><div style='width: 500px; line-height: 1.5;'>" + escaped + "</div></html>";
    }
}
