package com.ringo.dto.company.request;

import com.ringo.dto.company.LabelDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 5, message = "Number of contacts must be between 0 and 5")
    private List<@Valid LabelDto> contacts;
}
