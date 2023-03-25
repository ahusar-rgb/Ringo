package com.ringo.mapper.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.model.company.Organisation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class OrganisationMapper {
    @Mapping(target = "pathToPhoto", ignore = true)
    @Mapping(target = "hostedEvents", ignore = true)
    @Mapping(target = "role", ignore = true)
    public abstract Organisation toEntity(OrganisationRequestDto dto);

    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "hostedEventIds", ignore = true)
    @Mapping(target = "role", ignore = true)
    public abstract OrganisationResponseDto toDto(Organisation entity);

    public abstract List<Organisation> toEntities(List<OrganisationRequestDto> dtos);

    public abstract List<OrganisationResponseDto> toDtos(List<Organisation> entities);
}
