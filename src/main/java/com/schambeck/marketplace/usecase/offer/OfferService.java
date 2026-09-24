package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import com.schambeck.marketplace.exception.OfferValidationException;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.product.ProductRepository;
import com.schambeck.marketplace.usecase.inventory.InventoryBalance;
import com.schambeck.marketplace.usecase.inventory.InventoryBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfferService {
    private final OfferRepository repository;
    private final OfferStatusHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;

    @Transactional
    public Offer create(OfferWeb web, String actor, boolean admin) {
        validateFields(web);
        Product product = productRepository.findByIdOrThrow(web.getProductId());
        authorizeProduct(product, actor, admin);
        Offer offer = Offer.builder()
                .product(product)
                .contextGame(web.getGame().trim())
                .contextPlatform(web.getPlatform().trim())
                .contextRegion(trimToNull(web.getRegion()))
                .contextVariant(trimToNull(web.getVariant()))
                .price(web.getPrice())
                .deliveryMethod(web.getDeliveryMethod().trim())
                .availabilityStatus(availabilityFor(product))
                .status(OfferStatus.DRAFT)
                .version(0)
                .build();
        Offer saved = repository.save(offer);
        recordHistory(saved, null, OfferStatus.DRAFT, "created", admin ? "admin" : "seller", actor);
        return saved;
    }

    @Transactional
    public Offer updateDraft(Integer id, OfferUpdateRequest request, String actor, boolean admin) {
        if (request == null || request.getProductId() == null) {
            throw new OfferValidationException("productId is required.");
        }
        requireAudit(request.getReason(), request.getSource());
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        authorizeOffer(offer, actor, admin);
        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw new OfferValidationException("Only a DRAFT offer can be changed.");
        }
        validateFields(request.getProductId(), request.getGame(), request.getPlatform(), request.getPrice(), request.getDeliveryMethod());
        Product product = productRepository.findByIdOrThrow(request.getProductId());
        authorizeProduct(product, actor, admin);
        offer.setProduct(product);
        offer.setContextGame(request.getGame().trim());
        offer.setContextPlatform(request.getPlatform().trim());
        offer.setContextRegion(trimToNull(request.getRegion()));
        offer.setContextVariant(trimToNull(request.getVariant()));
        offer.setPrice(request.getPrice());
        offer.setDeliveryMethod(request.getDeliveryMethod().trim());
        offer.setAvailabilityStatus(availabilityFor(product));
        recordHistory(offer, OfferStatus.DRAFT, OfferStatus.DRAFT, request.getReason(), request.getSource(), actor);
        return repository.save(offer);
    }

    public Page<Offer> searchPublished(Pageable pageable, String search) {
        return repository.findAllPublishedAndAvailable(normalize(search), pageable);
    }

    public Page<Offer> searchAdmin(Pageable pageable, OfferStatus status, String search) {
        return repository.searchAdmin(status, normalize(search), pageable);
    }

    public Page<Offer> searchOwned(String username, Pageable pageable, OfferStatus status, String search) {
        return repository.searchOwned(username, status, normalize(search), pageable);
    }

    public Offer findPublishedById(Integer id) {
        Offer offer = repository.findByIdOrThrow(id);
        if (offer.getStatus() != OfferStatus.PUBLISHED || !isAvailable(offer.getProduct())) {
            throw new EntityNotFoundException(Offer.class, id);
        }
        return offer;
    }

    public Offer findById(Integer id) {
        return repository.findByIdOrThrow(id);
    }

    public Offer findAdminById(Integer id) {
        return repository.findByIdOrThrow(id);
    }

    public Offer findOwnedById(String username, Integer id) {
        return repository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new EntityNotFoundException(Offer.class, id));
    }

    public List<OfferStatusHistory> history(Integer id, String actor, boolean admin) {
        Offer offer = repository.findByIdOrThrow(id);
        authorizeOffer(offer, actor, admin);
        return historyRepository.findAllByOfferIdOrderByIdAsc(id);
    }

    @Transactional
    public Offer publish(Integer id, OfferActionRequest request, String actor, boolean admin) {
        return transitionTo(id, OfferStatus.PUBLISHED, request, actor, admin, true);
    }

    @Transactional
    public Offer pause(Integer id, OfferActionRequest request, String actor, boolean admin) {
        return transitionTo(id, OfferStatus.PAUSED, request, actor, admin, false);
    }

    @Transactional
    public Offer reactivate(Integer id, OfferActionRequest request, String actor, boolean admin) {
        return transitionTo(id, OfferStatus.PUBLISHED, request, actor, admin, true);
    }

    @Transactional
    public Offer retire(Integer id, OfferActionRequest request, String actor, boolean admin) {
        return transitionTo(id, OfferStatus.RETIRED, request, actor, admin, false);
    }

    @Transactional
    public Offer changeAvailability(Integer id, OfferAvailabilityRequest request, String actor, boolean admin) {
        if (request == null || request.getAvailabilityStatus() == null) {
            throw new OfferValidationException("availabilityStatus is required.");
        }
        requireAudit(request.getReason(), request.getSource());
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        authorizeOffer(offer, actor, admin);
        if (offer.getStatus() == OfferStatus.RETIRED) {
            throw new OfferValidationException("A RETIRED offer cannot change availability.");
        }
        OfferAvailabilityStatus actual = availabilityFor(offer.getProduct());
        if (request.getAvailabilityStatus() != actual) {
            throw new OfferValidationException("Availability is derived from product quantity.");
        }
        offer.setAvailabilityStatus(actual);
        recordHistory(offer, offer.getStatus(), offer.getStatus(), request.getReason(), request.getSource(), actor);
        return repository.save(offer);
    }

    /** Revalidates status and product stock immediately before checkout. */
    @Transactional
    public Offer validateForCheckout(Integer id) {
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        OfferAvailabilityStatus actual = availabilityFor(offer.getProduct());
        offer.setAvailabilityStatus(actual);
        if (offer.getStatus() != OfferStatus.PUBLISHED) {
            throw new OfferValidationException("Offer " + id + " is not published and cannot start checkout.");
        }
        if (actual != OfferAvailabilityStatus.AVAILABLE) {
            repository.save(offer);
            throw new OfferValidationException("Offer " + id + " is unavailable and cannot start checkout.");
        }
        return offer;
    }

    private Offer transitionTo(Integer id, OfferStatus target, OfferActionRequest request,
                               String actor, boolean admin, boolean requireAvailable) {
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        authorizeOffer(offer, actor, admin);
        requireTransition(offer, target);
        if (requireAvailable) {
            validatePublishable(offer);
        }
        transition(offer, target, request, actor);
        offer.setAvailabilityStatus(availabilityFor(offer.getProduct()));
        return repository.save(offer);
    }

    private void validatePublishable(Offer offer) {
        if (offer.getPrice() == null || offer.getPrice().signum() <= 0
                || isBlank(offer.getDeliveryMethod()) || isBlank(offer.getContextGame())
                || isBlank(offer.getContextPlatform())
                || availabilityFor(offer.getProduct()) != OfferAvailabilityStatus.AVAILABLE) {
            throw new OfferValidationException("An offer requires positive price, game, platform, delivery method and available product stock to be published.");
        }
    }

    private void transition(Offer offer, OfferStatus target, OfferActionRequest request, String actor) {
        if (request == null) {
            throw new OfferValidationException("reason and source are required for offer status changes.");
        }
        requireAudit(request.getReason(), request.getSource());
        OfferStatus previous = offer.getStatus();
        offer.setStatus(target);
        recordHistory(offer, previous, target, request.getReason(), request.getSource(), actor);
    }

    private void requireTransition(Offer offer, OfferStatus target) {
        boolean allowed = switch (offer.getStatus()) {
            case DRAFT -> target == OfferStatus.PUBLISHED || target == OfferStatus.RETIRED;
            case PUBLISHED -> target == OfferStatus.PAUSED || target == OfferStatus.RETIRED;
            case PAUSED -> target == OfferStatus.PUBLISHED || target == OfferStatus.RETIRED;
            case RETIRED -> false;
        };
        if (!allowed) {
            throw new OfferValidationException("Invalid offer transition from " + offer.getStatus() + " to " + target + ".");
        }
    }

    private void validateFields(OfferWeb web) {
        if (web == null || web.getProductId() == null) {
            throw new OfferValidationException("productId is required.");
        }
        validateFields(web.getProductId(), web.getGame(), web.getPlatform(), web.getPrice(), web.getDeliveryMethod());
    }

    private void validateFields(Integer productId, String game, String platform, BigDecimal price, String deliveryMethod) {
        if (productId == null) {
            throw new OfferValidationException("productId is required.");
        }
        if (isBlank(game) || isBlank(platform)) {
            throw new OfferValidationException("game and platform are required.");
        }
        if (price == null || price.signum() <= 0) {
            throw new OfferValidationException("price must be positive.");
        }
        if (isBlank(deliveryMethod)) {
            throw new OfferValidationException("deliveryMethod is required.");
        }
    }

    private void authorizeProduct(Product product, String actor, boolean admin) {
        if (!admin && (product.getUser() == null || !actor.equals(product.getUser().getUsername()))) {
            throw new OfferValidationException("The product does not belong to the authenticated seller.");
        }
    }

    private void authorizeOffer(Offer offer, String actor, boolean admin) {
        authorizeProduct(offer.getProduct(), actor, admin);
    }

    private void requireAudit(String reason, String source) {
        if (isBlank(reason) || isBlank(source)) {
            throw new OfferValidationException("reason and source are required for offer changes.");
        }
    }

    private void recordHistory(Offer offer, OfferStatus from, OfferStatus to,
                               String reason, String source, String actor) {
        historyRepository.save(OfferStatusHistory.builder()
                .offerId(offer.getId())
                .fromStatus(from)
                .toStatus(to)
                .reason(reason.trim())
                .source(source.trim())
                .changedBy(actor)
                .recordedAt(LocalDateTime.now())
                .build());
    }

    private OfferAvailabilityStatus availabilityFor(Product product) {
        return isAvailable(product) ? OfferAvailabilityStatus.AVAILABLE : OfferAvailabilityStatus.UNAVAILABLE;
    }

    private boolean isAvailable(Product product) {
        if (product == null) return false;
        if (inventoryBalanceRepository == null) {
            return product.getQuantity() != null && product.getQuantity().signum() > 0;
        }
        return inventoryBalanceRepository.findByProduct_Id(product.getId())
                .map(InventoryBalance::getAvailableQuantity)
                .map(quantity -> quantity != null && quantity.signum() > 0)
                .orElseGet(() -> product.getQuantity() != null && product.getQuantity().signum() > 0);
    }

    private static String normalize(String search) {
        return search == null ? "" : search.trim();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
