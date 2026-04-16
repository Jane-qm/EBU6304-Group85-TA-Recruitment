package common.service;

import common.dao.MOJobDAO;
import common.dao.MOOfferDAO;
import common.domain.NotificationKind;
import common.entity.MOJob;
import common.entity.MOOffer;
import common.entity.User;
import common.entity.UserRole;
import ta.dao.TAProfileDAO;
import ta.entity.TAProfile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MOOfferService {
    private final MOOfferDAO dao = new MOOfferDAO();
    private final NotificationService notificationService = new NotificationService();
    private final UserService userService = new UserService();
    private final TAProfileDAO taProfileDAO = new TAProfileDAO();
    private final MOJobDAO jobDAO = new MOJobDAO();

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
                        buildRejectTitle(savedOffer),
                        buildRejectContent(savedOffer),
                        NotificationKind.OFFER_REJECTED
                );
                return savedOffer;
            }
        }
        throw new IllegalArgumentException("Offer not found.");
    }

    /**
     * Resolves the TA's display name from their profile, falling back to their
     * email address if the profile has not been filled in, and ultimately to the
     * user-ID string if neither is available.
     */
    private String resolveTaName(Long taUserId) {
        if (taUserId == null) {
            return "Unknown TA";
        }
        TAProfile profile = taProfileDAO.findByTaId(taUserId);
        if (profile == null) {
            User taUser = userService.findById(taUserId);
            if (taUser != null) {
                profile = taProfileDAO.findByEmail(taUser.getEmail());
            }
        }
        if (profile != null) {
            String forename = profile.getForename();
            String surname  = profile.getSurname();
            if (forename != null && !forename.isBlank()
                    && surname != null && !surname.isBlank()) {
                return forename + " " + surname;
            }
            if (profile.getChineseName() != null && !profile.getChineseName().isBlank()) {
                return profile.getChineseName();
            }
        }
        User taUser = userService.findById(taUserId);
        return (taUser != null) ? taUser.getEmail() : "TA #" + taUserId;
    }

    /**
     * Returns a human-readable course label: "MODULE_CODE – Job Title" when a
     * matching job is found, otherwise just the module code.
     */
    private String resolveCourseLabel(MOOffer offer) {
        if (offer.getModuleCode() == null) {
            return "unknown module";
        }
        for (MOJob job : jobDAO.findAll()) {
            if (offer.getModuleCode().equals(job.getModuleCode())
                    && offer.getMoUserId() != null
                    && offer.getMoUserId().equals(job.getMoUserId())) {
                if (job.getTitle() != null && !job.getTitle().isBlank()) {
                    return offer.getModuleCode() + " \u2013 " + job.getTitle();
                }
                break;
            }
        }
        return offer.getModuleCode();
    }

    private String buildRejectTitle(MOOffer offer) {
        return "Offer Declined \u2014 " + resolveCourseLabel(offer);
    }

    private String buildRejectContent(MOOffer offer) {
        String taName     = resolveTaName(offer.getTaUserId());
        String course     = resolveCourseLabel(offer);
        String rejectedAt = offer.getRespondedAt() != null
                ? offer.getRespondedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return taName + " has declined your offer for " + course + ".\n"
                + "Declined at: " + rejectedAt + "\n"
                + "You may now select a replacement candidate from the waitlist.";
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
