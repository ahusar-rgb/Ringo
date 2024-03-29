package com.ringo.service.company.event;


import com.ringo.dto.company.JoinEventResult;
import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.dto.company.response.EventSmallDto;
import com.ringo.dto.company.response.TicketDto;
import com.ringo.exception.InternalException;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventMapper;
import com.ringo.mapper.company.EventPersonalizedMapper;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.model.company.Ticket;
import com.ringo.model.company.TicketType;
import com.ringo.model.form.RegistrationSubmission;
import com.ringo.model.payment.JoiningIntent;
import com.ringo.repository.company.EventRepository;
import com.ringo.repository.company.ParticipantRepository;
import com.ringo.repository.company.TicketTypeRepository;
import com.ringo.service.company.JoiningIntentService;
import com.ringo.service.company.ParticipantService;
import com.ringo.service.company.RegistrationValidator;
import com.ringo.service.company.TicketService;
import com.ringo.service.time.Time;
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
    private final JoiningIntentService joiningIntentService;
    private final TicketTypeRepository ticketTypeRepository;

    public JoinEventResult joinEvent(Long id, Long ticketTypeId, RegistrationSubmission submission) {
        Event event = repository.findActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        Participant participant = participantService.getFullActiveUser();
        validator.throwIfSubmissionInvalid(event.getRegistrationForm(), submission);

        if(event.getTicketTypes().isEmpty()) {
            if(ticketTypeId == null) {
                return JoinEventResult.fromTicket(getFreeTicket(participant, event, null, submission));
            } else {
                throw new UserException("This event does not have tickets");
            }
        } else {
            if(ticketTypeId == null)
                throw new UserException("Ticket type id is required");
        }

        TicketType ticketType = event.getTicketTypes().stream()
                .filter(t -> t.getId().equals(ticketTypeId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Ticket type [id: %d] not found".formatted(ticketTypeId)));

        if(ticketType.getPrice() == 0)
            return JoinEventResult.fromTicket(getFreeTicket(participant, event, ticketType, submission));


        JoiningIntent joiningIntent = createJoiningIntent(participant, event, ticketType, submission);
        return JoinEventResult.fromPaymentIntent(
                joiningIntent.getPaymentIntentClientSecret(),
                event.getHost().getStripeAccountId()
        );
    }

    public EventSmallDto leaveEvent(Long id) {
        Participant participant = participantService.getFullActiveUser();
        Event event = repository.findActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        Ticket ticket = ticketService.cancelTicket(event, participant);

        event.setPeopleCount(event.getPeopleCount() - 1);
        event = repository.save(event);

        TicketType ticketType = ticket.getTicketType();
        if(ticketType != null) {
            ticketType.setPeopleCount(ticketType.getPeopleCount() - 1);
            ticketTypeRepository.save(ticketType);
        }

        return mapper.toDtoSmall(event);
    }

    public EventResponseDto saveEvent(Long id) {
        Participant participant = participantService.getFullActiveUser();
        Event event = repository.findFullActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(participant.getSavedEvents().contains(event))
            throw new UserException("Event [id: %d] is already saved".formatted(id));

        participant.getSavedEvents().add(event);
        participantRepository.save(participant);

        event.setPeopleSaved(event.getPeopleSaved() + 1);
        repository.save(event);

        return personalizedMapper.toPersonalizedDto(event);
    }

    public EventResponseDto unsaveEvent(Long id) {
        Participant participant = participantService.getFullActiveUser();
        Event event = repository.findFullActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(!participant.getSavedEvents().remove(event))
            throw new UserException("Event [id: %d] is not saved".formatted(id));

        participantRepository.save(participant);
        event.setPeopleSaved(event.getPeopleSaved() - 1);
        repository.save(event);

        return personalizedMapper.toPersonalizedDto(event);
    }

    public List<EventSmallDto> getSavedEvents() {
        Participant participant = participantService.getFullActiveUser();
        return mapper.toDtoSmallList(participant.getSavedEvents().stream().filter(Event::getIsActive).toList());
    }

    public TicketDto getTicketForEvent(Long id) {
        Event event = repository.findActiveById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        Participant participant = participantService.getFullActiveUser();

        TicketDto ticketDto = ticketService.getTicketWithCode(event, participant);
        ticketDto.setEvent(mapper.toDtoSmall(event));
        ticketDto.setRegistrationForm(event.getRegistrationForm());
        return ticketDto;
    }

    private TicketDto getFreeTicket(Participant participant, Event event, TicketType ticketType, RegistrationSubmission submission) {
        return ticketService.issueTicket(joiningIntentService.createNoPayment(participant, event, ticketType, submission));
    }

    private JoiningIntent createJoiningIntent(Participant participant, Event event, TicketType ticketType, RegistrationSubmission submission) {
        if(ticketType.getMaxTickets() != null && ticketType.getPeopleCount() >= ticketType.getMaxTickets())
            throw new UserException("This ticket type is sold out");

        if(ticketType.getSalesStopTime() != null && ticketType.getSalesStopTime().isBefore(Time.getLocalUTC()))
            throw new UserException("This ticket type is no longer available");

        ticketType.setPeopleCount(ticketType.getPeopleCount() + 1);
        if(ticketType.getPrice() == 0)
            throw new InternalException("Ticket type price is 0");

        return joiningIntentService.create(participant, event, ticketType, submission);
    }
}
