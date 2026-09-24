package com.schambeck.marketplace.usecase.inventory;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "inventory_allocation")
public class InventoryAllocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private InventoryReservation reservation;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal quantity;
    @Column(name = "consumed_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal consumedQuantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private InventoryAllocationStatus status;
    @Column(name = "operation_key", nullable = false, unique = true) private String operationKey;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "modified_at") private LocalDateTime modifiedAt;
}
