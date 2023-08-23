package com.ringo.unit;

import com.ringo.dto.company.EventResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.*;
import com.ringo.mock.model.EventMock;
import com.ringo.mock.model.ParticipantMock;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.repository.EventRepository;
import com.ringo.repository.ParticipantRepository;
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
    @Spy
    private EventMapper eventMapper;
    @Mock
    private EventPersonalizedMapper eventPersonalizedMapper;
    @Mock
    private RegistrationValidator validator;
    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private EventInteractionService eventInteractionService;
    @Captor
    private ArgumentCaptor<Event> eventCaptor;
    @Captor
    private ArgumentCaptor<Participant> participantCaptor;


    @BeforeEach
    void init() {
        eventMapper = new EventMapperImpl();

        ReflectionTestUtils.setField(eventMapper, "eventMainPhotoMapper", new EventMainPhotoMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "currencyMapper", new CurrencyMapperImpl());

        OrganisationMapper organisationMapper = new OrganisationMapperImpl();
        ReflectionTestUtils.setField(organisationMapper, "labelMapper", new LabelMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "organisationMapper", organisationMapper);

        ReflectionTestUtils.setField(eventInteractionService, "mapper", eventMapper);

        eventPersonalizedMapper = new EventPersonalizedMapper(
                eventMapper,
                ticketService,
                participantService
        );
        ReflectionTestUtils.setField(eventInteractionService, "personalizedMapper", eventPersonalizedMapper);
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
}
