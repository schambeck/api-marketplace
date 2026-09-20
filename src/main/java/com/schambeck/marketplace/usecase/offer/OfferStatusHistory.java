package com.schambeck.marketplace.usecase.offer;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@Getter
@Entity
@Table(name = "offer_status_history")
@NoArgsConstructor
@AllArgsConstructor
public class OfferStatusHistory {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "offer_id", nullable = false)
    private Integer offerId;

    @Enumerated(STRING)
    @Column(name = "from_status")
    private OfferStatus fromStatus;

    @Enumerated(STRING)
    @Column(name = "to_status", nullable = false)
    private OfferStatus toStatus;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
