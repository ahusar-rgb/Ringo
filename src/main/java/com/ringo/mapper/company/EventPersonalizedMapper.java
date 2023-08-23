package com.ringo.mapper.company;

import com.ringo.dto.company.EventResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.service.company.ParticipantService;
import com.ringo.service.company.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPersonalizedMapper {

    private final EventMapper eventMapper;
    private final TicketService ticketService;
    private final ParticipantService participantService;

    public EventResponseDto toPersonalizedDto(Event event) {
        EventResponseDto dto = eventMapper.toDtoDetails(event);

        try {
            Participant participant = participantService.getFullActiveUser();

            dto.setIsRegistered(ticketService.ticketExists(event, participant));
            dto.setIsSaved(participant.getSavedEvents().contains(event));
        } catch (NotFoundException | UserException e) {
            return dto;
        }

        return dto;
    }
}
