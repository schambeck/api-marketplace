package com.schambeck.marketplace.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@RequiredArgsConstructor
public class WebhookWeb {
    private final Integer identifier;
    private final String action;
    @JsonProperty("api_version")
    private final String apiVersion;
    private final JsonNode data;
    @JsonProperty("date_created")
    private final String dateCreated;
    private final String id;
    @JsonProperty("live_mode")
    private final Boolean liveMode;
    private final String type;
    @JsonProperty("user_id")
    private final Long userId;
    private final Integer version;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
