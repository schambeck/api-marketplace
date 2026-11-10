package com.schambeck.marketplace.usecase.order;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @Builder
public class OrderItemWeb {
    private Integer id; private Integer productId; private Integer offerId; private Integer sellerId;
    private String productName; private String productDescription; private String contextGame;
    private String contextPlatform; private String contextRegion; private String contextVariant;
    private String deliveryMethod; private BigDecimal unitPrice; private BigDecimal quantity;
    private BigDecimal subtotalAmount; private Integer snapshotVersion; private String snapshot;

    public static OrderItemWeb from(OrderItem item) {
        return builder().id(item.getId()).productId(item.getProduct().getId()).offerId(item.getOffer().getId())
                .sellerId(item.getSeller().getId()).productName(item.getProductName())
                .productDescription(item.getProductDescription()).contextGame(item.getContextGame())
                .contextPlatform(item.getContextPlatform()).contextRegion(item.getContextRegion())
                .contextVariant(item.getContextVariant()).deliveryMethod(item.getDeliveryMethod())
                .unitPrice(item.getUnitPrice()).quantity(item.getQuantity()).subtotalAmount(item.getSubtotalAmount())
                .snapshotVersion(item.getSnapshotVersion()).snapshot(item.getSnapshot()).build();
    }
}
