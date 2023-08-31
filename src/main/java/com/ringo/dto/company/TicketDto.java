package com.ringo.dto.company;

import com.ringo.model.form.RegistrationForm;
import com.ringo.model.form.RegistrationSubmission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TicketDto {
    private ParticipantResponseDto participant;
    private EventSmallDto event;
    private String timeOfSubmission;
    private String expiryDate;
    private Boolean isValidated;
    private String ticketCode;
    private RegistrationForm registrationForm;
    private RegistrationSubmission registrationSubmission;
}
