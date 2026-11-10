package com.schambeck.marketplace.usecase.seller;

import com.schambeck.marketplace.usecase.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_application")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SellerApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "public_name", nullable = false) private String publicName;
    @Column(nullable = false, length = 1000) private String description;
    @Column(name = "contact_email", nullable = false) private String contactEmail;
    @Column(name = "item_types", nullable = false, length = 500) private String itemTypes;
    @Column(name = "delivery_method", nullable = false) private String deliveryMethod;
    @Column(name = "terms_version", nullable = false) private String termsVersion;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SellerApplicationStatus status;
    @Column(name = "decision_reason", length = 1000) private String decisionReason;
    @Column(name = "decided_by") private String decidedBy;
    @CreatedDate @Column(name = "submitted_at", nullable = false) private LocalDateTime submittedAt;
    @LastModifiedDate @Column(name = "modified_at", nullable = false) private LocalDateTime modifiedAt;
    @Version @Column(nullable = false) private Integer version;
}
