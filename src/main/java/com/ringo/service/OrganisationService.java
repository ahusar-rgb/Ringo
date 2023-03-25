package com.ringo.service;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.exception.IllegalInsertException;
import com.ringo.exception.NotFoundException;
import com.ringo.mapper.company.OrganisationMapper;
import com.ringo.model.company.Organisation;
import com.ringo.model.enums.Role;
import com.ringo.repository.OrganisationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;

    public OrganisationResponseDto findOrganisationById(Long id) {
        log.info("findOrganisationById: {}", id);
        Organisation organisation = organisationRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Organisation [id: %d] not found".formatted(id)));

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto createOrganisation(OrganisationRequestDto dto) {
        log.info("createOrganisation: {}", dto);
        Organisation organisation = organisationMapper.toEntity(dto);

        if (organisationRepository.findByEmail(organisation.getEmail()).isPresent()) {
            throw new IllegalInsertException("Organisation with [email: " + organisation.getEmail() + "] already exists");
        }
        if (organisationRepository.findByUsername(organisation.getUsername()).isPresent()) {
            throw new IllegalInsertException("Organisation with [username: " + organisation.getUsername() + "] already exists");
        }

        organisation.setRole(Role.ROLE_ORGANISATION);
        organisation = organisationRepository.save(organisation);

        return organisationMapper.toDto(organisation);
    }
}
