package com.schambeck.marketplace.usecase.inventory;

import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "inventory_reservation")
public class InventoryReservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "order_item_id", nullable = false)
    private Integer orderItemId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private InventoryStatus status;
    @Column(name = "operation_key", nullable = false, unique = true)
    private String operationKey;
    @Column(name = "release_operation_key", unique = true)
    private String releaseOperationKey;
    @Column(name = "expiration_operation_key", unique = true)
    private String expirationOperationKey;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "modified_at") private LocalDateTime modifiedAt;
}
