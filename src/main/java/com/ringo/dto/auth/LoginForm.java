package com.ringo.dto.auth;

import lombok.Data;

@Data
public class LoginForm {
    private String email;
    private String password;
}
