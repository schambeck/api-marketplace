package com.schambeck.marketplace.usecase.order;

import com.schambeck.marketplace.usecase.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @Entity @Table(name="order")
public class Order {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(name="public_id", nullable=false, unique=true) private UUID publicId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="buyer_id") private User buyer;
    @Column(name="cart_id") private Integer cartId;
    @Column(name="checkout_key", nullable=false) private String checkoutKey;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private OrderStatus status;
    @Column(name="subtotal_amount", nullable=false) private BigDecimal subtotalAmount;
    @Column(name="discount_total", nullable=false) private BigDecimal discountTotal;
    @Column(name="tax_total", nullable=false) private BigDecimal taxTotal;
    @Column(name="shipping_total", nullable=false) private BigDecimal shippingTotal;
    @Column(name="fee_total", nullable=false) private BigDecimal feeTotal;
    @Column(name="total_amount", nullable=false) private BigDecimal totalAmount;
    @Column(name="buyer_snapshot", columnDefinition="jsonb", nullable=false) private String buyerSnapshot;
    @Column(columnDefinition="jsonb") private String instructions;
    private String paymentReference, inventoryReference, fulfillmentReference, deliveryReference;
    @Version private Integer version;
    @Column(name="created_at") private LocalDateTime createdAt;
    @Column(name="modified_at") private LocalDateTime modifiedAt;
}
