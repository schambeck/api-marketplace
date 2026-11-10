package com.schambeck.marketplace.usecase.seller;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "seller_application_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SellerApplicationHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "application_id") private SellerApplication application;
    @Column(name = "from_status") private String fromStatus;
    @Column(name = "to_status", nullable = false) private String toStatus;
    private String reason;
    @Column(nullable = false) private String actor;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    static SellerApplicationHistory of(SellerApplication a, SellerApplicationStatus from, SellerApplicationStatus to, String reason, String actor) {
        return builder().application(a).fromStatus(from == null ? null : from.name()).toStatus(to.name()).reason(reason).actor(actor).createdAt(LocalDateTime.now()).build();
    }
}
