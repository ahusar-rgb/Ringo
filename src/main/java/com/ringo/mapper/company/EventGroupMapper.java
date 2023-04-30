package com.ringo.mapper.company;

import com.ringo.dto.company.EventGroup;
import com.ringo.dto.company.EventGroupDto;
import com.ringo.service.company.EventPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class EventGroupMapper {

    private final EventPhotoService eventPhotoService;
    private final PhotoMapper photoMapper;

    public EventGroupDto toDto(EventGroup eventGroup) {
        EventGroupDto dto = EventGroupDto.builder()
                .coordinates(eventGroup.getCoordinates())
                .count(eventGroup.getCount())
                .id(eventGroup.getId())
                .build();

        if(eventGroup.getMainPhoto() != null)
            dto.setMainPhotoId(eventGroup.getMainPhoto().getLowQualityPhoto().getId());

        return dto;
    }

    public List<EventGroupDto> toDtos(List<EventGroup> eventGroups) {
        return eventGroups.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
