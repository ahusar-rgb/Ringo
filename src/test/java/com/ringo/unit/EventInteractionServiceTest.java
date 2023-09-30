package com.ringo.unit;

import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.dto.company.response.EventSmallDto;
import com.ringo.dto.company.response.TicketDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.*;
import com.ringo.mock.model.EventMock;
import com.ringo.mock.model.ParticipantMock;
import com.ringo.mock.model.RegistrationSubmissionMock;
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
import com.ringo.service.company.event.EventInteractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventInteractionServiceTest {

    @Mock
    private ParticipantService participantService;
    @Mock
    private EventRepository repository;
    @Mock
    private TicketService ticketService;
    @Mock
    private JoiningIntentService joiningIntentService;
    @Spy
    private EventMapper eventMapper;
    @Mock
    private RegistrationValidator validator;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private EventInteractionService eventInteractionService;
    @Captor
    private ArgumentCaptor<Event> eventCaptor;
    @Captor
    private ArgumentCaptor<Participant> participantCaptor;


    @BeforeEach
    void init() {
        eventMapper = new EventMapperImpl();
        TicketTypeMapper ticketTypeMapper = new TicketTypeMapperImpl();
        ReflectionTestUtils.setField(ticketTypeMapper, "currencyMapper", new CurrencyMapperImpl());

        ReflectionTestUtils.setField(eventMapper, "eventMainPhotoMapper", new EventMainPhotoMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "ticketTypeMapper", ticketTypeMapper);

        OrganisationMapper organisationMapper = new OrganisationMapperImpl();
        ReflectionTestUtils.setField(organisationMapper, "labelMapper", new LabelMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "organisationMapper", organisationMapper);
        ReflectionTestUtils.setField(eventMapper, "currencyMapper", new CurrencyMapperImpl());

        ReflectionTestUtils.setField(eventInteractionService, "mapper", eventMapper);

        EventPersonalizedMapper personalizedMapper = new EventPersonalizedMapper(
                eventMapper,
                ticketService,
                participantService
        );
        ReflectionTestUtils.setField(eventInteractionService, "personalizedMapper", personalizedMapper);
    }


    @Test
    void saveEvent() {
        //given
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(repository.findFullActiveById(event.getId())).thenReturn(java.util.Optional.of(event));
        when(repository.save(eventCaptor.capture())).thenReturn(event);

        //then
        EventResponseDto dto = eventInteractionService.saveEvent(event.getId());

        verify(participantRepository, times(1)).save(participantCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getPeopleSaved()).isEqualTo(1);
        assertThat(savedEvent).usingRecursiveComparison().ignoringFields("peopleSaved").isEqualTo(event);

        Participant savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getSavedEvents().contains(event)).isTrue();
        assertThat(savedParticipant).usingRecursiveComparison().ignoringFields("savedEvents").isEqualTo(participant);

        assertThat(dto.getIsSaved()).isTrue();
    }

    @Test
    void saveEventNotFound() {
        //given
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(participantService.getFullActiveUser()).thenReturn(participant);

        //then
        assertThatThrownBy(() -> eventInteractionService.saveEvent(event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event [id: %d] not found".formatted(event.getId()));

        verify(participantRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void saveEventAlreadySaved() {
        //given
        Event event = EventMock.getEventMock();
        event.setPeopleSaved(1);
        Participant participant = ParticipantMock.getParticipantMock();
        participant.getSavedEvents().add(event);

        //when
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(repository.findFullActiveById(event.getId())).thenReturn(java.util.Optional.of(event));

        //then
        assertThatThrownBy(() -> eventInteractionService.saveEvent(event.getId()))
                .isInstanceOf(UserException.class)
                .hasMessage("Event [id: %d] is already saved".formatted(event.getId()));

        verify(participantRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void unsaveEvent() {
        //given
        Event event = EventMock.getEventMock();
        event.setPeopleSaved(1);
        Participant participant = ParticipantMock.getParticipantMock();
        participant.getSavedEvents().add(event);

        //when
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(repository.findFullActiveById(event.getId())).thenReturn(java.util.Optional.of(event));
        when(repository.save(eventCaptor.capture())).thenReturn(event);

        //then
        EventResponseDto dto = eventInteractionService.unsaveEvent(event.getId());

        verify(participantRepository, times(1)).save(participantCaptor.capture());
        verify(repository, times(1)).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getPeopleSaved()).isEqualTo(0);
        assertThat(savedEvent).usingRecursiveComparison().ignoringFields("peopleSaved").isEqualTo(event);

        Participant savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getSavedEvents().contains(event)).isFalse();

        assertThat(dto.getIsSaved()).isFalse();
    }

    @Test
    void unsaveEventNotSaved() {
        //given
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(participantService.getFullActiveUser()).thenReturn(participant);

        //then
        assertThatThrownBy(() -> eventInteractionService.unsaveEvent(event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event [id: %d] not found".formatted(event.getId()));

        verify(participantRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void joinFreeEventSuccess() {
        //given
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();
        JoiningIntent joiningIntent = joi.cre

        //when
        when(ticketService.issueTicket(event, event.getTicketTypes().get(0), participant, null)).thenReturn(new TicketDto());
        when(repository.findActiveById(event.getId())).thenReturn(java.util.Optional.of(event));
        when(participantService.getFullActiveUser()).thenReturn(participant);

        //then
        TicketDto ticketDto = eventInteractionService.joinEvent(event.getId(), event.getTicketTypes().get(0).getId(), null);
        assertThat(ticketDto.getEvent()).isEqualTo(eventMapper.toDtoSmall(event));

        verify(repository, times(1)).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getPeopleCount()).isEqualTo(1);
        assertThat(savedEvent).usingRecursiveComparison().ignoringFields("peopleCount").isEqualTo(event);
        assertThat(savedEvent.getTicketTypes().get(0).getPeopleCount()).isEqualTo(1);
    }

    @Test
    void joinPaidEventSuccess() {

    }

    @Test
    void joinEventWithFormSuccess() {
        //given
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();

        //when
        when(ticketService.issueTicket(event, event.getTicketTypes().get(1), participant, submission)).thenReturn(new TicketDto());
        when(repository.findActiveById(event.getId())).thenReturn(java.util.Optional.of(event));
        when(participantService.getFullActiveUser()).thenReturn(participant);

        //then
        TicketDto ticketDto = eventInteractionService.joinEvent(event.getId(), event.getTicketTypes().get(1).getId(), submission);
        assertThat(ticketDto.getEvent()).isEqualTo(eventMapper.toDtoSmall(event));

        verify(repository, times(1)).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getPeopleCount()).isEqualTo(1);
        assertThat(savedEvent).usingRecursiveComparison().ignoringFields("peopleCount").isEqualTo(event);
    }

    @Test
    void joinEventInvalidForm() {
        //given
        Event event = EventMock.getEventMock();
        ParticipantMock.getParticipantMock();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();

        //when
        when(repository.findActiveById(event.getId())).thenReturn(java.util.Optional.of(event));
        doThrow(new UserException("Invalid submission")).when(validator).throwIfSubmissionInvalid(event.getRegistrationForm(), submission);

        //then
        assertThatThrownBy(() -> eventInteractionService.joinEvent(event.getId(), event.getTicketTypes().get(0).getId(), submission))
                .isInstanceOf(UserException.class)
                .hasMessage("Invalid submission");

        verify(repository, never()).save(any());
        verify(ticketService, never()).issueTicket(any(), any(), any(), any());
    }

    @Test
    void joinEventFull() {
        //given
        Event event = EventMock.getEventMock();
        TicketType ticketType = event.getTicketTypes().get(0);
        ticketType.setPeopleCount(100);
        ticketType.setMaxTickets(100);
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();

        //when
        when(repository.findActiveById(event.getId())).thenReturn(java.util.Optional.of(event));

        //then
        assertThatThrownBy(() -> eventInteractionService.joinEvent(event.getId(), ticketType.getId(), submission))
                .isInstanceOf(UserException.class)
                .hasMessage("This ticket type is sold out");

        verify(repository, never()).save(any());
        verify(ticketService, never()).issueTicket(any(), any(), any(), any());
    }

    @Test
    void joinEventTicketTypeNotFound() {
        //given
        Event event = EventMock.getEventMock();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();

        //when
        when(repository.findActiveById(event.getId())).thenReturn(Optional.of(event));

        //then
        assertThatThrownBy(() -> eventInteractionService.joinEvent(event.getId(), 100L, submission))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Ticket type [id: %d] not found".formatted(100L));

        verify(repository, never()).save(any());
        verify(ticketService, never()).issueTicket(any(), any(), any(), any());
    }

    @Test
    void joinEventTicketTypeExpired() {
        //given
        Event event = EventMock.getEventMock();
        event.getTicketTypes().get(0).setSalesStopTime(LocalDateTime.now().minusDays(1));
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();

        //when
        when(repository.findActiveById(event.getId())).thenReturn(Optional.of(event));

        //then
        assertThatThrownBy(() -> eventInteractionService.joinEvent(event.getId(), event.getTicketTypes().get(0).getId(), submission))
                .isInstanceOf(UserException.class)
                .hasMessage("This ticket type is no longer available");

        verify(repository, never()).save(any());
        verify(ticketService, never()).issueTicket(any(), any(), any(), any());
    }

    @Test
    void leaveEventSuccess() {
        //given
        Event event = EventMock.getEventMock();
        event.setPeopleCount(5);
        event.getTicketTypes().get(0).setPeopleCount(1);
        Participant participant = ParticipantMock.getParticipantMock();

        Event changedEvent = EventMock.getEventMock();
        changedEvent.setId(event.getId());
        changedEvent.setPeopleCount(4);
        changedEvent.setHost(event.getHost());

        //when
        when(repository.findActiveById(event.getId())).thenReturn(java.util.Optional.of(event));
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(repository.save(eventCaptor.capture())).thenReturn(changedEvent);
        when(ticketService.cancelTicket(event, participant)).thenReturn(Ticket.builder()
                .ticketType(event.getTicketTypes().get(0))
                .build());

        //then
        EventSmallDto responseDto = eventInteractionService.leaveEvent(event.getId());
        assertThat(responseDto.getPeopleCount()).isEqualTo(4);
        EventSmallDto expectedDto = eventMapper.toDtoSmall(event);
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("peopleCount", "currency").isEqualTo(expectedDto);
        assertThat(responseDto.getCurrency()).usingRecursiveComparison().ignoringFields("id").isEqualTo(expectedDto.getCurrency());



        Event savedEvent = eventCaptor.getValue();
        verify(repository, times(1)).save(savedEvent);
        verify(ticketService, times(1)).cancelTicket(event, participant);


        assertThat(savedEvent.getPeopleCount()).isEqualTo(4);
        assertThat(savedEvent).usingRecursiveComparison().ignoringFields("peopleCount").isEqualTo(event);
        assertThat(savedEvent.getTicketTypes().get(0).getPeopleCount()).isEqualTo(0);
    }
}
