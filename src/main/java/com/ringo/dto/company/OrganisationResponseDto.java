package com.ringo.dto.company;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganisationResponseDto extends UserResponseDto {
    private String description;
    private Float rating;
    private String contacts;
    private Set<Long> hostedEventIds;
}
