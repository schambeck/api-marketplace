package com.schambeck.marketplace.usecase.inventory;

import com.schambeck.marketplace.usecase.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "inventory_balance")
@EntityListeners(AuditingEntityListener.class)
public class InventoryBalance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "available_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal availableQuantity;
    @Column(name = "reserved_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal reservedQuantity;
    @Column(name = "allocated_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal allocatedQuantity;
    @Column(name = "consumed_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal consumedQuantity;

    @Version @Column(name = "version", nullable = false)
    private Integer version;
    @CreatedDate @Column(name = "created_at")
    private LocalDateTime createdAt;
    @LastModifiedDate @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
