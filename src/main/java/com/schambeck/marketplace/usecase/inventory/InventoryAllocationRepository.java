package com.schambeck.marketplace.usecase.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryAllocationRepository extends JpaRepository<InventoryAllocation, Integer> {
    Optional<InventoryAllocation> findByOperationKey(String operationKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select allocation from InventoryAllocation allocation join fetch allocation.reservation reservation where allocation.id = :id")
    Optional<InventoryAllocation> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select allocation from InventoryAllocation allocation where allocation.reservation.id = :reservationId and allocation.status in :statuses")
    Optional<InventoryAllocation> findActiveForUpdate(@Param("reservationId") Integer reservationId,
                                                       @Param("statuses") java.util.Collection<InventoryAllocationStatus> statuses);
}
