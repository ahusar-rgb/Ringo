package com.ringo.service.company.event;

import com.ringo.dto.company.EventResponseDto;
import com.ringo.dto.company.EventSmallDto;
import com.ringo.dto.company.TicketDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventMapper;
import com.ringo.mapper.company.EventPersonalizedMapper;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.model.form.RegistrationSubmission;
import com.ringo.repository.EventRepository;
import com.ringo.repository.ParticipantRepository;
import com.ringo.service.company.ParticipantService;
import com.ringo.service.company.RegistrationValidator;
import com.ringo.service.company.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventInteractionService {

    private final ParticipantService participantService;
    private final EventRepository repository;
    private final TicketService ticketService;
    private final EventMapper mapper;
    private final EventPersonalizedMapper personalizedMapper;
    private final RegistrationValidator validator;
    private final ParticipantRepository participantRepository;

    public TicketDto joinEvent(Long id, RegistrationSubmission submission) {
        Participant participant = participantService.getFullUser();
        Event event = repository.findActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(event.getPeopleCount() >= event.getCapacity())
            throw new UserException("Event is already full");

        validator.throwIfSubmissionInvalid(event.getRegistrationForm(), submission);

        TicketDto ticketDto = ticketService.issueTicket(event, participant, submission);

        event.setPeopleCount(event.getPeopleCount() + 1);
        event = repository.save(event);

        ticketDto.setEvent(mapper.toDtoSmall(event));
        return ticketDto;
    }

    public TicketDto leaveEvent(Long id) {
        Participant participant = participantService.getFullUser();
        Event event = repository.findActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        TicketDto ticketDto = ticketService.cancelTicket(event, participant);
        event.setPeopleCount(event.getPeopleCount() - 1);
        event = repository.save(event);

        ticketDto.setEvent(mapper.toDtoSmall(event));
        return ticketDto;
    }

    public EventResponseDto saveEvent(Long id) {
        Participant participant = participantService.getFullUser();
        Event event = repository.findFullActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(participant.getSavedEvents().contains(event))
            throw new UserException("Event [id: %d] is already saved".formatted(id));

        participant.getSavedEvents().add(event);
        participantRepository.save(participant);

        event.setPeopleSaved(event.getPeopleSaved() + 1);
        event = repository.save(event);

        return personalizedMapper.toPersonalizedDto(event);
    }

    public EventResponseDto unsaveEvent(Long id) {
        Participant participant = participantService.getFullUser();
        Event event = repository.findFullActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(!participant.getSavedEvents().remove(event))
            throw new UserException("Event [id: %d] is not saved".formatted(id));

        participantRepository.save(participant);
        event.setPeopleSaved(event.getPeopleSaved() - 1);
        event = repository.save(event);

        return personalizedMapper.toPersonalizedDto(event);
    }

    public List<EventSmallDto> getSavedEvents() {
        Participant participant = participantService.getFullUser();
        return mapper.toDtoSmallList(participant.getSavedEvents().stream().toList());
    }

    public TicketDto getTicketForEvent(Long id) {
        Event event = repository.findActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        Participant participant = participantService.getFullUser();

        TicketDto ticketDto = ticketService.getTicketWithCode(event, participant);
        ticketDto.setEvent(mapper.toDtoSmall(event));
        return ticketDto;
    }
}
