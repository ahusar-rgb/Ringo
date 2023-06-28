package com.ringo.service.company;

import com.ringo.auth.AppleIdTokenService;
import com.ringo.auth.GoogleIdTokenService;
import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.ParticipantMapper;
import com.ringo.mapper.company.UserMapper;
import com.ringo.model.company.Participant;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.ParticipantRepository;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.AbstractUserService;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
public class ParticipantService extends AbstractUserService<ParticipantRequestDto, Participant, ParticipantResponseDto> {

    @Autowired
    private ParticipantMapper mapper;
    @Autowired
    private UserService userService;
    @Autowired
    private GoogleIdTokenService googleIdTokenService;
    @Autowired
    private AppleIdTokenService appleIdTokenService;

    private final ParticipantRepository repository;

    public ParticipantService(UserRepository userRepository, ParticipantRepository repository,
                              PasswordEncoder passwordEncoder, UserMapper mapper) {
        super(userRepository, repository, passwordEncoder, mapper);
        this.repository = repository;
    }

    public ParticipantResponseDto findById(Long id) {
        log.info("findParticipantById: {}", id);
        return mapper.toDto(repository.findById(id).orElseThrow(
                () -> new NotFoundException("Participant [id: %d] not found".formatted(id))
        ));
    }

    public ParticipantResponseDto findCurrentParticipant() {
        log.info("findCurrentParticipant");
        Participant participant = getCurrentUser();
        ParticipantResponseDto dto = mapper.toDto(participant);
        dto.setEmail(participant.getEmail());
        return dto;
    }

    public ParticipantResponseDto activate() {
       Participant participant = getCurrentUser();

        log.info("activateParticipant: {}", participant.getEmail());

        if(participant.getIsActive()) {
            throw new UserException("Participant [email: %s] is already active".formatted(participant.getEmail()));
        }
        throwIfNotFullyFilled(participant);

        participant.setIsActive(true);
        ParticipantResponseDto dto = mapper.toDto(repository.save(participant));
        dto.setEmail(participant.getEmail());
        return dto;
    }

    public ParticipantResponseDto save(ParticipantRequestDto dto) {
        log.info("saveParticipant: {}", dto);
        User user = userService.build(dto);
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
        Participant participant = getCurrentUser();

        mapper.partialUpdate(participant, dto);
        participant.setUpdatedAt(LocalDateTime.now());
        ParticipantResponseDto responseDto = mapper.toDto(repository.save(participant));
        responseDto.setEmail(participant.getEmail());
        return responseDto;
    }

    public ParticipantResponseDto signUpGoogle(String token) {
        User user = googleIdTokenService.getUserFromToken(token);
        Participant participant = Participant.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();

        return saveEmptyParticipant(participant);
    }

    public ParticipantResponseDto signUpApple(String token) {
        User user = appleIdTokenService.getUserFromToken(token);
        Participant participant = Participant.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();

        return saveEmptyParticipant(participant);
    }

    private ParticipantResponseDto saveEmptyParticipant(Participant participant) {
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

    @Override
    public void throwIfNotFullyFilled(Participant participant) {
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

    @Override
    public ParticipantResponseDto createFromUser(ParticipantRequestDto dto, User user) {
        Participant participant = mapper.fromUser(user);
        mapper.partialUpdate(participant, dto);

        throwIfNotFullyFilled(participant);
        participant.setRole(Role.ROLE_PARTICIPANT);

        ParticipantResponseDto saved = mapper.toDto(repository.save(participant));
        saved.setEmail(participant.getEmail());
        return saved;
    }

    @Override
    public ParticipantResponseDto partialUpdate(ParticipantRequestDto dto) {
        Participant participant = getCurrentUser();
        mapper.partialUpdate(participant, dto);

        if(repository.findByUsername(dto.getUsername()).isPresent()) {
            throw new UserException("Participant with [username: " + dto.getUsername() + "] already exists");
        }

        throwIfNotFullyFilled(participant);
        participant.setUpdatedAt(LocalDateTime.now());

        ParticipantResponseDto responseDto = mapper.toDto(repository.save(participant));
        responseDto.setEmail(participant.getEmail());
        return responseDto;
    }
}
