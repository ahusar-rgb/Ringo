package com.ringo.mapper.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.model.company.Organisation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganisationMapper {

    private final ReviewMapper reviewMapper;

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
                .username(entity.getUsername())
                .description(entity.getDescription())
                .contacts(entity.getContacts())
                .rating(entity.getRating())
                .build();

        organisation.setPastEventsCount((int)entity.getHostedEvents().stream().filter(event -> event.getEndTime().isBefore(java.time.LocalDateTime.now())).count());
        organisation.setUpcomingEventsCount((int)entity.getHostedEvents().stream().filter(event -> event.getStartTime().isAfter(java.time.LocalDateTime.now())).count());
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
