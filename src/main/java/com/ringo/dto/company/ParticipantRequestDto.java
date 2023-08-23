package com.ringo.dto.company;

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
public class ParticipantRequestDto extends UserRequestDto {
    private String dateOfBirth;
    private String gender;
}
