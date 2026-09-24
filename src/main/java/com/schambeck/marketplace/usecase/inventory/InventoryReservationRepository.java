package com.schambeck.marketplace.usecase.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Integer> {
    Optional<InventoryReservation> findByOperationKey(String operationKey);

    Optional<InventoryReservation> findByReleaseOperationKey(String operationKey);

    Optional<InventoryReservation> findByExpirationOperationKey(String operationKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from InventoryReservation reservation join fetch reservation.product where reservation.id = :id")
    Optional<InventoryReservation> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from InventoryReservation reservation where reservation.status = :status and reservation.expiresAt <= :now order by reservation.id")
    List<InventoryReservation> findExpiredForUpdate(@Param("status") InventoryStatus status, @Param("now") LocalDateTime now);
}
