package com.ringo.dto.company;

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
public class OrganisationResponseDto extends UserResponseDto {
    private String description;
    private Float rating;
    private String contacts;
    private Integer pastEventsCount;
    private Integer upcomingEventsCount;
}
