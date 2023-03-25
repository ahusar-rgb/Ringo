package com.ringo.dto.company;

import com.ringo.dto.common.UserRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParticipantRequestDto extends UserRequestDto {
    private String dateOfBirth;
    private String gender;
}
