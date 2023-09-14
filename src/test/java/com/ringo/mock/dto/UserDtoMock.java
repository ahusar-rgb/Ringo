package com.ringo.mock.dto;

import com.ringo.dto.company.request.UserRequestDto;
import com.ringo.it.util.IdGenerator;

public class UserDtoMock {
    public static UserRequestDto getUserDtoMock() {
        return UserRequestDto.builder()
                .email("test" + IdGenerator.getNewId() + "@test.com")
                .password("test")
                .build();
    }
}
