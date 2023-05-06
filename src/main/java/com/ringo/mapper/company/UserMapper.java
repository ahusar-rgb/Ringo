package com.ringo.mapper.company;

import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.security.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserRequestDto, UserResponseDto, User> {

    @Override
    @Mapping(target = "profilePicture", source = "profilePicture.id")
    UserResponseDto toDto(User entity);

    @Override
    @Mapping(target = "profilePicture", ignore = true)
    User toEntity(UserRequestDto entityDto);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget User destination, UserRequestDto source);
}
