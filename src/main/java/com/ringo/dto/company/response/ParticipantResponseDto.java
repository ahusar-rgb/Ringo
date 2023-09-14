package com.ringo.dto.company.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class ParticipantResponseDto extends UserResponseDto {
    private String dateOfBirth;
    private String gender;
}
