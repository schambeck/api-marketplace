package com.schambeck.marketplace.usecase.inventory;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class InventoryReservationWeb {
    private Integer id;
    private Integer orderItemId;
    private Integer productId;
    private Integer sellerId;
    private BigDecimal quantity;
    private InventoryStatus status;
    private LocalDateTime expiresAt;

    static InventoryReservationWeb from(InventoryReservation reservation) {
        InventoryReservationWeb web = new InventoryReservationWeb();
        web.id = reservation.getId();
        web.orderItemId = reservation.getOrderItemId();
        web.productId = reservation.getProduct().getId();
        web.sellerId = reservation.getSeller().getId();
        web.quantity = reservation.getQuantity();
        web.status = reservation.getStatus();
        web.expiresAt = reservation.getExpiresAt();
        return web;
    }
}
