package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TicketDto extends AbstractEntityDto {
    private ParticipantRequestDto user;
    private EventRequestDto event;
    private String timeOfSubmission;
    private String expiryDate;
    private Boolean isValidated;
}
