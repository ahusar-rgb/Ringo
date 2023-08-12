package com.ringo.mock.model;

import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.model.company.Ticket;
import com.ringo.model.company.TicketId;

import java.time.LocalDateTime;

public class TicketMock {
    public static Ticket getTicketMock() {

        Participant participant = ParticipantMock.getParticipantMock();
        Event event = EventMock.getEventMock();

        return Ticket.builder()
                .id(new TicketId(participant, event))
                .timeOfSubmission(LocalDateTime.of(2021, 1, 1, 1, 1))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .isValidated(false)
                .registrationSubmission(null)
                .build();
    }
}
