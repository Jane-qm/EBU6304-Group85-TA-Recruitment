package common.dao;

import common.entity.MOOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MOOfferDAO {
    private final JsonPersistenceManager persistenceManager = new JsonPersistenceManager();
    private final AtomicLong idGenerator = new AtomicLong(5000L);

    public MOOfferDAO() {
        persistenceManager.initializeBaseFiles();
        for (MOOffer offer : findAll()) {
            if (offer.getOfferId() != null && offer.getOfferId() > idGenerator.get()) {
                idGenerator.set(offer.getOfferId());
            }
        }
    }

    public List<MOOffer> findAll() {
        return new ArrayList<>(persistenceManager.readList(JsonPersistenceManager.MO_OFFERS_FILE, MOOffer.class));
    }

    public MOOffer save(MOOffer offer) {
        List<MOOffer> all = findAll();
        if (offer.getOfferId() == null) {
            offer.setOfferId(idGenerator.incrementAndGet());
            all.add(offer);
        } else {
            boolean updated = false;
            for (int i = 0; i < all.size(); i++) {
                if (offer.getOfferId().equals(all.get(i).getOfferId())) {
                    all.set(i, offer);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                all.add(offer);
            }
        }
        persistenceManager.writeList(JsonPersistenceManager.MO_OFFERS_FILE, all);
        return offer;
    }
}
