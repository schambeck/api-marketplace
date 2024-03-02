package com.schambeck.marketplace.usecase.user;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.MappingTarget;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserWeb toWeb(User entity);

    User toEntity(UserWeb web);

    void copy(User source, @MappingTarget User target);
}
