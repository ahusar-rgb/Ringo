package com.ringo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordForm {
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "New password is required")
    private String newPassword;
}
