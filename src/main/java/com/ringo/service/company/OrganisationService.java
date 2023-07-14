package com.ringo.service.company;

import com.ringo.auth.AppleIdService;
import com.ringo.auth.GoogleIdService;
import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.OrganisationMapper;
import com.ringo.model.company.Organisation;
import com.ringo.model.security.Role;
import com.ringo.repository.OrganisationRepository;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.AbstractUserService;
import com.ringo.service.common.PhotoService;
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

    private final UserRepository userRepository;
    private final OrganisationMapper organisationMapper;
    private final OrganisationRepository organisationRepository;

    public OrganisationService(UserRepository userRepository,
                               OrganisationRepository repository,
                               PasswordEncoder passwordEncoder,
                               OrganisationMapper mapper,
                               PhotoService photoService) {
        super(userRepository, repository, passwordEncoder, mapper, photoService);
        this.organisationMapper = mapper;
        this.organisationRepository = repository;
        this.userRepository = userRepository;
    }


    public OrganisationResponseDto findById(Long id) {
        log.info("findOrganisationById: {}", id);
        Organisation organisation = organisationRepository.findByIdActiveWithEvents(id).orElseThrow(
                () -> new NotFoundException("Organisation [id: %d] not found".formatted(id)));

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto save(OrganisationRequestDto dto) {
        return save(dto, Role.ROLE_ORGANISATION);
    }

    public OrganisationResponseDto findCurrentOrganisation() {
        log.info("findCurrentOrganisation");
        Organisation organisation = organisationRepository.findByIdWithEvents(getUserDetails().getId()).orElseThrow(
                () -> new UserException("Authorized user is not an organisation"));

        OrganisationResponseDto dto = organisationMapper.toDto(organisation);
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
    protected void throwIfNotFullyFilled(Organisation organisation) {
        if(organisation.getUsername() == null)
            throw new UserException("Username is not set");
    }

    @Override
    protected void throwIfUniqueConstraintsViolated(Organisation user) {
       if(userRepository.findByEmail(user.getEmail()).isPresent())
           throw new UserException("User with email %s already exists".formatted(user.getEmail()));
       if(user.getUsername() != null && userRepository.findByUsername(user.getUsername()).isPresent())
           throw new UserException("User with username %s already exists".formatted(user.getUsername()));
    }
}
