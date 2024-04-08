package com.schambeck.marketplace.webhook;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;

@Mapper
public interface WebhookMapper {
    WebhookMapper INSTANCE = Mappers.getMapper(WebhookMapper.class);

    WebhookWeb toWeb(Webhook entity);

    Webhook toEntity(WebhookWeb web);

    void copy(Webhook source, @MappingTarget Webhook target);
}
