package com.ringo.unit;

import com.ringo.dto.company.EventResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.mapper.company.*;
import com.ringo.mock.model.EventMock;
import com.ringo.mock.model.OrganisationMock;
import com.ringo.mock.model.ParticipantMock;
import com.ringo.model.company.Event;
import com.ringo.model.company.Organisation;
import com.ringo.model.company.Participant;
import com.ringo.repository.EventRepository;
import com.ringo.service.company.OrganisationService;
import com.ringo.service.company.ParticipantService;
import com.ringo.service.company.TicketService;
import com.ringo.service.company.event.EventSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventSearchServiceTest {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private OrganisationService organisationService;
    @InjectMocks
    private EventSearchService eventSearchService;
    @Spy
    private EventMapper eventMapper;
    private EventPersonalizedMapper eventPersonalizedMapper;
    @Mock
    private ParticipantService participantService;
    @Mock
    private TicketService ticketService;

    @BeforeEach
    void init() {
        eventMapper = new EventMapperImpl();

        ReflectionTestUtils.setField(eventMapper, "eventMainPhotoMapper", new EventMainPhotoMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "currencyMapper", new CurrencyMapperImpl());

        OrganisationMapper organisationMapper = new OrganisationMapperImpl();
        ReflectionTestUtils.setField(organisationMapper, "labelMapper", new LabelMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "organisationMapper", organisationMapper);

        ReflectionTestUtils.setField(eventSearchService, "mapper", eventMapper);

        eventPersonalizedMapper = new EventPersonalizedMapper(
                eventMapper,
                ticketService,
                participantService
        );
        ReflectionTestUtils.setField(eventSearchService, "personalizedMapper", eventPersonalizedMapper);
    }

    @Test
    void findByIdNotActive() {
        //given
        Event event = EventMock.getEventMock();
        event.setIsActive(false);
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullActiveUser()).thenReturn(organisation);
        when(participantService.getFullActiveUser()).thenThrow(new NotFoundException("User is not found"));

        //then
        EventResponseDto responseDto = eventSearchService.findById(event.getId());
        assertThat(responseDto).isEqualTo(eventMapper.toDtoDetails(event));
        assert responseDto.getId().equals(event.getId());
    }

    @Test
    void findByIdActiveTicketExistsAndSaved() {
        //given
        Event event = EventMock.getEventMock();
        event.setIsActive(true);

        Participant participant = ParticipantMock.getParticipantMock();
        participant.setSavedEvents(Set.of(event));

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullActiveUser()).thenThrow(new NotFoundException("User is not found"));
        when(ticketService.ticketExists(event, participant)).thenReturn(true);
        when(participantService.getFullActiveUser()).thenReturn(participant);

        //then
        EventResponseDto responseDto = eventSearchService.findById(event.getId());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields(
                "isRegistered", "isSaved"
        ).isEqualTo(eventMapper.toDtoDetails(event));
        assertThat(responseDto.getIsSaved()).isTrue();
        assertThat(responseDto.getIsRegistered()).isTrue();
    }

    @Test
    void findByIdActiveNoTicketAndNotSaved() {
        //given
        Event event = EventMock.getEventMock();
        event.setIsActive(true);

        Participant participant = ParticipantMock.getParticipantMock();
        participant.setSavedEvents(Set.of());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullActiveUser()).thenThrow(new NotFoundException("User is not found"));
        when(ticketService.ticketExists(event, participant)).thenReturn(false);
        when(participantService.getFullActiveUser()).thenReturn(participant);

        //then
        EventResponseDto responseDto = eventSearchService.findById(event.getId());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields(
                "isRegistered", "isSaved"
        ).isEqualTo(eventMapper.toDtoDetails(event));
        assertThat(responseDto.getIsSaved()).isFalse();
        assertThat(responseDto.getIsRegistered()).isFalse();
    }
}
