package com.ringo.service.company;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.*;
import com.ringo.exception.IllegalInsertException;
import com.ringo.exception.NotFoundException;
import com.ringo.mapper.company.EventGroupMapper;
import com.ringo.mapper.company.EventMapper;
import com.ringo.model.company.*;
import com.ringo.repository.CategoryRepository;
import com.ringo.repository.CurrencyRepository;
import com.ringo.repository.EventRepository;
import com.ringo.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ringo.utils.Geography.getDistance;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final ApplicationProperties config;
    private final EventRepository repository;
    private final EventMapper mapper;
    private final EventGroupMapper groupMapper;
    private final EventPhotoService eventPhotoService;
    private final OrganisationRepository organisationRepository;
    private final CurrencyRepository currencyRepository;
    private final CategoryRepository categoryRepository;

    public EventResponseDto findEventById(Long id) {
        log.info("findEventById: {}", id);

        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        EventResponseDto dto = mapper.toDto(event);
        dto.setPhotos(new ArrayList<>());
        dto.setMainPhoto(eventPhotoService.findBytes(event.getMainPhoto()));

        event.getPhotos().forEach(
                photo -> {
                    if(!photo.getId().equals(event.getMainPhoto().getId()))
                        dto.getPhotos().add(eventPhotoService.findBytes(photo));
                }
        );

        return dto;
    }

    public EventResponseDto saveEvent(EventRequestDto eventDto) {
        log.info("saveEvent: {}", eventDto);

        Organisation organisation = organisationRepository.findActiveById(eventDto.getOrganisationId()).orElseThrow(
                () -> new NotFoundException("Organisation [id: %d] not found".formatted(eventDto.getOrganisationId()))
        );
        Currency currency = currencyRepository.findById(eventDto.getCurrencyId()).orElseThrow(
                () -> new NotFoundException("Currency [id: %d] not found".formatted(eventDto.getCurrencyId()))
        );
        List<Category> categories = eventDto.getCategoryIds().stream()
                .map(id -> categoryRepository.findById(id).orElseThrow(
                        () -> new NotFoundException("Category [id: %d] not found".formatted(id))
                )).toList();


        Event event = mapper.toEntity(eventDto);
        event.setHost(organisation);
        event.setCurrency(currency);
        event.setCategories(categories);
        event.setIsActive(false);

        if(event.getTotalPhotoCount() > config.getMaxPhotoCount())
            throw new IllegalInsertException("Max photos count reached: %d/%d".formatted(event.getTotalPhotoCount(), config.getMaxPhotoCount()));

        return mapper.toDto(repository.save(event));
    }

    public void addPhotoToEvent(AddEventPhotoRequest addEventPhotoRequest, MultipartFile photo) {
        log.info("addPhotoToEvent: {}, {}", addEventPhotoRequest.getEventId(), photo.getOriginalFilename());
        log.info("photo type: {}", photo.getContentType());

        Event event = repository.findById(addEventPhotoRequest.getEventId()).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(addEventPhotoRequest.getEventId()))
        );

        if(event.getPhotos().size() >= event.getTotalPhotoCount())
            throw new IllegalInsertException("All photos for event [id: %d] have been uploaded".formatted(event.getId()));

        EventPhoto newPhoto = eventPhotoService.savePhoto(event, photo);

        if(event.getMainPhoto() == null || addEventPhotoRequest.getIsMain()) {
            event.setMainPhoto(newPhoto);
        }
        event.getPhotos().add(newPhoto);


        if(event.getPhotos().size() == event.getTotalPhotoCount()) {
            event.setIsActive(true);
        }

        repository.save(event);
    }

//    public List<EventResponseDto> findEventsByDistance(double latitude, double longitude, double distance) {
//
//        if(distance >= config.getMaxDistanceForRequest())
//            throw new IllegalInsertException("Max distance for request exceeded: %f/%f".formatted(distance, config.getMaxDistanceForRequest()));
//
//        List<EventResponseDto> result = mapper.toDtos(repository.findAllByDistance(latitude, longitude, distance));
//
//    }

    public List<EventGroupDto> findEventsInArea(double latMin, double latMax, double lonMin, double lonMax) {
        List<EventGroup> groups = repository.findAllInArea(latMin, latMax, latMin, latMax).stream().map(event ->
                EventGroup.builder()
                        .count(1)
                        .coordinates(new Coordinates(event.getLatitude(), event.getLongitude()))
                        .mainPhoto(event.getMainPhoto())
                        .id(event.getId())
                        .build()
        ).collect(Collectors.toList());

        return groupMapper.toDtos(groupEvents(groups,
                getDistance(
                        new Coordinates(latMin, lonMin),
                        new Coordinates(latMax, lonMax)) / config.getMergeDistanceFactor()));
    }


    private List<EventGroup> groupEvents(List<EventGroup> groups, int mergeDistance) {
        List<EventGroup> result = new ArrayList<>(groups);
        for(int i = 0; i < result.size(); i++) {
            for(int j = i; j < result.size(); j++) {
                EventGroup group = result.get(i);
                EventGroup other = result.get(j);
                if (group == other) continue;
                int distance = getDistance(group.getCoordinates(), other.getCoordinates());
                if (distance <= mergeDistance) {
                    result.remove(group);
                    result.remove(other);
                    result.add(new EventGroup(
                            new Coordinates((group.getCoordinates().latitude() + other.getCoordinates().latitude()) / 2,
                                    (group.getCoordinates().longitude() + other.getCoordinates().longitude()) / 2),
                            group.getCount() + other.getCount(),
                            null,
                            null
                    ));
                    i = 0;
                    break;
                }
            }
        }
        return result;
    }
}
