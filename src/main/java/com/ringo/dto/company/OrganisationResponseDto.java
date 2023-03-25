package com.ringo.dto.company;

import com.ringo.dto.common.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganisationResponseDto extends UserResponseDto {
    private String description;
    private Float rating;
    private String contacts;
    private List<Long> hostedEventIds;
}
