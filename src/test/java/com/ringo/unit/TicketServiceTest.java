package com.ringo.unit;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ringo.auth.JwtService;
import com.ringo.config.ApplicationProperties;
import com.ringo.dto.common.TicketCode;
import com.ringo.dto.company.response.TicketDto;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.*;
import com.ringo.mock.model.*;
import com.ringo.model.company.*;
import com.ringo.model.form.RegistrationSubmission;
import com.ringo.model.payment.JoiningIntent;
import com.ringo.repository.company.EventRepository;
import com.ringo.repository.company.ParticipantRepository;
import com.ringo.repository.company.TicketRepository;
import com.ringo.service.common.EmailSender;
import com.ringo.service.common.QrCodeGenerator;
import com.ringo.service.company.JoiningIntentService;
import com.ringo.service.company.OrganisationService;
import com.ringo.service.company.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {
    @Mock
    private TicketRepository repository;
    @Spy
    private TicketMapper ticketMapper;
    @Spy
    private TicketTypeMapper ticketTypeMapper;
    @Mock
    private EventRepository eventRepository;
    @Spy
    private EventMapper eventMapper;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailSender emailSender;
    @Mock
    private QrCodeGenerator qrCodeGenerator;
    @Mock
    private ApplicationProperties config;
    @InjectMocks
    private TicketService ticketService;
    @InjectMocks
    private JoiningIntentService joinIntentService;
    @Captor
    private ArgumentCaptor<Ticket> ticketCaptor;
    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    @BeforeEach
    void init() {
        eventMapper = new EventMapperImpl();
        ticketTypeMapper = new TicketTypeMapperImpl();
        ReflectionTestUtils.setField(ticketTypeMapper, "currencyMapper", new CurrencyMapperImpl());

        ReflectionTestUtils.setField(eventMapper, "eventMainPhotoMapper", new EventMainPhotoMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "ticketTypeMapper", ticketTypeMapper);

        OrganisationMapper organisationMapper = new OrganisationMapperImpl();
        ReflectionTestUtils.setField(organisationMapper, "labelMapper", new LabelMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "organisationMapper", organisationMapper);
        ReflectionTestUtils.setField(eventMapper, "currencyMapper", new CurrencyMapperImpl());

        ReflectionTestUtils.setField(ticketService, "eventMapper", eventMapper);

        ticketMapper = new TicketMapperImpl();
        ReflectionTestUtils.setField(ticketMapper, "participantMapper", new ParticipantMapperImpl());
        ReflectionTestUtils.setField(ticketMapper, "eventMapper", eventMapper);
        ReflectionTestUtils.setField(ticketService, "mapper", ticketMapper);
        ReflectionTestUtils.setField(ticketMapper, "ticketTypeMapper", ticketTypeMapper);
    }

    @Test
    void issueTicketSuccess() {
        //given
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();
        BufferedImage qrCode = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        String ticketToken = "ticketToken";
        JoiningIntent joiningIntent = JoiningIntent.builder()
                .participant(participant)
                .event(event)
                .registrationSubmission(submission)
                .ticketType(event.getTicketTypes().get(2))
                .build();

        //when
        when(repository.existsById(new TicketId(participant, event))).thenReturn(false);
        when(repository.save(ticketCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateTicketCode(ticketCaptor.capture())).thenReturn(ticketToken);
        when(qrCodeGenerator.generateQrCode(ticketToken)).thenReturn(qrCode);
        when(config.getTicketLifetimeAfterEventEndInDays()).thenReturn("3");

        //then
        TicketDto ticketDto = ticketService.issueTicket(joiningIntent);

        assertThat(ticketCaptor.getAllValues().get(1)).isEqualTo(ticketCaptor.getAllValues().get(0));

        Ticket ticket = ticketCaptor.getAllValues().get(0);
        assertThat(ticket.getId().getEvent()).isEqualTo(event);
        assertThat(ticket.getId().getParticipant()).isEqualTo(participant);
        assertThat(ticket.getTimeOfSubmission()).isNotNull();
        assertThat(ticket.getExpiryDate()).isEqualTo(event.getEndTime().plusDays(3));
        assertThat(ticket.getIsValidated()).isFalse();
        assertThat(ticket.getIsPaid()).isFalse();
        assertThat(ticket.getRegistrationSubmission()).isEqualTo(submission);

        verify(emailSender, times(1)).sendTicket(ticket, qrCode);

        assertThat(ticketDto.getTicketCode()).isEqualTo(ticketToken);
        assertThat(ticketDto).usingRecursiveComparison().ignoringFields("ticketCode", "event.peopleCount", "ticketType.peopleCount").isEqualTo(ticketMapper.toDto(ticket));
    }

    @Test
    void issueTicketAlreadyExists() {
        //given
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();
        JoiningIntent joiningIntent = JoiningIntent.builder()
                .participant(participant)
                .event(event)
                .registrationSubmission(submission)
                .build();

        //when
        when(repository.existsById(new TicketId(participant, event))).thenReturn(true);

        //then
        assertThatThrownBy(() -> ticketService.issueTicket(joiningIntent))
                .isInstanceOf(UserException.class)
                .hasMessage("The user is already registered for this event");
    }

    @Test
    void scanTicketSuccess() {
        //given
        String ticketCode = "ticketCode";
        DecodedJWT jwt = mock(DecodedJWT.class);
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();
        Ticket ticket = TicketMock.getTicketMock();
        ticket.setId(new TicketId(participant, event));
        Organisation organisation = OrganisationMock.getOrganisationMock();
        event.setHost(organisation);

        //when
        when(jwtService.verifyTicketCode(ticketCode)).thenReturn(jwt);
        Claim eventClaim = mock(Claim.class);
        when(jwt.getClaim("event")).thenReturn(eventClaim);
        when(eventClaim.asLong()).thenReturn(1L);

        Claim participantClaim = mock(Claim.class);
        when(jwt.getClaim("participant")).thenReturn(participantClaim);
        when(participantClaim.asLong()).thenReturn(2L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(repository.findById(new TicketId(participant, event))).thenReturn(Optional.of(ticket));
        when(organisationService.getFullActiveUser()).thenReturn(organisation);

        //then
        TicketDto ticketDto = ticketService.scanTicket(new TicketCode(ticketCode));

        assertThat(ticketDto).usingRecursiveComparison().ignoringFields("event").isEqualTo(ticketMapper.toDto(ticket));
        assertThat(ticketDto.getEvent()).isEqualTo(eventMapper.toDtoSmall(event));
    }

    @Test
    void scanTicketNotValid() {
        //given
        String ticketCode = "ticketCode";

        //when
        doThrow(new UserException("Ticket code is not valid")).when(jwtService).verifyTicketCode(ticketCode);

        //then
        assertThatThrownBy(() -> ticketService.scanTicket(new TicketCode(ticketCode)))
                .isInstanceOf(UserException.class)
                .hasMessage("Ticket code is not valid");
    }

    @Test
    void validateTicketSuccess() {
        //given
        String ticketCode = "ticketCode";
        DecodedJWT jwt = mock(DecodedJWT.class);
        Event event = EventMock.getEventMock();
        Participant participant = ParticipantMock.getParticipantMock();
        Ticket ticket = TicketMock.getTicketMock();
        Organisation organisation = OrganisationMock.getOrganisationMock();
        event.setHost(organisation);

        //when
        when(jwtService.verifyTicketCode(ticketCode)).thenReturn(jwt);
        Claim eventClaim = mock(Claim.class);
        when(jwt.getClaim("event")).thenReturn(eventClaim);
        when(eventClaim.asLong()).thenReturn(1L);

        Claim participantClaim = mock(Claim.class);
        when(jwt.getClaim("participant")).thenReturn(participantClaim);
        when(participantClaim.asLong()).thenReturn(2L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(repository.findById(new TicketId(participant, event))).thenReturn(Optional.of(ticket));
        when(organisationService.getFullActiveUser()).thenReturn(organisation);

        //then
        ticketService.validateTicket(new TicketCode(ticketCode));

        verify(repository, times(1)).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getIsValidated()).isTrue();
    }

    @Test
    void validateTicketNotHost() {
        //given
        String ticketCode = "ticketCode";
        DecodedJWT jwt = mock(DecodedJWT.class);
        Event event = EventMock.getEventMock();
        Organisation organisation = OrganisationMock.getOrganisationMock();
        event.setHost(OrganisationMock.getOrganisationMock());

        //when
        when(jwtService.verifyTicketCode(ticketCode)).thenReturn(jwt);
        Claim eventClaim = mock(Claim.class);
        when(jwt.getClaim("event")).thenReturn(eventClaim);
        when(eventClaim.asLong()).thenReturn(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(organisationService.getFullActiveUser()).thenReturn(organisation);

        //then
        assertThatThrownBy(() -> ticketService.validateTicket(new TicketCode(ticketCode)))
                .isInstanceOf(UserException.class)
                .hasMessage("Current user is not the host of this event");
    }
}
