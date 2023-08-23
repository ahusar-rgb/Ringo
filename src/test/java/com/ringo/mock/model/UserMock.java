package com.ringo.mock.model;

import com.ringo.it.util.IdGenerator;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;

public class UserMock {
    public static User getUserMock() {
        return User.builder()
                .id(IdGenerator.getNewId())
                .isActive(true)
                .email("test@test.com)")
                .name("Test")
                .username("test")
                .password("test")
                .role(Role.ROLE_ORGANISATION)
                .build();
    }
}
