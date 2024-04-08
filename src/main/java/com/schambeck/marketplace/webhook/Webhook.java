package com.schambeck.marketplace.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.JSON;

@Builder
@Getter
@Setter
@Entity
@Table(name = "webhook")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Webhook {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer identifier;
    
    @Column(name = "action")
    private String action;
    
    @Column(name = "api_version")
    private String apiVersion;
    
    @JdbcTypeCode(JSON)
    @Column(name = "data")
    private JsonNode data;
    
    @Column(name = "date_created")
    private String dateCreated;
    
    @Column(name = "external_id")
    private String id;
    
    @Column(name = "live_mode")
    private Boolean liveMode;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "user_id")
    private Long userId;
    
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
