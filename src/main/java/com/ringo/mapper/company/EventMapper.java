package com.ringo.mapper.company;

import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.dto.company.EventSmallDto;
import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.model.company.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final CurrencyMapper currencyMapper;
    private final CategoryMapper categoryMapper;
    private final OrganisationMapper organisationMapper;
    private final EventMainPhotoMapper eventMainPhotoMapper;
    private final EventPhotoMapper eventPhotoMapper;
    private final PhotoMapper photoMapper;

    public EventResponseDto toDto(Event event) {
        EventResponseDto dto = EventResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .address(event.getAddress())
                .coordinates(new Coordinates(event.getLatitude(), event.getLongitude()))
                .isTicketNeeded(event.getIsTicketNeeded())
                .price(event.getPrice())
                .currency(currencyMapper.toDto(event.getCurrency()))
                .startTime(event.getStartTime().toString())
                .endTime(event.getEndTime().toString())
                .categories(categoryMapper.toDtos(event.getCategories()))
                .host(organisationMapper.toDto(event.getHost()))
                .peopleCount(event.getPeopleCount())
                .capacity(event.getCapacity())
                .build();

        if(event.getMainPhoto() != null)
            dto.setMainPhoto(eventMainPhotoMapper.toDto(event.getMainPhoto()));

        List<EventPhotoDto> photos = new ArrayList<>();
        if(event.getPhotos() != null)
            event.getPhotos().forEach(
                    photo -> {
                        if(Objects.equals(photo.getId(), event.getMainPhoto().getId()))
                            return;

                       photos.add(eventPhotoMapper.toDto(photo));
                    }
            );

        dto.setPhotos(photos);

        return dto;
    }

    public EventSmallDto toSmallDto(Event event) {
        EventSmallDto dto = EventSmallDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .address(event.getAddress())
                .coordinates(new Coordinates(event.getLatitude(), event.getLongitude()))
                .isTicketNeeded(event.getIsTicketNeeded())
                .price(event.getPrice())
                .currency(currencyMapper.toDto(event.getCurrency()))
                .startTime(event.getStartTime().toString())
                .endTime(event.getEndTime().toString())
                .categories(categoryMapper.toDtos(event.getCategories()))
                .hostId(event.getHost().getId())
                .peopleCount(event.getPeopleCount())
                .capacity(event.getCapacity())
                .build();

        if(event.getMainPhoto() != null)
           dto.setMainPhotoId(event.getMainPhoto().getMediumQualityPhoto().getId());

        return dto;
    }

    public List<EventSmallDto> toSmallDtos(List<Event> events) {
        List<EventSmallDto> dtos = new ArrayList<>();
        events.forEach(event -> dtos.add(toSmallDto(event)));
        return dtos;
    }

    public Event toEntity(EventRequestDto dto) {
        return Event.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .address(dto.getAddress())
                .latitude(dto.getCoordinates().latitude())
                .longitude(dto.getCoordinates().longitude())
                .isTicketNeeded(dto.getIsTicketNeeded())
                .price(dto.getPrice())
                .currency(null)
                .startTime(LocalDateTime.parse(dto.getStartTime()))
                .endTime(LocalDateTime.parse(dto.getEndTime()))
                .categories(null)
                .capacity(dto.getCapacity())
                .peopleCount(0)
                .totalPhotoCount(dto.getTotalPhotoCount())
                .build();
    }

    public List<Event> toEntities(List<EventRequestDto> dtos) {
        List<Event> events = new ArrayList<>();
        dtos.forEach(dto -> events.add(toEntity(dto)));
        return events;
    }
}
