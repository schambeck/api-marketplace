package com.schambeck.marketplace.usecase.inventory;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter @Setter
public class InventoryOrderWeb {
    private UUID orderId;
    private List<InventoryReservationWeb> reservations;
    private List<InventoryAllocationWeb> allocations;
    private List<InventoryMovementWeb> movements;
}
