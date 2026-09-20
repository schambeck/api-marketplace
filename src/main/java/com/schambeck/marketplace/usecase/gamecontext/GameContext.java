package com.schambeck.marketplace.usecase.gamecontext;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@Getter
@Setter
@Entity
@Table(name = "game_context")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GameContext {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "game", nullable = false)
    private String game;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "region")
    private String region;

    @Column(name = "server")
    private String server;

    @Column(name = "variant")
    private String variant;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private GameContextStatus status;

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
