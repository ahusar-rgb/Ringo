package com.ringo.mapper.company;

import com.ringo.dto.company.EventGroup;
import com.ringo.dto.company.EventGroupDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventGroupMapper {

    @Mapping(target = "mainPhotoId", source = "mainPhoto.lowQualityPhoto.id")
    EventGroupDto toDto(EventGroup eventGroup);

    List<EventGroupDto> toDtoList(List<EventGroup> eventGroups);
}
