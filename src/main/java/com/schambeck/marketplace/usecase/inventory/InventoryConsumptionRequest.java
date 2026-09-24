package com.schambeck.marketplace.usecase.inventory;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class InventoryConsumptionRequest {
    private BigDecimal quantity;
    private String operationKey;
}
