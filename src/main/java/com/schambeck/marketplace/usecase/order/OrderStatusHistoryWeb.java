package com.schambeck.marketplace.usecase.order;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class OrderStatusHistoryWeb {
    private String fromStatus; private String toStatus; private String cause; private String source;
    private String actor; private String idempotencyKey; private LocalDateTime createdAt;

    public static OrderStatusHistoryWeb from(OrderStatusHistory value) {
        return builder().fromStatus(value.getFromStatus()).toStatus(value.getToStatus()).cause(value.getCause())
                .source(value.getSource()).actor(value.getActor()).idempotencyKey(value.getIdempotencyKey())
                .createdAt(value.getCreatedAt()).build();
    }
}
