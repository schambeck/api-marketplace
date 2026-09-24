package com.schambeck.marketplace.usecase.inventory;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class InventoryReservationRequest {
    private Integer productId;
    private Integer orderItemId;
    private BigDecimal quantity;
    private String operationKey;
}
