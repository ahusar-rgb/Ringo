package com.ringo.service.company;

import com.ringo.auth.AppleIdService;
import com.ringo.auth.AuthenticationService;
import com.ringo.auth.GoogleIdService;
import com.ringo.dto.company.request.ParticipantRequestDto;
import com.ringo.dto.company.response.ParticipantResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.ParticipantMapper;
import com.ringo.model.company.*;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.company.*;
import com.ringo.service.common.AbstractUserService;
import com.ringo.service.common.PhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ParticipantService extends AbstractUserService<ParticipantRequestDto, Participant, ParticipantResponseDto> {

    @Autowired
    private GoogleIdService googleIdService;
    @Autowired
    private AppleIdService appleIdService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private OrganisationRepository organisationRepository;

    private final ParticipantRepository repository;
    private final ParticipantMapper mapper;

    public ParticipantService(UserRepository userRepository,
                              ParticipantRepository repository,
                              PasswordEncoder passwordEncoder,
                              ParticipantMapper mapper,
                              PhotoService photoService,
                              AuthenticationService authenticationService) {
        super(userRepository, repository, passwordEncoder, mapper, photoService, authenticationService);
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
    protected void prepareForDelete(Participant user) {
        List<Ticket> tickets = ticketRepository.findAllByParticipantId(user.getId());
        for(Ticket ticket : tickets) {
            Event event = ticket.getId().getEvent();
            event.setPeopleCount(event.getPeopleCount() - 1);
        }
        for(Event event : user.getSavedEvents())
            event.setPeopleSaved(event.getPeopleSaved() - 1);

        if(user.getReviews() != null) {
            for(Review review : user.getReviews()) {
                Organisation organisation = review.getOrganisation();
                organisation.getReviews().remove(review);
                reviewRepository.delete(review);

                Float rating = reviewRepository.getAverageRatingByOrganisationId(organisation.getId());
                organisation.setRating(rating);

                organisationRepository.save(organisation);
            }
        }

        ticketRepository.deleteAll(tickets);
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
            User found = userRepository.findByUsername(user.getUsername()).orElse(null);
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
