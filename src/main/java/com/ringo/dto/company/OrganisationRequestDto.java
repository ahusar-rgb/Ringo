package com.ringo.dto.company;

import com.ringo.dto.common.UserRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganisationRequestDto extends UserRequestDto {
    private String description;
    private Float rating;
    private String contacts;
}
