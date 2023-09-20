package com.ringo.mapper.company;

import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.service.company.ParticipantService;
import com.ringo.service.company.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPersonalizedMapper {

    private final EventMapper eventMapper;
    private final TicketService ticketService;
    private final ParticipantService participantService;

    public EventResponseDto toPersonalizedDto(Event event) {
        EventResponseDto dto = eventMapper.toDtoDetails(event);

        Optional<Participant> participantOptional = participantService.getFullActiveUserOptional();

        if(participantOptional.isEmpty())
            return dto;

        Participant participant = participantOptional.get();
        dto.setIsRegistered(ticketService.ticketExists(event, participant));
        dto.setIsSaved(participant.getSavedEvents().contains(event));
        return dto;
    }
}
