package com.ringo.service.company;

import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.ParticipantMapper;
import com.ringo.model.company.Participant;
import com.ringo.model.security.Role;
import com.ringo.repository.ParticipantRepository;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository repository;
    private final ParticipantMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public ParticipantResponseDto findById(Long id) {
        log.info("findParticipantById: {}", id);
        ParticipantResponseDto dto = mapper.toDto(repository.findById(id).orElseThrow(
                () -> new UserException("Participant [id: %d] not found".formatted(id))
        ));

        dto.setEmail(null);
        return dto;
    }

    public ParticipantResponseDto findCurrentParticipant() {
        log.info("findCurrentParticipant");
        return mapper.toDto(repository.findById(userService.getCurrentUserAsEntity().getId()).orElseThrow(
                () -> new UserException("Authorized user is not a participant")
        ));
    }

    public ParticipantResponseDto save(ParticipantRequestDto dto) {
        log.info("saveParticipant: {}", dto);
        Participant participant = mapper.toEntity(dto);

        if (repository.findByEmail(participant.getEmail()).isPresent()) {
            throw new UserException("Participant with [email: " + participant.getEmail() + "] already exists");
        }
        if (repository.findByUsername(participant.getUsername()).isPresent()) {
            throw new UserException("Participant with [username: " + participant.getUsername() + "] already exists");
        }

        participant.setRole(Role.ROLE_PARTICIPANT);
        participant.setPassword(passwordEncoder.encode(dto.getPassword()));

        return mapper.toDto(repository.save(participant));
    }

    public ParticipantResponseDto update(ParticipantRequestDto dto) {
        log.info("updateParticipant: {}", dto);
        Participant participant = repository.findById(userService.getCurrentUserAsEntity().getId()).orElseThrow(
                () -> new UserException("Authorized user is not a participant")
        );

        mapper.partialUpdate(participant, dto);
        return mapper.toDto(repository.save(participant));
    }
}
