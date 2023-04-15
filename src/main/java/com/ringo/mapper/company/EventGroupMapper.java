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

    public EventGroupDto toDto(EventGroup eventGroup) {
        return EventGroupDto.builder()
                .coordinates(eventGroup.getCoordinates())
                .count(eventGroup.getCount())
                .mainPhoto(
                        eventGroup.getMainPhoto() != null
                                ? eventPhotoService.findBytes(eventGroup.getMainPhoto())
                                : null
                )
                .id(eventGroup.getId())
                .build();
    }

    public List<EventGroupDto> toDtos(List<EventGroup> eventGroups) {
        return eventGroups.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
