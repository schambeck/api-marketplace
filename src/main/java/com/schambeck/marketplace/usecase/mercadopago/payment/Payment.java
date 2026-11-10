package com.schambeck.marketplace.usecase.mercadopago.payment;

import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@Getter
@Setter
@Entity
@Table(name = "payment")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = IDENTITY)
	private Integer id;

	@ManyToOne(cascade = MERGE)
	@JoinColumn(name = "cart_id", referencedColumnName = "id")
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@Column(name = "payment_id")
	private Long paymentId;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "external_reference")
	private String externalReference;
	
	@Column(name = "merchant_order_id")
	private Long merchantOrderId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "detail")
	private PaymentDetail detail;
	
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
