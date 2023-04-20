package com.ringo.service.company;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.*;
import com.ringo.dto.search.EventSearchDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventGroupMapper;
import com.ringo.mapper.company.EventMapper;
import com.ringo.model.company.*;
import com.ringo.repository.CategoryRepository;
import com.ringo.repository.CurrencyRepository;
import com.ringo.repository.EventRepository;
import com.ringo.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        return mapper.toDto(event);
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
            throw new UserException("Max photos count reached: %d/%d".formatted(event.getTotalPhotoCount(), config.getMaxPhotoCount()));

        return mapper.toDto(repository.save(event));
    }

    public void addPhotoToEvent(AddEventPhotoRequest addEventPhotoRequest, MultipartFile photo) {
        log.info("addPhotoToEvent: {}, {}", addEventPhotoRequest.getEventId(), photo.getOriginalFilename());
        log.info("photo type: {}", photo.getContentType());

        Event event = repository.findById(addEventPhotoRequest.getEventId()).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(addEventPhotoRequest.getEventId()))
        );

        if(event.getPhotos().size() >= event.getTotalPhotoCount())
            throw new UserException("All photos for event [id: %d] have been uploaded".formatted(event.getId()));

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

    public List<EventSmallDto> findTopByDistance(double latitude, double longitude, int limit) {

        if(limit > 100)
            throw new RuntimeException("Limit can't be more than 100");

        Pageable pageable = PageRequest.of(0, limit);
        return repository.findTopByDistance(latitude, longitude, pageable).getContent().stream().map(
                event -> {
                    EventSmallDto dto = mapper.toSmallDto(event);
                    dto.setDistance(getDistance(new Coordinates(latitude, longitude), dto.getCoordinates()));
                    return dto;
                }
        ).collect(Collectors.toList());
    }

    public List<EventGroupDto> findEventsInArea(double latMin, double latMax, double lonMin, double lonMax) {

        log.info("findEventsInArea: {}, {}, {}, {}", latMin, latMax, lonMin, lonMax);
        List<Event> events = repository.findAllInArea(latMin, latMax, lonMin, lonMax);

        List<EventGroup> groups = events.stream().map(event ->
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

    public List<EventSmallDto> searchEvents(EventSearchDto searchDto) {
        log.info("searchEvents: {}", searchDto);

        List<Event> events = repository.findAll(searchDto.getSpecification(), searchDto.getPageable()).getContent();

        return events.stream()
                .map(event -> {
                    EventSmallDto dto = mapper.toSmallDto(event);
                    if(searchDto.getLatitude() != null && searchDto.getLongitude() != null)
                        dto.setDistance(
                                getDistance(new Coordinates(searchDto.getLatitude(), searchDto.getLongitude()),
                                dto.getCoordinates())
                        );
                    return dto;
        }).collect(Collectors.toList());
    }

}
