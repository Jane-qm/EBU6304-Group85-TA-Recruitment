package common.service;

import common.dao.MOOfferDAO;
import common.domain.NotificationKind;
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
    public MOOffer sendOffer(MOOffer offer) {
        offer.setStatus("SENT");
        MOOffer savedOffer = createOrUpdate(offer);
        notificationService.notifyUser(
                savedOffer.getTaUserId(),
                UserRole.TA,
                "New Offer",
                "You received an offer for module " + savedOffer.getModuleCode()
                        + " (" + savedOffer.getOfferedHours() + " hours).",
                "OFFER_SENT"
        );
        return savedOffer;
    }

    public MOOffer rejectOffer(Long offerId) {
        for (MOOffer offer : dao.findAll()) {
            if (offerId != null && offerId.equals(offer.getOfferId())) {
                offer.setStatus("REJECTED");
                offer.setRespondedAt(LocalDateTime.now());
                MOOffer savedOffer = dao.save(offer);
                notificationService.notifyUser(
                        savedOffer.getMoUserId(),
                        UserRole.MO,
                        "Offer Rejected",
                        "TA user #" + savedOffer.getTaUserId() + " rejected the offer for "
                                + savedOffer.getModuleCode() + ".",
                        "OFFER_REJECTED"
                );
                return savedOffer;
            }
        }
        throw new IllegalArgumentException("Offer not found.");
    }

    public MOOffer acceptOffer(Long offerId) {
        for (MOOffer offer : dao.findAll()) {
            if (offerId != null && offerId.equals(offer.getOfferId())) {
                offer.setStatus("ACCEPTED");
                offer.setRespondedAt(LocalDateTime.now());
                MOOffer saved = dao.save(offer);
                notificationService.notifyUser(
                        saved.getMoUserId(),
                        UserRole.MO,
                        "Offer accepted",
                        "TA user #" + saved.getTaUserId() + " accepted the offer for "
                                + saved.getModuleCode() + ".",
                        NotificationKind.OFFER_RESPONSE
                );
                return saved;
            }
        }
        throw new IllegalArgumentException("Offer not found.");
    }

}
