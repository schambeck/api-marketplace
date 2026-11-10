package com.schambeck.marketplace.usecase.order;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter @Builder
public class OrderWeb {
    private UUID publicId; private OrderStatus status; private BigDecimal subtotalAmount;
    private BigDecimal discountTotal; private BigDecimal taxTotal; private BigDecimal shippingTotal;
    private BigDecimal feeTotal; private BigDecimal totalAmount; private String buyerSnapshot;
    private String instructions; private String paymentReference; private String inventoryReference;
    private String fulfillmentReference; private String deliveryReference;
    private List<OrderItemWeb> items; private List<OrderStatusHistoryWeb> history;

    public static OrderWeb from(Order order, OrderService service) {
        return builder().publicId(order.getPublicId()).status(order.getStatus())
                .subtotalAmount(order.getSubtotalAmount()).discountTotal(order.getDiscountTotal())
                .taxTotal(order.getTaxTotal()).shippingTotal(order.getShippingTotal()).feeTotal(order.getFeeTotal())
                .totalAmount(order.getTotalAmount()).buyerSnapshot(order.getBuyerSnapshot())
                .instructions(order.getInstructions()).paymentReference(order.getPaymentReference())
                .inventoryReference(order.getInventoryReference()).fulfillmentReference(order.getFulfillmentReference())
                .deliveryReference(order.getDeliveryReference())
                .items(service.items(order).stream().map(OrderItemWeb::from).toList())
                .history(service.history(order).stream().map(OrderStatusHistoryWeb::from).toList()).build();
    }
}
