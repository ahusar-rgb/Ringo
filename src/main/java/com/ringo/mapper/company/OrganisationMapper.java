package com.ringo.mapper.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.model.company.Organisation;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class OrganisationMapper {

    public Organisation toEntity(OrganisationRequestDto dto)
    {
        return Organisation.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .description(dto.getDescription())
                .email(dto.getEmail())
                .contacts(dto.getContacts())
                .build();
    }

    public OrganisationResponseDto toDto(Organisation entity) {
        OrganisationResponseDto organisation = OrganisationResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .description(entity.getDescription())
                .contacts(entity.getContacts())
                .rating(entity.getRating())
                .build();

        organisation.setHostedEventIds(new HashSet<>());
        entity.getHostedEvents().forEach(e -> organisation.getHostedEventIds().add(e.getId()));
        if(entity.getProfilePicture() != null)
            organisation.setProfilePicture(entity.getProfilePicture().getId());
        return organisation;
    }


    public void partialUpdate(Organisation entity, OrganisationRequestDto dto) {
        if(dto.getName() != null) entity.setName(dto.getName());
        if(dto.getUsername() != null) entity.setUsername(dto.getUsername());
        if(dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if(dto.getContacts() != null) entity.setContacts(dto.getContacts());
    }
}
