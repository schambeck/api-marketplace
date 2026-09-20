package com.schambeck.marketplace.usecase.cartitem;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;

@Mapper
public interface CartItemMapper {
    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    @Mapping(target = "offerId", source = "offer.id")
    CartItemWeb toWeb(CartItem entity);

    @Mapping(target = "offer", ignore = true)
    CartItem toEntity(CartItemWeb web);

    void copy(CartItem source, @MappingTarget CartItem target);
}
