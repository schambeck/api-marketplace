package com.schambeck.marketplace.usecase.gamecontext;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GameContextMapper {
    GameContextMapper INSTANCE = Mappers.getMapper(GameContextMapper.class);

    GameContextWeb toWeb(GameContext entity);

    GameContext toEntity(GameContextWeb web);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    void copy(GameContext source, @MappingTarget GameContext target);
}
