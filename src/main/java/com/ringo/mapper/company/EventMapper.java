package com.ringo.mapper.company;

import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.dto.company.EventSmallDto;
import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.company.Event;
import com.ringo.model.photo.EventPhoto;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = {EventMainPhotoMapper.class, EventPhotoMapper.class, CategoryMapper.class, CurrencyMapper.class, OrganisationMapper.class},
        imports = {Coordinates.class})
public abstract class EventMapper implements EntityMapper<EventRequestDto, EventResponseDto, Event> {

    @Autowired
    private EventPhotoMapper eventPhotoMapper;

    @Override
    public EventResponseDto toDto(Event entity) {
        throw new UnsupportedOperationException("Use toDtoSmall or toDtoDetails instead");
    }

    @Named("toDtoSmall")
    @Mapping(target = "mainPhotoId", expression = "java(entity.getMainPhoto() == null ? null : entity.getMainPhoto().getId())")
    public abstract EventSmallDto toDtoSmall(Event entity);

    @Named("toDtoSmallList")
    public List<EventSmallDto> toDtoSmallList(List<Event> events) {
        if (events == null) {
            return null;
        }

        List<EventSmallDto> list = new ArrayList<>(events.size());
        for (Event event : events) {
            list.add(toDtoSmall(event));
        }
        return list;
    }

    @Override
    @Named("toDtoDetails")
    @Mapping(target = "coordinates", expression = "java(new Coordinates(entity.getLatitude(), entity.getLongitude()))")
    @Mapping(target = "photos", expression = "java(getPhotosWithoutMain(entity))")
    public abstract EventResponseDto toDtoDetails(Event entity);

    @Named("getPhotosWithoutMain")
    public List<EventPhotoDto> getPhotosWithoutMain(Event entity) {
        List<EventPhotoDto> photos = new ArrayList<>();
        if (entity.getPhotos() != null) {
            for (EventPhoto photo : entity.getPhotos()) {
                if (!photo.getPhoto().getId().equals(entity.getMainPhoto().getHighQualityPhoto().getId())) {
                    photos.add(eventPhotoMapper.toDto(photo));
                }
            }
        }
        return photos;
    }

    @Override
    @Mapping(target = "latitude", expression = "java(eventRequestDto.getCoordinates() == null ? null : eventRequestDto.getCoordinates().latitude())")
    @Mapping(target = "longitude", expression = "java(eventRequestDto.getCoordinates() == null ? null : eventRequestDto.getCoordinates().longitude())")
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "mainPhoto", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "peopleCount", expression = "java(0)")
    @Mapping(target = "peopleSaved", expression = "java(0)")
    public abstract Event toEntity(EventRequestDto eventRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "mainPhoto", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "peopleCount", ignore = true)
    @Mapping(target = "peopleSaved", ignore = true)
    @Mapping(target = "registrationForm", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "latitude", expression = "java(eventSmallDto.getCoordinates() == null ? event.getLatitude() : eventSmallDto.getCoordinates().latitude())")
    @Mapping(target = "longitude", expression = "java(eventSmallDto.getCoordinates() == null ? event.getLongitude() : eventSmallDto.getCoordinates().longitude())")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void partialUpdate(@MappingTarget Event event, EventRequestDto eventSmallDto);
}
