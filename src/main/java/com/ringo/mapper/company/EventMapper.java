package com.ringo.mapper.company;

import com.ringo.config.Constants;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.CurrencyDto;
import com.ringo.dto.company.request.EventRequestDto;
import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.dto.company.response.EventSmallDto;
import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.company.Event;
import com.ringo.model.company.TicketType;
import com.ringo.model.photo.EventPhoto;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring",
        uses = {
                EventMainPhotoMapper.class,
                EventPhotoMapper.class,
                CategoryMapper.class,
                CurrencyMapper.class,
                OrganisationMapper.class,
                TicketTypeMapper.class
        },
        imports = {Coordinates.class})
public abstract class EventMapper implements EntityMapper<EventRequestDto, EventResponseDto, Event> {

    @Autowired
    private EventPhotoMapper eventPhotoMapper;

    @Autowired
    private CurrencyMapper currencyMapper;

    @Override
    public EventResponseDto toDto(Event entity) {
        throw new UnsupportedOperationException("Use toDtoSmall or toDtoDetails instead");
    }

    @Named("toDtoSmall")
    @Mapping(target = "mainPhotoId", expression = "java(entity.getMainPhoto() == null ? null : entity.getMainPhoto().getHighQualityPhoto().getId())")
    @Mapping(target = "coordinates", expression = "java(new Coordinates(entity.getLatitude(), entity.getLongitude()))")
    @Mapping(target = "hostId", source = "host.id")
    @Mapping(target = "startTime", source = "startTime", dateFormat = Constants.DATE_TIME_FORMAT)
    @Mapping(target = "endTime", source = "endTime", dateFormat = Constants.DATE_TIME_FORMAT)
    @Mapping(target = "capacity", source = "ticketTypes", qualifiedByName = "getCapacity")
    @Mapping(target = "price", source = "ticketTypes", qualifiedByName = "getPrice")
    @Mapping(target = "currency", source = "ticketTypes", qualifiedByName = "getCurrency")
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

    @Named("getCapacity")
    public Integer getCapacity(List<TicketType> ticketTypes) {
        if (ticketTypes == null ||
                ticketTypes.stream().anyMatch(ticket -> ticket.getMaxTickets() == null)) {
            return null;
        }

        return ticketTypes.stream().mapToInt(TicketType::getMaxTickets).sum();
    }

    @Named("getPrice")
    public Float getPrice(List<TicketType> ticketTypes) {
        if(ticketTypes == null)
            return null;

        return (float)ticketTypes.stream().mapToDouble(TicketType::getPrice).min().orElse(0.0);
    }

    @Named("getCurrency")
    public CurrencyDto getCurrency(List<TicketType> ticketTypes) {
        if(ticketTypes == null)
            return null;

        Optional<TicketType> ticketTypeOptional = ticketTypes.stream().min((t1, t2) -> (int) (t1.getPrice() - t2.getPrice()));
        if(ticketTypeOptional.isEmpty())
            return null;
        if(ticketTypeOptional.get().getCurrency() == null)
            return null;

        return currencyMapper.toDto(ticketTypeOptional.get().getCurrency());
    }

    @Override
    @Named("toDtoDetails")
    @Mapping(target = "coordinates", expression = "java(new Coordinates(entity.getLatitude(), entity.getLongitude()))")
    @Mapping(target = "photos", expression = "java(getPhotosWithoutMain(entity))")
    @Mapping(target = "startTime", source = "startTime", dateFormat = Constants.DATE_TIME_FORMAT)
    @Mapping(target = "endTime", source = "endTime", dateFormat = Constants.DATE_TIME_FORMAT)
    public abstract EventResponseDto toDtoDetails(Event entity);

    @Named("getPhotosWithoutMain")
    public List<EventPhotoDto> getPhotosWithoutMain(Event entity) {
        List<EventPhotoDto> photos = new ArrayList<>();
        if (entity.getPhotos() != null) {
            for (EventPhoto photo : entity.getPhotos()) {
                if (entity.getMainPhoto() == null || !photo.getPhoto().getId().equals(entity.getMainPhoto().getHighQualityPhoto().getId())) {
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
    @Mapping(target = "savedBy", ignore = true)
    @Mapping(target = "ticketTypes", ignore = true)
    @Mapping(target = "peopleCount", expression = "java(0)")
    @Mapping(target = "peopleSaved", expression = "java(0)")
    @Mapping(target = "startTime", source = "startTime", dateFormat = Constants.DATE_TIME_FORMAT)
    @Mapping(target = "endTime", source = "endTime", dateFormat = Constants.DATE_TIME_FORMAT)
    public abstract Event toEntity(EventRequestDto eventRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "mainPhoto", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "peopleCount", ignore = true)
    @Mapping(target = "peopleSaved", ignore = true)
    @Mapping(target = "ticketTypes", ignore = true)
    @Mapping(target = "registrationForm", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "latitude", expression = "java(eventSmallDto.getCoordinates() == null ? event.getLatitude() : eventSmallDto.getCoordinates().latitude())")
    @Mapping(target = "longitude", expression = "java(eventSmallDto.getCoordinates() == null ? event.getLongitude() : eventSmallDto.getCoordinates().longitude())")
    @Mapping(target = "startTime", source = "startTime", dateFormat = Constants.DATE_TIME_FORMAT)
    @Mapping(target = "endTime", source = "endTime", dateFormat = Constants.DATE_TIME_FORMAT)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void partialUpdate(@MappingTarget Event event, EventRequestDto eventSmallDto);
}
