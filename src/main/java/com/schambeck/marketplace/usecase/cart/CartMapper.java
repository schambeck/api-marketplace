package com.schambeck.marketplace.usecase.cart;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;

@Mapper
public interface CartMapper {
    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    CartWeb toWeb(Cart entity);

    Cart toEntity(CartWeb web);

    void copy(Cart source, @MappingTarget Cart target);
}
