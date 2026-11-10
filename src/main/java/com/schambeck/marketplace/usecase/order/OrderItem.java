package com.schambeck.marketplace.usecase.order;

import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime;
import com.schambeck.marketplace.usecase.product.Product; import com.schambeck.marketplace.usecase.offer.Offer; import com.schambeck.marketplace.usecase.user.User;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate; import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @Entity @Table(name="order_item")
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id") private Order order;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id") private Product product;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="offer_id") private Offer offer;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="seller_id") private User seller;
 private String productName; @Column(columnDefinition="text") private String productDescription; private String contextGame,contextPlatform,contextRegion,contextVariant,deliveryMethod;
 @Column(nullable=false) private BigDecimal unitPrice; @Column(nullable=false) private BigDecimal quantity; @Column(nullable=false) private BigDecimal subtotalAmount;
 @Column(nullable=false) private Integer snapshotVersion; @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb",nullable=false) private String snapshot; @Version private Integer version;
 @CreatedDate @Column(name="created_at", nullable=false) private LocalDateTime createdAt; @LastModifiedDate @Column(name="modified_at", nullable=false) private LocalDateTime modifiedAt;
}
