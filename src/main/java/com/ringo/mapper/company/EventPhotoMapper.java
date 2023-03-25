package com.ringo.mapper.company;

import com.ringo.dto.company.EventPhotoDto;
import com.ringo.exception.NotFoundException;
import com.ringo.mapper.common.PhotoMapper;
import com.ringo.model.company.Event;
import com.ringo.model.company.EventPhoto;
import com.ringo.repository.EventRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = "spring",
    uses = {EventMapper.class})
public abstract class EventPhotoMapper implements PhotoMapper<EventPhotoDto, EventPhoto> {

    @Autowired
    private EventRepository eventRepository;

    @Override
    @Mapping(source = "event", target = "eventId", qualifiedByName = "EventToId")
    @Mapping(target = "isMain", ignore = true)
    public abstract EventPhotoDto toDto(EventPhoto entity);

    @Override
    @Mapping(source = "eventId", target = "event", qualifiedByName = "IdToEvent")
    public abstract EventPhoto toEntity(EventPhotoDto dto);

    @Override
    @Mapping(source = "event", target = "eventId", qualifiedByName = "EventToId")
    public abstract List<EventPhotoDto> toDtos(List<EventPhoto> entities);

    @Override
    @Mapping(source = "eventId", target = "event", qualifiedByName = "IdToEvent")
    public abstract List<EventPhoto> toEntities(List<EventPhotoDto> dtos);

    @Named("EventToId")
    public Long EventToId(Event entity) {
        if (entity == null) {
            return null;
        }
        return entity.getId();
    }

    @Named("IdToEvent")
    public Event IdToEvent(Long id) {
        if (id == null) {
            return null;
        }

        return eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );
    }
}
