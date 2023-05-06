package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
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
    private String name;
    private String username;
    private String password;
    private String email;
    private String role;
}
