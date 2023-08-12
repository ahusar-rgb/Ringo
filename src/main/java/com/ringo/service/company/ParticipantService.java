package com.ringo.service.company;

import com.ringo.auth.AppleIdService;
import com.ringo.auth.AuthenticationService;
import com.ringo.auth.GoogleIdService;
import com.ringo.auth.JwtService;
import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.ParticipantMapper;
import com.ringo.model.company.Participant;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.ParticipantRepository;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.AbstractUserService;
import com.ringo.service.common.EmailSender;
import com.ringo.service.common.PhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ParticipantService extends AbstractUserService<ParticipantRequestDto, Participant, ParticipantResponseDto> {

    @Autowired
    private GoogleIdService googleIdService;
    @Autowired
    private AppleIdService appleIdService;

    private final ParticipantRepository repository;
    private final ParticipantMapper mapper;

    public ParticipantService(UserRepository userRepository,
                              ParticipantRepository repository,
                              PasswordEncoder passwordEncoder,
                              ParticipantMapper mapper,
                              PhotoService photoService,
                              AuthenticationService authenticationService,
                              EmailSender emailSender,
                              JwtService jwtService) {
        super(userRepository, repository, passwordEncoder, mapper, photoService, authenticationService, emailSender, jwtService);
        this.repository = repository;
        this.mapper = mapper;
    }

    public ParticipantResponseDto findById(Long id) {
        log.info("findParticipantById: {}", id);
        return mapper.toDto(repository.findByIdActive(id).orElseThrow(
                () -> new NotFoundException("Participant [id: %d] not found".formatted(id))
        ));
    }

    public ParticipantResponseDto save(ParticipantRequestDto dto) {
        return save(dto, Role.ROLE_PARTICIPANT);
    }

    public ParticipantResponseDto findCurrentParticipant() {
        log.info("findCurrentParticipant");
        Participant participant = getFullUser();
        ParticipantResponseDto dto = mapper.toDto(participant);
        dto.setEmail(participant.getEmail());
        return dto;
    }

    public ParticipantResponseDto signUpGoogle(String token) {
        return signUpWithIdProvider(token, googleIdService, Role.ROLE_PARTICIPANT);
    }

    public ParticipantResponseDto signUpApple(String token) {
        return signUpWithIdProvider(token, appleIdService, Role.ROLE_PARTICIPANT);
    }

    @Override
    public void throwIfRequiredFieldsNotFilled(Participant participant) {
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
    public void throwIfUniqueConstraintsViolated(Participant user) {
        if(user.getUsername() != null) {
            User found = userRepository.findActiveByUsername(user.getUsername()).orElse(null);
            if(found != null && !found.getId().equals(user.getId())) {
                throw new UserException("Participant with [username: " + user.getUsername() + "] already exists");
            }
        }

        if(user.getEmail() != null) {
            User found = userRepository.findVerifiedByEmail(user.getEmail()).orElse(null);
            if(found != null && !found.getId().equals(user.getId())) {
                throw new UserException("Participant with [email: " + user.getEmail() + "] already exists");
            }
        }
    }
}
