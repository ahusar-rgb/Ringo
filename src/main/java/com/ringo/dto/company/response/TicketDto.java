package com.ringo.dto.company.response;

import com.ringo.model.form.RegistrationForm;
import com.ringo.model.form.RegistrationSubmission;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TicketDto {
    private ParticipantResponseDto participant;
    private EventSmallDto event;
    private String timeOfSubmission;
    private TicketTypeResponseDto ticketType;
    private String expiryDate;
    private Boolean isValidated;
    private String ticketCode;
    private RegistrationForm registrationForm;
    private RegistrationSubmission registrationSubmission;
}
