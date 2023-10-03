package com.ringo.service.company;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.ringo.auth.JwtService;
import com.ringo.dto.common.TicketCode;
import com.ringo.dto.company.response.TicketDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventMapper;
import com.ringo.mapper.company.TicketMapper;
import com.ringo.model.company.*;
import com.ringo.model.form.RegistrationSubmission;
import com.ringo.model.payment.JoiningIntent;
import com.ringo.repository.company.EventRepository;
import com.ringo.repository.company.ParticipantRepository;
import com.ringo.repository.company.TicketRepository;
import com.ringo.service.common.EmailSender;
import com.ringo.service.common.QrCodeGenerator;
import com.ringo.service.time.Time;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.util.List;

import static java.lang.Float.compare;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketService {

    private final TicketRepository repository;
    private final TicketMapper mapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ParticipantService participantService;
    private final ParticipantRepository participantRepository;
    private final OrganisationService organisationService;
    private final JwtService jwtService;
    private final EmailSender emailSender;
    private final QrCodeGenerator qrCodeGenerator;
    private final JoiningIntentService joinIntentService;

     public TicketDto issueTicket(JoiningIntent joiningIntent) {
         Event event = joiningIntent.getEvent();
         Participant participant = joiningIntent.getParticipant();
         RegistrationSubmission submission = joiningIntent.getRegistrationSubmission();
         TicketType ticketType = joiningIntent.getTicketType();

        throwIfTicketExists(event, participant);

        Ticket ticket = Ticket.builder()
                .id(new TicketId(participant, event))
                .timeOfSubmission(Time.getLocalUTC())
                .expiryDate(event.getEndTime().plusDays(3))
                .isValidated(false)
                .isPaid(ticketType != null && ticketType.getPrice() != null && compare(ticketType.getPrice(), 0f) != 0)
                .registrationSubmission(submission)
                .ticketType(ticketType)
                .build();

        TicketDto ticketDto = mapper.toDto(repository.save(ticket));
        ticketDto.setTicketCode(jwtService.generateTicketCode(ticket));
        ticketDto.setEvent(eventMapper.toDtoSmall(event));

        event.setPeopleCount(event.getPeopleCount() + 1);
        if(ticketType != null)
            ticketType.setPeopleCount(ticketType.getPeopleCount() + 1);
        eventRepository.save(event);


        BufferedImage qrCode = qrCodeGenerator.generateQrCode(ticketDto.getTicketCode());

        try {
            emailSender.sendTicket(ticket, qrCode);
        } catch (Exception e) {
            log.error("Failed to send ticket to user", e);
        }

        return ticketDto;
    }

    public void throwIfTicketExists(Event event, Participant participant) {
        if(repository.existsById(new TicketId(participant, event)))
            throw new UserException("The user is already registered for this event");
    }

    public boolean ticketExists(Event event, Participant participant) {
        return repository.existsById(new TicketId(participant, event));
    }

    public TicketDto scanTicket(TicketCode ticketCode) {
        Ticket ticket = getTicketFromCode(ticketCode);
        return mapper.toDto(ticket);
    }

    public Ticket getTicketFromCode(TicketCode ticketCode) {
        DecodedJWT jwt = jwtService.verifyTicketCode(ticketCode.getTicketCode());

        Event event = eventRepository.findById(jwt.getClaim("event").asLong())
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if(notHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        Participant participant = participantRepository.findById(jwt.getClaim("participant").asLong())
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        Ticket ticket = repository.findById(new TicketId(participant, event))
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        if(ticket.getExpiryDate().isBefore(Time.getLocalUTC()))
            throw new UserException("Ticket expired");

        return ticket;
    }

    public void validateTicket(TicketCode ticketCode) {
        Ticket ticket = getTicketFromCode(ticketCode);
        ticket.setIsValidated(true);

        repository.save(ticket);
    }

    public List<TicketDto> findByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if(notHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        return mapper.toDtoList(repository.findAllByEventId(event.getId()));
    }

    private boolean notHostOfEvent(Event event) {
        Organisation organisation = organisationService.getFullActiveUser();
        return !event.getHost().getId().equals(organisation.getId());
    }

    public List<TicketDto> getMyTickets() {
        Participant participant = participantService.getFullActiveUser();

        List<Ticket> tickets = repository.findAllByParticipantId(participant.getId());

        return tickets.stream().map(ticket -> {
            if(eventRepository.findById(ticket.getId().getEvent().getId()).isEmpty())
                throw new NotFoundException("Event not found");

            TicketDto ticketDto = mapper.toDto(ticket);
            ticketDto.setEvent(eventMapper.toDtoSmall(ticket.getId().getEvent()));
            ticketDto.setTicketCode(jwtService.generateTicketCode(ticket));
            return ticketDto;
        }).toList();
    }

    public Ticket cancelTicket(Event event, Participant participant) {
        Ticket ticket = repository.findById(new TicketId(participant, event))
                .orElseThrow(() -> new UserException("The user is not registered for this event"));

        if(ticket.getIsPaid()) {
            if (ticket.getExpiryDate().isAfter(Time.getLocalUTC())) //not expired
                throw new UserException("Can't cancel a ticket for a paid event");
        }

        repository.delete(ticket);
        return ticket;
    }

    public TicketDto getTicketWithCode(Event event, Participant participant) {
        Ticket ticket = repository.findById(new TicketId(participant, event))
                .orElseThrow(() -> new UserException("The user is not registered for this event"));

        TicketDto ticketDto = mapper.toDto(ticket);
        ticketDto.setTicketCode(jwtService.generateTicketCode(ticket));
        return ticketDto;
    }

    public void issueToUserByEmail(Long eventId, Long ticketTypeId, String email) {
        Participant participant = participantRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if(notHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        TicketType ticketType = event.getTicketTypes().stream()
                .filter(type -> type.getId().equals(ticketTypeId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Ticket type not found"));

        issueTicket(joinIntentService.createNoPayment(participant, event, ticketType, null));
    }
}