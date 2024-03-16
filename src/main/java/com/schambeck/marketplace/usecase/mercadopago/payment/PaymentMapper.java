package com.schambeck.marketplace.usecase.mercadopago.payment;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;

@Mapper
public interface PaymentMapper {
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    PaymentWeb toWeb(Payment entity);

    Payment toEntity(PaymentWeb web);

    void copy(Payment source, @MappingTarget Payment target);
}
