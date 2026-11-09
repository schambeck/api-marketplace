package com.schambeck.marketplace.usecase.order;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @Entity @Table(name="order_status_history")
public class OrderStatusHistory { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id") private Order order; private String fromStatus,toStatus,cause,source,actor,idempotencyKey,metadata; @Column(name="created_at") private LocalDateTime createdAt; }
