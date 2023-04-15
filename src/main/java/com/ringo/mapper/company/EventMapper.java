package com.ringo.mapper.company;

import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.company.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
    unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
    unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE,
    uses = {OrganisationMapper.class})
public interface EventMapper extends EntityMapper<EventRequestDto, EventResponseDto, Event> {

    @Override
    @Mapping(target = "mainPhoto", ignore = true)
    @Mapping(target = "photos", ignore = true)
    EventResponseDto toDto(Event entity);

    @Override
    @Mapping(target = "mainPhoto", ignore = true)
    @Mapping(target = "photos", ignore = true)
    Event toEntity(EventRequestDto entityDto);
}
