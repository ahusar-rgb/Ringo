package com.ringo.mapper.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.mapper.common.AbstractUserMapper;
import com.ringo.model.company.Organisation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = {LabelMapper.class})
public interface OrganisationMapper extends AbstractUserMapper<OrganisationRequestDto, Organisation, OrganisationResponseDto> {

    @Override
    @Mapping(target = "profilePictureId", source = "profilePicture.id")
    @Mapping(target = "pastEventsCount", ignore = true)
    @Mapping(target = "upcomingEventsCount", ignore = true)
    @Mapping(target = "contacts", source = "contacts")
    @Mapping(target = "email", ignore = true)
    OrganisationResponseDto toDto(Organisation entity);

    @Override
    @Named("toDtoDetails")
    @Mapping(target = "profilePictureId", source = "profilePicture.id")
    @Mapping(target = "contacts", source = "contacts")
    default OrganisationResponseDto toDtoDetails(Organisation entity) {
        OrganisationResponseDto dto = toDto(entity);

        if(entity.getHostedEvents().isEmpty()) {
            dto.setPastEventsCount(0);
            dto.setUpcomingEventsCount(0);
        } else {
            dto.setPastEventsCount((int)entity.getHostedEvents().stream()
                    .filter(event -> event.getEndTime().isBefore(LocalDateTime.now()))
                    .count()
            );
            dto.setUpcomingEventsCount((int)entity.getHostedEvents().stream()
                    .filter(event -> event.getStartTime()
                            .isAfter(LocalDateTime.now()))
                    .count()
            );
        }

        return dto;
    }
}
