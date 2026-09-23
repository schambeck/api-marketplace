package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.usecase.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@Getter
@Setter
@Entity
@Table(name = "offer")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Offer {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "context_game", nullable = false)
    private String contextGame;

    @Column(name = "context_platform", nullable = false)
    private String contextPlatform;

    @Column(name = "context_region")
    private String contextRegion;

    @Column(name = "context_variant")
    private String contextVariant;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "delivery_method", nullable = false)
    private String deliveryMethod;

    @Enumerated(STRING)
    @Column(name = "availability_status", nullable = false)
    private OfferAvailabilityStatus availabilityStatus;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private OfferStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
