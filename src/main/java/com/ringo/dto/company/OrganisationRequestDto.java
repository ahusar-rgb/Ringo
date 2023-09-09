package com.ringo.dto.company;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganisationRequestDto extends UserRequestDto {
    @Pattern(regexp = "^.{3,5000}$", message = "Description must be between 3 and 5000 characters")
    private String description;
    private List<LabelDto> contacts;
}
