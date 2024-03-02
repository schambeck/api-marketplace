package com.schambeck.marketplace.usecase.product;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductWeb toWeb(Product entity);

    Product toEntity(ProductWeb web);

    void copy(Product source, @MappingTarget Product target);
}
