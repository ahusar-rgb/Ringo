package com.ringo.mapper.company;

import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.security.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserRequestDto, UserResponseDto, User> {

}
