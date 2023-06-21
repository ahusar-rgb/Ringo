package com.ringo.service.company;

import com.ringo.auth.GoogleIdTokenService;
import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.OrganisationMapper;
import com.ringo.model.company.Organisation;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.OrganisationRepository;
import com.ringo.service.security.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final GoogleIdTokenService googleIdTokenService;

    public OrganisationResponseDto findById(Long id) {
        log.info("findOrganisationById: {}", id);
        Organisation organisation = organisationRepository.findByIdWithEvents(id).orElseThrow(
                () -> new NotFoundException("Organisation [id: %d] not found".formatted(id)));

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto findCurrentOrganisation() {
        log.info("findCurrentOrganisation");
        Organisation organisation = organisationRepository.findByIdWithEvents(userService.getCurrentUserIfActive().getId()).orElseThrow(
                () -> new UserException("Authorized user is not an organisation"));

        OrganisationResponseDto dto = organisationMapper.toDto(organisation);
        dto.setEmail(organisation.getEmail());
        return dto;
    }

    public OrganisationResponseDto create(OrganisationRequestDto dto) {
        log.info("createOrganisation: {}", dto);
        Organisation organisation = organisationMapper.toEntity(dto);

        if (organisationRepository.findByEmail(organisation.getEmail()).isPresent()) {
            throw new UserException("Organisation with [email: " + organisation.getEmail() + "] already exists");
        }
        if (organisationRepository.findByUsername(organisation.getUsername()).isPresent()) {
            throw new UserException("Organisation with [username: " + organisation.getUsername() + "] already exists");
        }

        organisation.setRole(Role.ROLE_ORGANISATION);
        organisation.setPassword(passwordEncoder.encode(dto.getPassword()));
        organisation.setIsActive(true);
        organisation.setCreatedAt(LocalDateTime.now());
        organisationRepository.save(organisation);

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto update(OrganisationRequestDto dto) {
        log.info("updateOrganisation: {}", dto);

        Organisation organisation = organisationRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new UserException("Authorized user is not an organisation"));

        organisationMapper.partialUpdate(organisation, dto);
        organisation.setUpdatedAt(LocalDateTime.now());
        organisationRepository.save(organisation);

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto activate() {
        Organisation organisation = organisationRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new UserException("Authorized user is not an organisation"));

        throwIfNotFullyFilled(organisation);

        organisation.setIsActive(true);
        return organisationMapper.toDto(organisationRepository.save(organisation));
    }

    private void throwIfNotFullyFilled(Organisation organisation) {
        if(organisation.getUsername() == null)
            throw new UserException("Username is not set");
    }

    public OrganisationResponseDto signUpGoogle(String token) {
        User user = googleIdTokenService.getUserFromToken(token);
        Organisation organisation = Organisation.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();

        organisation.setIsActive(false);
        organisation.setCreatedAt(LocalDateTime.now());
        organisation.setRole(Role.ROLE_ORGANISATION);

        return organisationMapper.toDto(organisationRepository.save(organisation));
    }
}
