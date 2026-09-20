package com.schambeck.marketplace.usecase.gamecontext;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@Getter
@Entity
@Table(name = "game_context_history")
@NoArgsConstructor
@AllArgsConstructor
public class GameContextHistory {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "game_context_id", nullable = false)
    private Integer gameContextId;

    @Column(name = "action", nullable = false)
    private String action;

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

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
