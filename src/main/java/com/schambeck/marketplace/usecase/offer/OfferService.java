package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import com.schambeck.marketplace.exception.OfferValidationException;
import com.schambeck.marketplace.usecase.gamecontext.GameContext;
import com.schambeck.marketplace.usecase.gamecontext.GameContextReference;
import com.schambeck.marketplace.usecase.gamecontext.GameContextService;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.product.ProductRepository;
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
    private final GameContextService gameContextService;

    @Transactional
    public Offer create(OfferWeb web) {
        return create(web, null);
    }

    @Transactional
    public Offer create(OfferWeb web, String actor) {
        validateFields(web);
        Product product = productRepository.findByIdOrThrow(web.getProductId());
        GameContext context = gameContextService.findById(web.getGameContextId());
        Offer offer = Offer.builder()
                .product(product)
                .gameContext(context)
                .contextVersion(context.getVersion())
                .contextGame(context.getGame())
                .contextPublisher(context.getPublisher())
                .contextPlatform(context.getPlatform())
                .contextRegion(context.getRegion())
                .contextServer(context.getServer())
                .contextVariant(context.getVariant())
                .price(web.getPrice())
                .deliveryMethod(web.getDeliveryMethod().trim())
                .availabilitySource(web.getAvailabilitySource().trim())
                .availabilityStatus(web.getAvailabilityStatus())
                .status(OfferStatus.DRAFT)
                .version(0)
                .build();
        Offer saved = repository.save(offer);
        recordHistory(saved, null, OfferStatus.DRAFT, "created", "api", actor);
        return saved;
    }

    @Transactional
    public Offer updateDraft(Integer id, OfferUpdateRequest request, String actor) {
        if (request == null || request.getProductId() == null || request.getGameContextId() == null) {
            throw new OfferValidationException("productId and gameContextId are required.");
        }
        if (isBlank(request.getReason()) || isBlank(request.getSource())) {
            throw new OfferValidationException("reason and source are required for offer changes.");
        }
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw new OfferValidationException("Only a DRAFT offer can be changed.");
        }
        validateFields(request.getProductId(), request.getGameContextId(), request.getPrice(),
                request.getDeliveryMethod(), request.getAvailabilitySource(), request.getAvailabilityStatus());
        Product product = productRepository.findByIdOrThrow(request.getProductId());
        GameContext context = gameContextService.findById(request.getGameContextId());
        offer.setProduct(product);
        offer.setGameContext(context);
        offer.setContextVersion(context.getVersion());
        offer.setContextGame(context.getGame());
        offer.setContextPublisher(context.getPublisher());
        offer.setContextPlatform(context.getPlatform());
        offer.setContextRegion(context.getRegion());
        offer.setContextServer(context.getServer());
        offer.setContextVariant(context.getVariant());
        offer.setPrice(request.getPrice());
        offer.setDeliveryMethod(request.getDeliveryMethod().trim());
        offer.setAvailabilitySource(request.getAvailabilitySource().trim());
        offer.setAvailabilityStatus(request.getAvailabilityStatus());
        recordHistory(offer, OfferStatus.DRAFT, OfferStatus.DRAFT, request.getReason(), request.getSource(), actor);
        return repository.save(offer);
    }

    public Page<Offer> searchPublished(Pageable pageable) {
        return searchPublished(pageable, null);
    }

    public Page<Offer> searchPublished(Pageable pageable, String search) {
        String normalizedSearch = search == null ? "" : search.trim();
        return repository.findAllByStatusAndSearch(OfferStatus.PUBLISHED, normalizedSearch, pageable);
    }

    public Page<Offer> searchAdmin(Pageable pageable, OfferStatus status, String search) {
        String normalizedSearch = search == null ? "" : search.trim();
        return repository.searchAdmin(status, normalizedSearch, pageable);
    }

    public Offer findPublishedById(Integer id) {
        Offer offer = repository.findByIdOrThrow(id);
        if (offer.getStatus() != OfferStatus.PUBLISHED) {
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

    public List<OfferStatusHistory> history(Integer id) {
        findById(id);
        return historyRepository.findAllByOfferIdOrderByIdAsc(id);
    }

    @Transactional
    public Offer publish(Integer id, OfferActionRequest request, String actor) {
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        requireTransition(offer, OfferStatus.PUBLISHED);
        if (offer.getPrice() == null || offer.getPrice().signum() <= 0
                || isBlank(offer.getDeliveryMethod()) || isBlank(offer.getAvailabilitySource())
                || offer.getAvailabilityStatus() != OfferAvailabilityStatus.AVAILABLE) {
            throw new OfferValidationException("An offer requires positive price, delivery method, availability source and AVAILABLE status to be published.");
        }
        GameContextReference reference = gameContextService.referenceForOfferPublication(offer.getGameContext().getId());
        updateContextSnapshot(offer, reference);
        transition(offer, OfferStatus.PUBLISHED, request, actor);
        return repository.save(offer);
    }

    @Transactional
    public Offer pause(Integer id, OfferActionRequest request, String actor) {
        return transitionLocked(id, OfferStatus.PAUSED, request, actor);
    }

    @Transactional
    public Offer reactivate(Integer id, OfferActionRequest request, String actor) {
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        requireTransition(offer, OfferStatus.PUBLISHED);
        if (offer.getAvailabilityStatus() != OfferAvailabilityStatus.AVAILABLE) {
            throw new OfferValidationException("An unavailable offer cannot be reactivated.");
        }
        GameContextReference reference = gameContextService.referenceForOfferPublication(offer.getGameContext().getId());
        updateContextSnapshot(offer, reference);
        transition(offer, OfferStatus.PUBLISHED, request, actor);
        return repository.save(offer);
    }

    @Transactional
    public Offer retire(Integer id, OfferActionRequest request, String actor) {
        return transitionLocked(id, OfferStatus.RETIRED, request, actor);
    }

    @Transactional
    public Offer changeAvailability(Integer id, OfferAvailabilityRequest request, String actor) {
        if (request == null || request.getAvailabilityStatus() == null) {
            throw new OfferValidationException("availabilityStatus is required.");
        }
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        if (offer.getStatus() == OfferStatus.RETIRED) {
            throw new OfferValidationException("A RETIRED offer cannot change availability.");
        }
        if (isBlank(request.getReason()) || isBlank(request.getSource())) {
            throw new OfferValidationException("reason and source are required for availability changes.");
        }
        offer.setAvailabilityStatus(request.getAvailabilityStatus());
        recordHistory(offer, offer.getStatus(), offer.getStatus(), request.getReason(), request.getSource(), actor);
        return repository.save(offer);
    }

    /** Revalidates the offer while holding a row lock immediately before checkout. */
    @Transactional
    public Offer validateForCheckout(Integer id) {
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        if (offer.getStatus() != OfferStatus.PUBLISHED) {
            throw new OfferValidationException("Offer " + id + " is not published and cannot start checkout.");
        }
        if (offer.getAvailabilityStatus() != OfferAvailabilityStatus.AVAILABLE) {
            throw new OfferValidationException("Offer " + id + " is unavailable and cannot start checkout.");
        }
        gameContextService.referenceForOfferPublication(offer.getGameContext().getId());
        return offer;
    }

    private Offer transitionLocked(Integer id, OfferStatus target, OfferActionRequest request, String actor) {
        Offer offer = repository.findByIdForUpdateOrThrow(id);
        requireTransition(offer, target);
        transition(offer, target, request, actor);
        return repository.save(offer);
    }

    private void transition(Offer offer, OfferStatus target, OfferActionRequest request, String actor) {
        if (request == null || isBlank(request.getReason()) || isBlank(request.getSource())) {
            throw new OfferValidationException("reason and source are required for offer status changes.");
        }
        OfferStatus previous = offer.getStatus();
        offer.setStatus(target);
        recordHistory(offer, previous, target, request.getReason(), request.getSource(), actor);
    }

    private void requireTransition(Offer offer, OfferStatus target) {
        OfferStatus current = offer.getStatus();
        boolean allowed = switch (current) {
            case DRAFT -> target == OfferStatus.PUBLISHED || target == OfferStatus.RETIRED;
            case PUBLISHED -> target == OfferStatus.PAUSED || target == OfferStatus.RETIRED;
            case PAUSED -> target == OfferStatus.PUBLISHED || target == OfferStatus.RETIRED;
            case RETIRED -> false;
        };
        if (!allowed) {
            throw new OfferValidationException("Invalid offer transition from " + current + " to " + target + ".");
        }
    }

    private void updateContextSnapshot(Offer offer, GameContextReference reference) {
        offer.setContextVersion(reference.version());
        offer.setContextGame(reference.game());
        offer.setContextPublisher(reference.publisher());
        offer.setContextPlatform(reference.platform());
        offer.setContextRegion(reference.region());
        offer.setContextServer(reference.server());
        offer.setContextVariant(reference.variant());
    }

    private void validateFields(OfferWeb web) {
        if (web == null || web.getProductId() == null || web.getGameContextId() == null) {
            throw new OfferValidationException("productId and gameContextId are required.");
        }
        validateFields(web.getProductId(), web.getGameContextId(), web.getPrice(), web.getDeliveryMethod(),
                web.getAvailabilitySource(), web.getAvailabilityStatus());
    }

    private void validateFields(Integer productId, Integer gameContextId, BigDecimal price, String deliveryMethod,
                                String availabilitySource, OfferAvailabilityStatus availabilityStatus) {
        if (productId == null || gameContextId == null) {
            throw new OfferValidationException("productId and gameContextId are required.");
        }
        if (price == null || price.signum() <= 0) {
            throw new OfferValidationException("price must be positive.");
        }
        if (isBlank(deliveryMethod) || isBlank(availabilitySource) || availabilityStatus == null) {
            throw new OfferValidationException("deliveryMethod, availabilitySource and availabilityStatus are required.");
        }
    }

    private void recordHistory(Offer offer, OfferStatus from, OfferStatus to, String reason, String source, String actor) {
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
