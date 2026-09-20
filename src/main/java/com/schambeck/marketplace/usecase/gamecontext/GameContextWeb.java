package com.schambeck.marketplace.usecase.gamecontext;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class GameContextWeb {
    private Integer id;
    private String game;
    private String publisher;
    private String platform;
    private String region;
    private String server;
    private String variant;
    private GameContextStatus status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
