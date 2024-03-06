package com.schambeck.marketplace.usecase.cartitem;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;

@Mapper
public interface CartItemMapper {
    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    CartItemWeb toWeb(CartItem entity);

    CartItem toEntity(CartItemWeb web);

    void copy(CartItem source, @MappingTarget CartItem target);
}
