package com.ringo.mapper.common;

import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.model.security.User;
import org.mapstruct.*;

public interface AbstractUserMapper<S extends UserRequestDto, T extends User, R extends UserResponseDto>
        extends EntityMapper<S, R, T>{
    @Override
    @Mapping(target = "profilePictureId", source = "profilePicture.id")
    R toDto(T entity);

    @Override
    @Mapping(target = "profilePictureId", source = "profilePicture.id")
    @Named("toDtoDetails")
    R toDtoDetails(T entity);

    @Override
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "id", ignore = true)
    T toEntity(S dto);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget T destination, S source);

    @Named("fromUser")
    T fromUser(User user);
}