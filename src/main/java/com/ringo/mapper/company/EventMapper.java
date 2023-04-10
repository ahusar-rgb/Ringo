package com.ringo.mapper.company;

import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.EventGroup;
import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.model.company.Event;
import com.ringo.repository.OrganisationRepository;
import com.ringo.service.company.CategoryService;
import com.ringo.service.company.CurrencyService;
import com.ringo.service.company.EventPhotoService;
import com.ringo.service.company.EventPhotoStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;
    private final CategoryMapper categoryMapper;
    private final CurrencyMapper currencyMapper;
    private final CategoryService categoryService;
    private final CurrencyService currencyService;
    private final EventPhotoStorage eventPhotoStorage;
    private final EventPhotoService eventPhotoService;

    public EventResponseDto toDto(Event event) {
        return EventResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .mainPhoto(event.getMainPhoto() == null ? null : eventPhotoStorage.findPhoto(eventPhotoService.findPhotoByPath(event.getMainPhoto())))
                .photos(
                        eventPhotoService.findPhotosByEventId(event.getId())
                                .stream()
                                .map(eventPhotoStorage::findPhoto)
                                .toList()
                )
                .address(event.getAddress())
                .coordinates(new Coordinates(event.getLatitude(), event.getLongitude()))
                .isTicketNeeded(event.getIsTicketNeeded())
                .price(event.getPrice())
                .currency(currencyMapper.toDto(event.getCurrency()))
                .startTime(event.getStartTime().toString())
                .endTime(event.getEndTime().toString())
                .categories(categoryMapper.toDtos(event.getCategories()))
                .organisation(organisationMapper.toDto(event.getHost()))
                .build();
    }

    public List<EventResponseDto> toDtos(List<Event> events) {
        return events.stream()
                .map(this::toDto)
                .toList();
    }

    public EventGroup eventGroup(Event event) {
        return new EventGroup(new Coordinates(event.getLatitude(), event.getLongitude()), 1, event.getMainPhoto());
    }

    public Event toEntity(EventRequestDto eventDto) {
        return Event.builder()
                .id(eventDto.getId())
                .name(eventDto.getName())
                .description(eventDto.getDescription())
                .mainPhoto(null)
                .address(eventDto.getAddress())
                .latitude(eventDto.getCoordinates().latitude())
                .longitude(eventDto.getCoordinates().longitude())
                .isTicketNeeded(eventDto.getIsTicketNeeded())
                .price(eventDto.getPrice())
                .currency(currencyMapper.toEntity(currencyService.findCurrencyById(eventDto.getCurrencyId())))
                .startTime(LocalDateTime.parse(eventDto.getStartTime()))
                .endTime(LocalDateTime.parse(eventDto.getEndTime()))
                .categories(categoryMapper.toEntities(eventDto.getCategoryIds().stream()
                                .map(categoryService::findCategoryById).toList()))
                .host(organisationRepository.findById(eventDto.getOrganisationId()).orElseThrow(
                        () -> new NotFoundException("Organisation [id: %d] not found".formatted(eventDto.getOrganisationId()))
                ))
                .photoCount(eventDto.getPhotoCount())
                .build();
    }


    public List<Event> toEntities(List<EventRequestDto> eventDtos) {
        return eventDtos.stream()
                .map(this::toEntity)
                .toList();
    }

    public List<EventGroup> toGroups(List<Event> events) {
        return events.stream()
                .map(this::eventGroup)
                .toList();
    }
}
