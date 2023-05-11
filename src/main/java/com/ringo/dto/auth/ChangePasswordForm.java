package com.ringo.dto.auth;

import lombok.Data;

@Data
public class ChangePasswordForm {
    private String password;
    private String newPassword;
}
