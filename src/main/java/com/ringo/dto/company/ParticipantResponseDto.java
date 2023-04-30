package com.ringo.dto.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParticipantResponseDto extends UserResponseDto {
    private String dateOfBirth;
    private String gender;
}
