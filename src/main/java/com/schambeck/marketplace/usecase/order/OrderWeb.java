package com.schambeck.marketplace.usecase.order;
import lombok.*; import java.math.BigDecimal; import java.util.UUID;
@Getter @Builder public class OrderWeb { private UUID publicId; private OrderStatus status; private BigDecimal subtotalAmount,totalAmount; public static OrderWeb from(Order o){return builder().publicId(o.getPublicId()).status(o.getStatus()).subtotalAmount(o.getSubtotalAmount()).totalAmount(o.getTotalAmount()).build();} }
