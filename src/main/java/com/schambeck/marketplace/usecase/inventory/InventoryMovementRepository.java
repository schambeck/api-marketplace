package com.schambeck.marketplace.usecase.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findAllByReservationIdInOrderByIdAsc(List<Integer> reservationIds);
}
