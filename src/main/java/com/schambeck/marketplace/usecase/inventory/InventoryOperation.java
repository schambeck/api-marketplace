package com.schambeck.marketplace.usecase.inventory;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "inventory_operation",
        uniqueConstraints = @UniqueConstraint(name = "uq_inventory_operation_key", columnNames = {"operation_type", "operation_key"}))
public class InventoryOperation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reservation_id", nullable = false) private Integer reservationId;
    @Column(name = "allocation_id") private Integer allocationId;
    @Column(name = "operation_type", nullable = false) private String operationType;
    @Column(name = "operation_key", nullable = false) private String operationKey;
    @Column(precision = 12, scale = 2) private BigDecimal quantity;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}
