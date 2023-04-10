package com.ringo.mapper.company;

import com.ringo.dto.security.UserRequestDto;
import com.ringo.dto.security.UserResponseDto;
import com.ringo.model.security.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDto(User entity);

    User toEntity(UserRequestDto dto);

    List<User> toEntities(List<UserRequestDto> dtos);

    List<UserResponseDto> toDtos(List<User> entities);
}
