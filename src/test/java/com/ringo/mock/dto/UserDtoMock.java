package com.ringo.mock.dto;

import com.ringo.dto.company.UserRequestDto;

public class UserDtoMock {
    public static UserRequestDto getUserDtoMock() {
        return UserRequestDto.builder()
                .email("test@test.com")
                .password("test")
                .build();
    }
}
