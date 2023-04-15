package com.ringo.service.company;


import com.ringo.dto.company.TicketDto;
import com.ringo.mapper.company.EventMapper;
import com.ringo.repository.EventRepository;
import com.ringo.repository.TicketRepository;
import com.ringo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    public TicketDto participate(Long eventId, Long participantId) {
//        Event event = eventRepository.findById(eventId).orElseThrow(
//                () -> new NotFoundException("Event#" + eventId + "not found")
//        );
//
//        Participant participant = userRepository.findById(userId).orElseThrow(
//                () -> new NotFoundException("User#" + userId + "not found")
//        );
//
//        event.addParticipant(participant);
//        eventRepository.save(event);
//
//        return TicketDto.builder()
//                .user(participantMapper.toDto(participant)))
//                .event(eventMapper.toDto(event)))
//                .timeOfSubmission(LocalDateTime.now())
//                .expiryDate(LocalDateTime.now().plusDays(1))
//                .isValidated(false)
//                .build();
        return null;
    }
}
