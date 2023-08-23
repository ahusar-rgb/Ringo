package com.ringo.service.company;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.ringo.auth.JwtService;
import com.ringo.dto.common.TicketCode;
import com.ringo.dto.company.TicketDto;
import com.ringo.exception.InternalException;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventMapper;
import com.ringo.mapper.company.TicketMapper;
import com.ringo.model.company.*;
import com.ringo.model.form.RegistrationSubmission;
import com.ringo.repository.EventRepository;
import com.ringo.repository.ParticipantRepository;
import com.ringo.repository.TicketRepository;
import com.ringo.service.common.EmailSender;
import com.ringo.service.common.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

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

    public TicketDto issueTicket(Event event, Participant participant, RegistrationSubmission submission) {

        throwIfTicketExists(event, participant);

        Ticket ticket = Ticket.builder()
                .id(new TicketId(participant, event))
                .timeOfSubmission(LocalDateTime.now())
                .expiryDate(event.getEndTime())
                .isValidated(false)
                .registrationSubmission(submission)
                .build();

        TicketDto ticketDto = mapper.toDto(repository.save(ticket));
        ticketDto.setTicketCode(jwtService.generateTicketCode(ticket));

        BufferedImage qrCode = qrCodeGenerator.generateQrCode(ticketDto.getTicketCode());

        try {
            emailSender.sendTicket(ticket, qrCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalException("Failed to send the ticket by email");
        }

        return ticketDto;
    }

    public byte[] getTicketQrCode(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id %d not found".formatted(eventId)));

        Participant participant = participantService.getFullUser();

        throwIfTicketExists(event, participant);

        Ticket ticket = Ticket.builder()
                .id(new TicketId(participant, event))
                .timeOfSubmission(LocalDateTime.now())
                .expiryDate(event.getEndTime())
                .isValidated(false)
                .build();

        TicketDto ticketDto = mapper.toDto(repository.save(ticket));
        ticketDto.setTicketCode(jwtService.generateTicketCode(ticket));

        BufferedImage image = qrCodeGenerator.generateQrCode(ticketDto.getTicketCode());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", stream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalException("Failed to generate QR code");
        }
        return stream.toByteArray();
    }

    public void throwIfTicketExists(Event event, Participant participant) {
        if(repository.existsById(new TicketId(participant, event)))
            throw new UserException("The user is already registered for this event");
    }

    public boolean ticketExists(Event event, Participant participant) {
        return repository.existsById(new TicketId(participant, event));
    }

    public TicketDto scanTicket(TicketCode ticketCode) {
        return mapper.toDto(getTicketFromCode(ticketCode));
    }

    public Ticket getTicketFromCode(TicketCode ticketCode) {
        DecodedJWT jwt = jwtService.verifyTicketCode(ticketCode.getTicketCode());

        Event event = eventRepository.findById(jwt.getClaim("event").asLong())
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if(!isUserHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        Participant participant = participantRepository.findById(jwt.getClaim("participant").asLong())
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        Ticket ticket = repository.findById(new TicketId(participant, event))
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        if(ticket.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new UserException("Ticket expired");

        return ticket;
    }

    public TicketDto validateTicket(TicketCode ticketCode) {
        Ticket ticket = getTicketFromCode(ticketCode);
        ticket.setIsValidated(true);

        return mapper.toDto(repository.save(ticket));
    }

    public List<TicketDto> findByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if(!isUserHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        return mapper.toDtoList(repository.findAllByEventId(event.getId()));
    }

    private boolean isUserHostOfEvent(Event event) {
        Organisation organisation = organisationService.getFullUser();
        return event.getHost().getId().equals(organisation.getId());
    }

    public List<TicketDto> getMyTickets() {
        Participant participant = participantService.getFullUser();

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

    public TicketDto cancelTicket(Event event, Participant participant) {
        Ticket ticket = repository.findById(new TicketId(participant, event))
                .orElseThrow(() -> new UserException("The user is not registered for this event"));

        repository.delete(ticket);
        return mapper.toDto(ticket);
    }

    public TicketDto getTicketWithCode(Event event, Participant participant) {
        Ticket ticket = repository.findById(new TicketId(participant, event))
                .orElseThrow(() -> new UserException("The user is not registered for this event"));

        TicketDto ticketDto = mapper.toDto(ticket);
        ticketDto.setTicketCode(jwtService.generateTicketCode(ticket));
        return ticketDto;
    }

    public void issueToUserByEmail(Long eventId, String email) {
        Participant participant = participantRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if(event.getRegistrationForm() != null)
            throw new UserException("Can't issue a ticket to a user for an event with a registration form");

        if(!isUserHostOfEvent(event))
            throw new UserException("Current user is not the host of this event");

        issueTicket(event, participant, null);
    }
}