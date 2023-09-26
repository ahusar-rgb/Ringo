package com.ringo.dto.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdTokenDto {
    @NotBlank
    private String idToken;
}
