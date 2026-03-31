package common.dao;

import common.entity.TAProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TAProfileDAO {
    private final JsonPersistenceManager persistenceManager = new JsonPersistenceManager();
    private final AtomicLong idGenerator = new AtomicLong(1000L);

    public TAProfileDAO() {
        persistenceManager.initializeBaseFiles();
        for (TAProfile profile : findAll()) {
            if (profile.getProfileId() != null && profile.getProfileId() > idGenerator.get()) {
                idGenerator.set(profile.getProfileId());
            }
        }
    }

    public List<TAProfile> findAll() {
        return new ArrayList<>(persistenceManager.readList(JsonPersistenceManager.TA_PROFILES_FILE, TAProfile.class));
    }

    public TAProfile save(TAProfile profile) {
        List<TAProfile> all = findAll();
        if (profile.getProfileId() == null) {
            profile.setProfileId(idGenerator.incrementAndGet());
            all.add(profile);
        } else {
            boolean updated = false;
            for (int i = 0; i < all.size(); i++) {
                if (profile.getProfileId().equals(all.get(i).getProfileId())) {
                    all.set(i, profile);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                all.add(profile);
            }
        }
        persistenceManager.writeList(JsonPersistenceManager.TA_PROFILES_FILE, all);
        return profile;
    }
}
