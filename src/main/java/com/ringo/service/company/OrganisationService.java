package com.ringo.service.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.OrganisationMapper;
import com.ringo.model.company.Organisation;
import com.ringo.model.security.Role;
import com.ringo.repository.OrganisationRepository;
import com.ringo.service.security.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public OrganisationResponseDto findById(Long id) {
        log.info("findOrganisationById: {}", id);
        Organisation organisation = organisationRepository.findByIdWithEvents(id).orElseThrow(
                () -> new NotFoundException("Organisation [id: %d] not found".formatted(id)));

        OrganisationResponseDto dto = organisationMapper.toDto(organisation);
        dto.setEmail(null);
        return dto;
    }

    public OrganisationResponseDto findCurrentOrganisation() {
        log.info("findCurrentOrganisation");
        Organisation organisation = organisationRepository.findByIdWithEvents(userService.getCurrentUserAsEntity().getId()).orElseThrow(
                () -> new UserException("Authorized user is not an organisation"));

        return organisationMapper.toDto(organisation);
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
        organisationRepository.save(organisation);

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto update(OrganisationRequestDto dto) {
        log.info("updateOrganisation: {}", dto);

        Organisation organisation = organisationRepository.findById(userService.getCurrentUserAsEntity().getId()).orElseThrow(
                () -> new UserException("Authorized user is not an organisation"));

        organisationMapper.partialUpdate(organisation, dto);
        organisationRepository.save(organisation);

        return organisationMapper.toDto(organisation);
    }
}
