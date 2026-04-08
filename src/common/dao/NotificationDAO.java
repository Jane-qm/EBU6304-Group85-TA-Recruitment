package common.dao;

import common.entity.NotificationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NotificationDAO {
    private final JsonPersistenceManager persistenceManager = new JsonPersistenceManager();
    private final AtomicLong idGenerator = new AtomicLong(7000L);

    public NotificationDAO() {
        persistenceManager.initializeBaseFiles();
        for (NotificationMessage notification : findAll()) {
            if (notification.getNotificationId() != null
                    && notification.getNotificationId() > idGenerator.get()) {
                idGenerator.set(notification.getNotificationId());
            }
        }
    }

    public List<NotificationMessage> findAll() {
        return new ArrayList<>(persistenceManager.readList(
                JsonPersistenceManager.NOTIFICATIONS_FILE,
                NotificationMessage.class
        ));
    }

    public NotificationMessage save(NotificationMessage notification) {
        List<NotificationMessage> all = findAll();
        if (notification.getNotificationId() == null) {
            notification.setNotificationId(idGenerator.incrementAndGet());
            all.add(notification);
        } else {
            boolean updated = false;
            for (int i = 0; i < all.size(); i++) {
                if (notification.getNotificationId().equals(all.get(i).getNotificationId())) {
                    all.set(i, notification);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                all.add(notification);
            }
        }
        persistenceManager.writeList(JsonPersistenceManager.NOTIFICATIONS_FILE, all);
        return notification;
    }
}
