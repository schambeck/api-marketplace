package com.schambeck.marketplace.usecase.inventory;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class InventoryMovementWeb {
    private Long id;
    private Integer reservationId;
    private Integer allocationId;
    private String operation;
    private String fromStatus;
    private String toStatus;
    private BigDecimal quantity;
    private String createdAt;

    static InventoryMovementWeb from(InventoryMovement movement) {
        InventoryMovementWeb web = new InventoryMovementWeb();
        web.id = movement.getId();
        web.reservationId = movement.getReservationId();
        web.allocationId = movement.getAllocationId();
        web.operation = movement.getOperation();
        web.fromStatus = movement.getFromStatus();
        web.toStatus = movement.getToStatus();
        web.quantity = movement.getQuantity();
        web.createdAt = movement.getCreatedAt() == null ? null : movement.getCreatedAt().toString();
        return web;
    }
}
