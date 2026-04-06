package common.service;

import common.dao.MOOfferDAO;
import common.entity.MOOffer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MOOfferService {
    private final MOOfferDAO dao = new MOOfferDAO();

    public MOOffer createOrUpdate(MOOffer offer) {
        if (offer.getOfferedAt() == null) {
            offer.setOfferedAt(LocalDateTime.now());
        }
        return dao.save(offer);
    }

    public List<MOOffer> listAll() {
        return dao.findAll();
    }

    public List<MOOffer> listByTaUserId(Long taUserId) {
        List<MOOffer> result = new ArrayList<>();
        for (MOOffer offer : dao.findAll()) {
            if (taUserId != null && taUserId.equals(offer.getTaUserId())) {
                result.add(offer);
            }
        }
        return result;
    }
}
