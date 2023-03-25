package com.ringo.mapper.company;

import com.ringo.dto.company.EventGroup;
import com.ringo.dto.company.EventGroupDto;
import com.ringo.service.EventPhotoService;
import com.ringo.service.EventPhotoStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventGroupMapper {
    private final EventPhotoStorage eventPhotoStorage;
    private final EventPhotoService eventPhotoService;

    public EventGroupDto toDto(EventGroup eventGroup) {
        return EventGroupDto.builder()
                .coordinates(eventGroup.getCoordinates())
                .count(eventGroup.getCount())
                .mainPhoto(eventGroup.getMainPhotoPath() == null ?
                        null : eventPhotoStorage.findPhoto(eventPhotoService.findPhotoByPath(eventGroup.getMainPhotoPath())))
                .build();
    }

    public List<EventGroupDto> toDtos(List<EventGroup> eventGroups) {
        return eventGroups.stream()
                .map(this::toDto)
                .toList();
    }
}
