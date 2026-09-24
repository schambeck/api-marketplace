package com.schambeck.marketplace.usecase.inventory;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "inventory_movement")
public class InventoryMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_id", nullable = false) private Integer productId;
    @Column(name = "reservation_id") private Integer reservationId;
    @Column(name = "allocation_id") private Integer allocationId;
    @Column(nullable = false) private String operation;
    @Column(name = "from_status") private String fromStatus;
    @Column(name = "to_status", nullable = false) private String toStatus;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal quantity;
    @Column(name = "operation_key", nullable = false) private String operationKey;
    private String actor;
    private String source;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}
