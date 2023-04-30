package com.ringo.mapper.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.company.Organisation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganisationMapper extends EntityMapper<OrganisationRequestDto, OrganisationResponseDto, Organisation> {

    @Override
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "hostedEvents", ignore = true)
    @Mapping(target = "role", ignore = true)
    Organisation toEntity(OrganisationRequestDto dto);

    @Override
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "hostedEventIds", ignore = true)
    @Mapping(target = "role", ignore = true)
    OrganisationResponseDto toDto(Organisation entity);
}
