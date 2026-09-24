package com.schambeck.marketplace.usecase.inventory;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class InventoryAllocationWeb {
    private Integer id;
    private Integer reservationId;
    private BigDecimal quantity;
    private BigDecimal consumedQuantity;
    private InventoryAllocationStatus status;

    static InventoryAllocationWeb from(InventoryAllocation allocation) {
        InventoryAllocationWeb web = new InventoryAllocationWeb();
        web.id = allocation.getId();
        web.reservationId = allocation.getReservation().getId();
        web.quantity = allocation.getQuantity();
        web.consumedQuantity = allocation.getConsumedQuantity();
        web.status = allocation.getStatus();
        return web;
    }
}
