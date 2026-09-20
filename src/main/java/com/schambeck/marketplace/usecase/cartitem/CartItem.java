package com.schambeck.marketplace.usecase.cartitem;

import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.offer.Offer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@Getter
@Setter
@Entity
@Table(name = "cart_item")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CartItem {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = IDENTITY)
	private Integer id;

	@ManyToOne(cascade = MERGE)
	@JoinColumn(name = "cart_id", referencedColumnName = "id")
	private Cart cart;

	@ManyToOne(cascade = MERGE)
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "offer_id", referencedColumnName = "id")
	private Offer offer;

	@Column(name = "price")
	private BigDecimal price;

	@Column(name = "quantity")
	private BigDecimal quantity;

	@Column(name = "total")
	private BigDecimal total;

	@Version
	@Column(name = "version")
	private Integer version;

	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "modified_at")
	private LocalDateTime modifiedAt;
}
