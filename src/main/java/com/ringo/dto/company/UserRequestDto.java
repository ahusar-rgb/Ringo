package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserRequestDto extends AbstractEntityDto {
    @Pattern(regexp = "^[a-zA-Z]{3,49}$", message = "Name must be between 3 and 49 characters")
    private String name;
    @Pattern(regexp = "^[a-zA-Z0-9]{3,29}$", message = "Username must be between 3 and 29 characters")
    private String username;
    @Pattern(regexp = "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W]).{8,64})$", message = "Password must be between 8 and 64 characters and contain at least one digit, one lowercase letter, one uppercase letter and one special character")
    private String password;
    @Email
    private String email;
}
