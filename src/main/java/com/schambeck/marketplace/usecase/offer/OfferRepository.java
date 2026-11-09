package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Integer> {
    @Query(value = """
            select offer from Offer offer
            join fetch offer.product product
            join product.user owner
            where owner.username = :username
              and (:status is null or offer.status = :status)
              and (:search = '' or lower(product.name) like lower(concat('%', :search, '%'))
                   or lower(product.description) like lower(concat('%', :search, '%'))
                   or lower(product.category) like lower(concat('%', :search, '%'))
                   or lower(offer.contextGame) like lower(concat('%', :search, '%'))
                   or lower(offer.contextPlatform) like lower(concat('%', :search, '%'))
                   or lower(coalesce(offer.contextRegion, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(offer.contextVariant, '')) like lower(concat('%', :search, '%')))
            order by offer.id asc
            """, countQuery = """
            select count(offer) from Offer offer
            join offer.product product join product.user owner
            where owner.username = :username
              and (:status is null or offer.status = :status)
              and (:search = '' or lower(product.name) like lower(concat('%', :search, '%'))
                   or lower(product.description) like lower(concat('%', :search, '%'))
                   or lower(product.category) like lower(concat('%', :search, '%'))
                   or lower(offer.contextGame) like lower(concat('%', :search, '%'))
                   or lower(offer.contextPlatform) like lower(concat('%', :search, '%'))
                   or lower(coalesce(offer.contextRegion, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(offer.contextVariant, '')) like lower(concat('%', :search, '%')))
            """)
    Page<Offer> searchOwned(@Param("username") String username, @Param("status") OfferStatus status,
                            @Param("search") String search, Pageable pageable);

    @Query("select offer from Offer offer join fetch offer.product product join product.user owner " +
            "where offer.id = :id and owner.username = :username")
    Optional<Offer> findByIdAndOwnerUsername(@Param("id") Integer id, @Param("username") String username);

    @Query(value = """
            select offer from Offer offer join fetch offer.product product
            where offer.status = 'PUBLISHED'
              and exists (select balance.id from InventoryBalance balance
                          where balance.product = product and balance.availableQuantity > 0)
              and (:search = '' or lower(product.name) like lower(concat('%', :search, '%'))
                   or lower(product.description) like lower(concat('%', :search, '%'))
                   or lower(product.category) like lower(concat('%', :search, '%'))
                   or lower(offer.contextGame) like lower(concat('%', :search, '%'))
                   or lower(offer.contextPlatform) like lower(concat('%', :search, '%'))
                   or lower(coalesce(offer.contextRegion, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(offer.contextVariant, '')) like lower(concat('%', :search, '%')))
            order by offer.id asc
            """, countQuery = """
            select count(offer) from Offer offer join offer.product product
            where offer.status = 'PUBLISHED'
              and exists (select balance.id from InventoryBalance balance
                          where balance.product = product and balance.availableQuantity > 0)
              and (:search = '' or lower(product.name) like lower(concat('%', :search, '%'))
                   or lower(product.description) like lower(concat('%', :search, '%'))
                   or lower(product.category) like lower(concat('%', :search, '%'))
                   or lower(offer.contextGame) like lower(concat('%', :search, '%'))
                   or lower(offer.contextPlatform) like lower(concat('%', :search, '%'))
                   or lower(coalesce(offer.contextRegion, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(offer.contextVariant, '')) like lower(concat('%', :search, '%')))
            """)
    Page<Offer> findAllPublishedAndAvailable(@Param("search") String search, Pageable pageable);

    Optional<Offer> findFirstByProduct_IdAndStatusOrderByIdAsc(Integer productId, OfferStatus status);

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
