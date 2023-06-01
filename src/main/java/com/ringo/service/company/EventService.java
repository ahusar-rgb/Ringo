package com.ringo.service.company;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.*;
import com.ringo.dto.search.EventSearchDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventGroupMapper;
import com.ringo.mapper.company.EventMapper;
import com.ringo.model.common.AbstractEntity;
import com.ringo.model.company.*;
import com.ringo.model.photo.EventMainPhoto;
import com.ringo.model.photo.EventPhoto;
import com.ringo.model.security.User;
import com.ringo.repository.*;
import com.ringo.repository.photo.EventPhotoRepository;
import com.ringo.service.common.CurrencyExchanger;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ringo.utils.Geography.getDistance;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventService {
    private final ApplicationProperties config;
    private final EventRepository repository;
    private final EventMapper mapper;
    private final EventGroupMapper groupMapper;
    private final EventPhotoService eventPhotoService;
    private final EventPhotoRepository eventPhotoRepository;
    private final OrganisationRepository organisationRepository;
    private final CurrencyRepository currencyRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final CurrencyExchanger currencyExchanger;
    private final TicketService ticketService;
    private final ParticipantRepository participantRepository;

    public EventResponseDto findById(Long id) {
        log.info("findEventById: {}", id);

        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(!event.getIsActive()) {
            if(!event.getHost().getId().equals(userService.getCurrentUserAsEntity().getId()))
                throw new NotFoundException("Event [id: %d] not found".formatted(id));
        }

        return getPersonalizedDto(event);
    }

    public EventResponseDto create(EventRequestDto eventDto) {
        log.info("saveEvent: {}", eventDto);

        User currentUser = userService.getCurrentUserAsEntity();
        Organisation organisation = organisationRepository.findById(currentUser.getId()).orElseThrow(
                () -> new NotFoundException("Only organisations can create events"));
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

        return mapper.toDto(repository.save(event));
    }

    public EventResponseDto update(Long id, EventRequestDto dto) {
        log.info("updateEvent: {}, {}", id, dto);

        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(!Objects.equals(event.getHost().getId(), userService.getCurrentUserAsEntity().getId()))
            throw new UserException("Only event host can update event");

        mapper.partialUpdate(event, dto);
        return mapper.toDto(repository.save(event));
    }

    public void delete(Long id) {
        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(!Objects.equals(event.getHost().getId(), userService.getCurrentUserAsEntity().getId()))
            throw new UserException("Only event host can update event");

        eventPhotoService.removeMainPhoto(event);
        event.getPhotos().stream().map(AbstractEntity::getId).forEach(
                photo -> {
                    try {
                        eventPhotoService.delete(photo);
                    } catch (UserException ignored) {}
                }
        );

        repository.deleteById(id);
    }

    public EventResponseDto addPhoto(Long eventId, MultipartFile photo) {
        log.info("addPhotoToEvent: {}, {}", eventId, photo.getOriginalFilename());
        log.info("photo type: {}", photo.getContentType());

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );

        if(!Objects.equals(event.getHost().getId(), userService.getCurrentUserAsEntity().getId()))
            throw new UserException("Only event host can update event");

        if(event.getPhotos().size() >= config.getMaxPhotoCount())
            throw new UserException("No more photos for this event is allowed");

        eventPhotoService.save(event, photo);
        event.setPhotoCount(event.getPhotoCount() + 1);
        repository.save(event);

        return mapper.toDto(event);
    }

    public EventResponseDto activate(Long eventId) {
        log.info("setActive: {}", eventId);

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );

        if(!Objects.equals(event.getHost().getId(), userService.getCurrentUserAsEntity().getId()))
            throw new UserException("Only event host can update event");

        if(event.getPhotos() == null || event.getPhotos().isEmpty())
            throw new UserException("Event must have at least one photo");

        event.setIsActive(true);
        return mapper.toDto(repository.save(event));
    }

    public EventResponseDto deactivate(Long eventId) {
        log.info("setInactive: {}", eventId);

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );

        if(!Objects.equals(event.getHost().getId(), userService.getCurrentUserAsEntity().getId()))
            throw new UserException("Only event host can update event");

        event.setIsActive(false);
        return mapper.toDto(repository.save(event));
    }

    public EventResponseDto removePhoto(Long eventId, Long photoId) {
        log.info("removePhotoFromEvent: {}, {}", eventId, photoId);

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );

        EventPhoto photo = eventPhotoRepository.findById(photoId).orElseThrow(
                () -> new NotFoundException("Photo [id: %d] not found".formatted(photoId))
        );

        if(!Objects.equals(event.getHost().getId(), userService.getCurrentUserAsEntity().getId()))
            throw new UserException("Only event host can update event");

        if(!event.getPhotos().remove(photo))
            throw new UserException("Photo [id: %d] was not owned by this event".formatted(photoId));

        eventPhotoService.delete(photo.getId());
        return mapper.toDto(repository.save(event));
    }

    public EventResponseDto setMainPhoto(Long eventId, Long photoId) {
        log.info("setMainPhoto: {}", photoId);

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );

        if(!Objects.equals(event.getHost().getId(), userService.getCurrentUserAsEntity().getId()))
            throw new UserException("Only event host can update event");

        EventMainPhoto eventMainPhoto = eventPhotoService.prepareMainPhoto(event, photoId);
        event.setMainPhoto(eventMainPhoto);
        return mapper.toDto(event);
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

    public List<EventSmallDto> search(EventSearchDto searchDto) {
        log.info("searchEvents: {}", searchDto);

        User user = userService.getCurrentUserAsEntity();
        Specification<Event> specification = searchDto.getSpecification();
        specification = specification.and((root, query, builder) -> builder.or(
                builder.equal(root.get("host").get("id"), user.getId()),
                builder.isTrue(root.get("isActive")))
        );

        List<Event> events = repository.findAll(specification, searchDto.getPageable()).getContent();

        return events.stream()
                .map(event -> {
                    EventSmallDto dto = mapper.toSmallDto(event);
                    if(searchDto.getLatitude() != null
                            && searchDto.getLongitude() != null
                            && dto.getCoordinates().longitude() != null
                            && dto.getCoordinates().latitude() != null)
                        dto.setDistance(
                                getDistance(new Coordinates(searchDto.getLatitude(), searchDto.getLongitude()),
                                dto.getCoordinates())
                        );
                    if(searchDto.getCurrencyId() != null && (searchDto.getPriceMin() != null || searchDto.getPriceMax() != null)) {
                        dto.setPrice(
                                currencyExchanger.exchange(
                                        event.getCurrency(),
                                        currencyRepository.findById(searchDto.getCurrencyId()).orElseThrow(
                                                () -> new UserException("Currency [id: %d] not found".formatted(searchDto.getCurrencyId()))),
                                        event.getPrice()
                                )
                        );
                        Currency currency = currencyRepository.findById(searchDto.getCurrencyId()).orElseThrow(
                                () -> new UserException("Currency [id: %d] not found".formatted(searchDto.getCurrencyId())));
                        dto.setCurrency(CurrencyDto.builder().name(currency.getName()).id(currency.getId()).symbol(currency.getSymbol()).build());
                    }

                    return dto;
        }).collect(Collectors.toList());
    }

    public TicketDto joinEvent(Long id) {
        User user = userService.getCurrentUserAsEntity();
        Participant participant = participantRepository.findById(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not a participant")
        );
        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(event.getPeopleCount() >= event.getCapacity())
            throw new UserException("Event is already full");

        TicketDto ticketDto = ticketService.issueTicket(event, participant);

        event.setPeopleCount(event.getPeopleCount() + 1);
        event = repository.save(event);

        ticketDto.setEvent(mapper.toDto(event));
        return ticketDto;
    }

    public TicketDto leaveEvent(Long id) {
        User user = userService.getCurrentUserAsEntity();
        Participant participant = participantRepository.findById(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not a participant")
        );
        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        TicketDto ticketDto = ticketService.cancelTicket(event, participant);
        event.setPeopleCount(event.getPeopleCount() - 1);
        event = repository.save(event);

        ticketDto.setEvent(mapper.toDto(event));
        return ticketDto;
    }

    public TicketDto getTicketForEvent(Long id) {
        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        User user = userService.getCurrentUserAsEntity();
        Participant participant = participantRepository.findById(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not a participant")
        );

        TicketDto ticketDto = ticketService.getTicketWithCode(event, participant);
        ticketDto.setEvent(mapper.toDto(event));
        return ticketDto;
    }

    public EventResponseDto saveEvent(Long id) {
        User user = userService.getCurrentUserAsEntity();
        Participant participant = participantRepository.findByIdWithSavedEvents(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not a participant")
        );
        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(participant.getSavedEvents().contains(event))
            throw new UserException("Event [id: %d] is already saved".formatted(id));

        participant.getSavedEvents().add(event);
        participantRepository.save(participant);

        event.setPeopleSaved(event.getPeopleSaved() + 1);
        event = repository.save(event);

        return getPersonalizedDto(event);
    }

    public EventResponseDto unsaveEvent(Long id) {
        User user = userService.getCurrentUserAsEntity();
        Participant participant = participantRepository.findByIdWithSavedEvents(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not a participant")
        );
        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(!participant.getSavedEvents().remove(event))
            throw new UserException("Event [id: %d] is not saved".formatted(id));

        participantRepository.save(participant);
        event.setPeopleSaved(event.getPeopleSaved() - 1);
        event = repository.save(event);

        return getPersonalizedDto(event);
    }

    public List<EventSmallDto> getSavedEvents() {
        User user = userService.getCurrentUserAsEntity();
        Participant participant = participantRepository.findByIdWithSavedEvents(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not a participant")
        );

        return mapper.toSmallDtos(participant.getSavedEvents());
    }

    private EventResponseDto getPersonalizedDto(Event event) {
        EventResponseDto dto = mapper.toDto(event);
        User user = userService.getCurrentUserAsEntity();
        Optional<Participant> participantOptional = participantRepository.findById(user.getId());
        participantOptional.ifPresent(
                participant -> {
                    dto.setIsRegistered(ticketService.ticketExists(event, participant));
                    dto.setIsSaved(participant.getSavedEvents().contains(event));
                }
        );
        return dto;
    }
}
