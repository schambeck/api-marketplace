package com.schambeck.marketplace.usecase.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryExpirationScheduler {
    private final InventoryService inventoryService;

    @Scheduled(fixedDelayString = "${inventory.expiration-delay-ms:60000}")
    public void expireReservations() {
        inventoryService.expireReservations();
    }
}
