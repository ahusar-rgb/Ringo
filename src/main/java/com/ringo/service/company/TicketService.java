package com.ringo.service.company;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.ringo.auth.JwtService;
import com.ringo.dto.common.TicketCode;
import com.ringo.dto.company.TicketDto;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.TicketMapper;
import com.ringo.model.company.*;
import com.ringo.model.security.User;
import com.ringo.repository.EventRepository;
import com.ringo.repository.OrganisationRepository;
import com.ringo.repository.ParticipantRepository;
import com.ringo.repository.TicketRepository;
import com.ringo.service.common.EmailSender;
import com.ringo.service.common.QrCodeGenerator;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketService {

    private final TicketRepository repository;
    private final TicketMapper mapper;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final OrganisationRepository organisationRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final EmailSender emailSender;
    private final QrCodeGenerator qrCodeGenerator;

    public TicketDto issueTicket(Event event, Participant participant) {

        if(repository.existsById(new TicketId(participant, event)))
            throw new UserException("The user is already registered for this event");

        Ticket ticket = Ticket.builder()
                .id(new TicketId(participant, event))
                .timeOfSubmission(LocalDateTime.now())
                .expiryDate(event.getEndTime())
                .isValidated(false)
                .build();

        TicketDto ticketDto = mapper.toDto(repository.save(ticket));
        ticketDto.setTicketCode(jwtService.generateTicketCode(ticket));

        BufferedImage qrCode = qrCodeGenerator.generateQrCode(ticketDto.getTicketCode());

        emailSender.sendTicket(
                participant.getEmail(),
                "Ticket for event %s".formatted(event.getName()),
                "Here is your ticket!",
                qrCode);

        return ticketDto;
    }

    public TicketDto scanTicket(TicketCode ticketCode) {
        DecodedJWT jwt = jwtService.verifyTicketCode(ticketCode.getTicketCode());

        Event event = eventRepository.findById(jwt.getClaim("event").asLong())
                .orElseThrow(() -> new UserException("Event not found"));

        if(!isUserHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        Participant participant = participantRepository.findById(jwt.getClaim("participant").asLong())
                .orElseThrow(() -> new UserException("Participant not found"));

        Ticket ticket = repository.findById(new TicketId(participant, event))
                .orElseThrow(() -> new UserException("Ticket not found"));

        if(ticket.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new UserException("Ticket expired");

        return mapper.toDto(ticket);
    }

    public TicketDto validateTicket(TicketCode ticketCode) {

        DecodedJWT jwt = jwtService.verifyTicketCode(ticketCode.getTicketCode());

        Event event = eventRepository.findById(jwt.getClaim("event").asLong())
                .orElseThrow(() -> new UserException("Event not found"));

        if(!isUserHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        Participant participant = participantRepository.findById(jwt.getClaim("participant").asLong())
                .orElseThrow(() -> new UserException("Participant not found"));

        Ticket ticket = repository.findById(new TicketId(participant, event))
                .orElseThrow(() -> new UserException("Ticket not found"));

        if(ticket.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new UserException("Ticket expired");

        ticket.setIsValidated(true);
        return mapper.toDto(repository.save(ticket));
    }

    public List<TicketDto> findByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new UserException("Event not found"));

        if(!isUserHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        return mapper.toDtos(repository.findAllByEventId(event.getId()));
    }

    private boolean isUserHostOfEvent(Event event) {
        User user = userService.getCurrentUserAsEntity();
        Optional<Organisation> organisation = organisationRepository.findById(user.getId());
        return organisation.filter(value -> event.getHost().equals(value)).isPresent();
    }
}