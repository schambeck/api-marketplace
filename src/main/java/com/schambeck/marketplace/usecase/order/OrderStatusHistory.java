package com.schambeck.marketplace.usecase.order;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @Entity @Table(name="order_status_history")
public class OrderStatusHistory { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id") private Order order; private String fromStatus,toStatus,cause,source,actor,idempotencyKey; @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb") private String metadata; @Column(name="created_at") private LocalDateTime createdAt; }
