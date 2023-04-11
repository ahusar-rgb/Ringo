package com.ringo.service.company;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.*;
import com.ringo.exception.IllegalInsertException;
import com.ringo.exception.NotFoundException;
import com.ringo.mapper.company.EventGroupMapper;
import com.ringo.mapper.company.EventMapper;
import com.ringo.model.company.Event;
import com.ringo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.ringo.utils.Geography.getDistance;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final ApplicationProperties config;
    private final EventRepository repository;
    private final EventMapper mapper;
    private final EventGroupMapper groupMapper;
    private final EventPhotoStorage eventPhotoStorage;
    private final EventPhotoService eventPhotoService;

    public EventResponseDto findEventById(Long id) {
        log.info("findEventById: {}", id);
        return mapper.toDto(
                repository.findActiveById(id).orElseThrow(
                        () -> new NotFoundException("Event [id: %d] not found".formatted(id)))
        );
    }

    public List<EventResponseDto> findAllEvents() {
        log.info("findAllEvents");
        return mapper.toDtos(repository.findAllActive());
    }

    public EventResponseDto saveEvent(EventRequestDto eventDto) {
        log.info("saveEvent: {}", eventDto);
        Event event = mapper.toEntity(eventDto);
        if(event.getPhotoCount() > config.getMaxPhotoCount())
            throw new IllegalInsertException("Max photos count reached: %d/%d".formatted(event.getPhotoCount(), config.getMaxPhotoCount()));
        return mapper.toDto(repository.save(event));
    }

    public EventResponseDto addPhotoToEvent(AddEventPhotoRequest addEventPhotoDto, MultipartFile photo) {
        log.info("addPhotoToEvent: {}", addEventPhotoDto);
        log.info("photo type: {}", photo.getContentType());
        Event event = repository.findById(addEventPhotoDto.getEventId()).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(addEventPhotoDto.getEventId()))
        );

        List<EventPhotoDto> photos = eventPhotoService.findPhotosByEventId(addEventPhotoDto.getEventId());
        if(photos.size() >= event.getPhotoCount())
            throw new IllegalInsertException("Max photos count reached: %d/%d".formatted(photos.size(), event.getPhotoCount()));

        String path = "event#" + event.getId();

        EventPhotoDto eventPhotoDto = EventPhotoDto.builder()
                .eventId(addEventPhotoDto.getEventId())
                .path(path + "/" + eventPhotoService.findPhotosByEventId(addEventPhotoDto.getEventId()).size() + "." + photo.getContentType().split("/")[1])
                .isMain(addEventPhotoDto.getIsMain())
                .build();

        if(addEventPhotoDto.getIsMain())
            event.setMainPhoto(eventPhotoDto.getPath());
        eventPhotoStorage.savePhoto(eventPhotoDto, photo);

        if(photos.size() == event.getPhotoCount() - 1) {
            event.setIsActive(true);
            repository.save(event);
        }

        return mapper.toDto(event);
    }

    public List<EventGroupDto> findEventsByDistance(double latitude, double longitude, int distance) {
        List<EventGroup> groups = mapper.toGroups(
                repository.findAllByDistance(latitude, longitude, distance)
        );
        return groupMapper.toDtos(groupEvents(groups, distance / config.getMergeDistanceFactor()));
    }

    public List<EventGroupDto> findEventsInArea(double latMin, double latMax, double lonMin, double lonMax) {
        List<EventGroup> groups = mapper.toGroups(
                repository.findAllInArea(latMin, latMax, lonMin, lonMax)
        );
        return groupMapper.toDtos(groupEvents(groups, getDistance(
                new Coordinates(latMin, lonMin),
                new Coordinates(latMax, lonMax)
        ) / config.getMergeDistanceFactor()));
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
