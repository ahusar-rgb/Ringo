package com.ringo.service.company;

import com.ringo.auth.GoogleIdTokenService;
import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.ParticipantMapper;
import com.ringo.model.company.Participant;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.ParticipantRepository;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository repository;
    private final ParticipantMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final GoogleIdTokenService googleIdTokenService;

    public Participant getCurrentUserAsParticipant() {
        User user = userService.getCurrentUserIfActive();
        return repository.findById(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not aan organisation")
        );
    }

    public ParticipantResponseDto findById(Long id) {
        log.info("findParticipantById: {}", id);
        return mapper.toDto(repository.findById(id).orElseThrow(
                () -> new NotFoundException("Participant [id: %d] not found".formatted(id))
        ));
    }

    public ParticipantResponseDto findCurrentParticipant() {
        log.info("findCurrentParticipant");
        ParticipantResponseDto dto = mapper.toDto(getCurrentUserAsParticipant());
        dto.setEmail(dto.getEmail());
        return dto;
    }

    public ParticipantResponseDto activate() {
       Participant participant = getCurrentUserAsParticipant();

        log.info("activateParticipant: {}", participant.getEmail());

        if(participant.getIsActive()) {
            throw new UserException("Participant [email: %s] is already active".formatted(participant.getEmail()));
        }
        throwIfNotFullyFilled(participant);

        participant.setIsActive(true);
        return mapper.toDto(repository.save(participant));
    }

    public ParticipantResponseDto save(ParticipantRequestDto dto) {
        log.info("saveParticipant: {}", dto);
        User user = userService.create(dto);
        Participant participant = mapper.fromUser(user);
        mapper.partialUpdate(participant, dto);
        throwIfNotFullyFilled(participant);

        participant.setRole(Role.ROLE_PARTICIPANT);
        participant.setCreatedAt(LocalDateTime.now());
        participant.setIsActive(true);

        ParticipantResponseDto saved = mapper.toDto(repository.save(participant));
        saved.setEmail(participant.getEmail());
        return saved;
    }

    public ParticipantResponseDto update(ParticipantRequestDto dto) {
        log.info("updateParticipant: {}", dto);
        Participant participant = getCurrentUserAsParticipant();

        mapper.partialUpdate(participant, dto);
        participant.setUpdatedAt(LocalDateTime.now());
        return mapper.toDto(repository.save(participant));
    }

    public ParticipantResponseDto signUpGoogle(String token) {
        User user = googleIdTokenService.getUserFromToken(token);
        Participant participant = Participant.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();

        if (repository.findByEmailAll(participant.getEmail()).isPresent()) {
            throw new UserException("Participant with [email: " + participant.getEmail() + "] already exists");
        }
        participant.setRole(Role.ROLE_PARTICIPANT);
        participant.setCreatedAt(LocalDateTime.now());
        participant.setIsActive(false);

        ParticipantResponseDto saved = mapper.toDto(repository.save(participant));
        saved.setEmail(participant.getEmail());
        return saved;
    }

    private void throwIfNotFullyFilled(Participant participant) {
        if(participant.getGender() == null) {
            throw new UserException("Gender is not specified");
        }
        if(participant.getDateOfBirth() == null) {
            throw new UserException("Date of birth is not specified");
        }
        if(participant.getName() == null) {
            throw new UserException("Name is not specified");
        }
        if(participant.getUsername() == null) {
            throw new UserException("Username is not specified");
        }
    }
}
