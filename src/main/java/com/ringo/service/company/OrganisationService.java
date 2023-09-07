package com.ringo.service.company;

import com.ringo.auth.AppleIdService;
import com.ringo.auth.AuthenticationService;
import com.ringo.auth.GoogleIdService;
import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.OrganisationMapper;
import com.ringo.model.company.Event;
import com.ringo.model.company.Organisation;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.OrganisationRepository;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.AbstractUserService;
import com.ringo.service.common.PhotoService;
import com.ringo.service.company.event.EventCleanUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class OrganisationService extends AbstractUserService<OrganisationRequestDto, Organisation, OrganisationResponseDto> {

    @Autowired
    private GoogleIdService googleIdService;
    @Autowired
    private AppleIdService appleIdService;
    @Autowired
    private EventCleanUpService eventCleanUpService;

    private final OrganisationMapper mapper;
    private final OrganisationRepository organisationRepository;

    public OrganisationService(UserRepository userRepository,
                               OrganisationRepository repository,
                               PasswordEncoder passwordEncoder,
                               OrganisationMapper mapper,
                               PhotoService photoService,
                               AuthenticationService authenticationService){
        super(userRepository, repository, passwordEncoder, mapper, photoService, authenticationService);
        this.mapper = mapper;
        this.organisationRepository = repository;
    }


    public OrganisationResponseDto findById(Long id) {
        log.info("findOrganisationById: {}", id);
        Organisation organisation = organisationRepository.findByIdActiveWithEvents(id).orElseThrow(
                () -> new NotFoundException("Organisation [id: %d] not found".formatted(id)));

        return mapper.toDtoDetails(organisation);
    }

    public OrganisationResponseDto save(OrganisationRequestDto dto) {
        return save(dto, Role.ROLE_ORGANISATION);
    }

    @Override
    protected void prepareForSave(Organisation user) {
        if(user.getContacts() != null)
            user.getContacts().forEach(contact -> {
                if(contact.getOrdinal() == null)
                    throw new UserException("Contact ordinal is not set");
                contact.setOrganisation(user);
            });
    }

    @Override
    public void prepareForDelete(Organisation user) {
        if(user.getHostedEvents().stream().anyMatch(event -> event.getPeopleCount() > 0))
            throw new UserException("Cannot delete organisation with events that have participants");

        for(Event event : user.getHostedEvents()) {
            eventCleanUpService.cleanUpEvent(event);
        }
    }

    public OrganisationResponseDto findCurrentOrganisation() {
        log.info("findCurrentOrganisation");
        Organisation organisation = getFullUser();

        OrganisationResponseDto dto = mapper.toDto(organisation);
        dto.setEmail(organisation.getEmail());
        return dto;
    }

    public OrganisationResponseDto signUpGoogle(String token) {
        return signUpWithIdProvider(token, googleIdService, Role.ROLE_ORGANISATION);
    }

    public OrganisationResponseDto signUpApple(String token) {
        return signUpWithIdProvider(token, appleIdService, Role.ROLE_ORGANISATION);
    }


    @Override
    public void throwIfRequiredFieldsNotFilled(Organisation organisation) {
        if(organisation.getUsername() == null)
            throw new UserException("Username is not set");
        if(organisation.getName() == null)
            throw new UserException("Name is not set");
    }

    @Override
    public void throwIfUniqueConstraintsViolated(Organisation user) {

        if(user.getUsername() != null) {
            User found  = userRepository.findByUsername(user.getUsername()).orElse(null);
            if(found != null && !found.getId().equals(user.getId()))
                throw new UserException("User with username %s already exists".formatted(user.getUsername()));
        }

        if(user.getEmail() != null) {
            User found  = userRepository.findVerifiedByEmail(user.getEmail()).orElse(null);
            if(found != null && !found.getId().equals(user.getId()))
                throw new UserException("User with email %s already exists".formatted(user.getEmail()));
        }
    }
}
