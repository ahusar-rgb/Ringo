package com.ringo.mock.model;

import com.ringo.model.security.User;

public class UserMock {
    public static User getUserMock() {
        return User.builder()
                .id(System.currentTimeMillis())
                .isActive(true)
                .email("test@test.com)")
                .name("Test")
                .username("test")
                .password("test")
                .build();
    }
}
