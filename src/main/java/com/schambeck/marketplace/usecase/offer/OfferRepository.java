package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Integer> {
    @Query("select offer from Offer offer join fetch offer.product where offer.status = :status order by offer.id asc")
    Page<Offer> findAllByStatus(@Param("status") OfferStatus status, Pageable pageable);

    @Query(value = """
            select offer
            from Offer offer
            join fetch offer.product product
            where offer.status = :status
              and (
                  :search = ''
                  or lower(product.name) like lower(concat('%', :search, '%'))
                  or lower(product.description) like lower(concat('%', :search, '%'))
                  or lower(product.category) like lower(concat('%', :search, '%'))
                  or lower(offer.contextGame) like lower(concat('%', :search, '%'))
                  or lower(coalesce(offer.contextPublisher, '')) like lower(concat('%', :search, '%'))
                  or lower(offer.contextPlatform) like lower(concat('%', :search, '%'))
                  or lower(coalesce(offer.contextRegion, '')) like lower(concat('%', :search, '%'))
                  or lower(coalesce(offer.contextServer, '')) like lower(concat('%', :search, '%'))
                  or lower(coalesce(offer.contextVariant, '')) like lower(concat('%', :search, '%'))
              )
            order by offer.id asc
            """,
            countQuery = """
            select count(offer)
            from Offer offer
            join offer.product product
            where offer.status = :status
              and (
                  :search = ''
                  or lower(product.name) like lower(concat('%', :search, '%'))
                  or lower(product.description) like lower(concat('%', :search, '%'))
                  or lower(product.category) like lower(concat('%', :search, '%'))
                  or lower(offer.contextGame) like lower(concat('%', :search, '%'))
                  or lower(coalesce(offer.contextPublisher, '')) like lower(concat('%', :search, '%'))
                  or lower(offer.contextPlatform) like lower(concat('%', :search, '%'))
                  or lower(coalesce(offer.contextRegion, '')) like lower(concat('%', :search, '%'))
                  or lower(coalesce(offer.contextServer, '')) like lower(concat('%', :search, '%'))
                  or lower(coalesce(offer.contextVariant, '')) like lower(concat('%', :search, '%'))
              )
            """)
    Page<Offer> findAllByStatusAndSearch(@Param("status") OfferStatus status,
                                         @Param("search") String search,
                                         Pageable pageable);

    Optional<Offer> findFirstByProduct_IdAndStatusAndAvailabilityStatusOrderByIdAsc(
            Integer productId, OfferStatus status, OfferAvailabilityStatus availabilityStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select offer from Offer offer join fetch offer.product where offer.id = :id")
    Optional<Offer> findByIdForUpdate(@Param("id") Integer id);

    default Offer findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(Offer.class, id));
    }

    default Offer findByIdForUpdateOrThrow(Integer id) {
        return findByIdForUpdate(id).orElseThrow(() -> new EntityNotFoundException(Offer.class, id));
    }
}
