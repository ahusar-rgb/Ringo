package com.ringo.service.company.event;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.company.request.EventRequestDto;
import com.ringo.dto.company.request.TicketTypeRequestDto;
import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.dto.photo.PhotoDimensions;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventMapper;
import com.ringo.mapper.company.TicketTypeMapper;
import com.ringo.model.company.Currency;
import com.ringo.model.company.*;
import com.ringo.model.form.RegistrationForm;
import com.ringo.model.photo.EventMainPhoto;
import com.ringo.model.photo.EventPhoto;
import com.ringo.repository.company.CategoryRepository;
import com.ringo.repository.company.CurrencyRepository;
import com.ringo.repository.company.EventRepository;
import com.ringo.repository.photo.EventPhotoRepository;
import com.ringo.service.company.OrganisationService;
import com.ringo.service.company.RegistrationValidator;
import com.ringo.service.time.Time;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventService {
    private final ApplicationProperties config;
    private final EventRepository repository;
    private final EventMapper mapper;
    private final EventPhotoService eventPhotoService;
    private final EventPhotoRepository eventPhotoRepository;
    private final OrganisationService organisationService;
    private final CurrencyRepository currencyRepository;
    private final CategoryRepository categoryRepository;
    private final RegistrationValidator registrationValidator;
    private final EventCleanUpService eventCleanUpService;
    private final TicketTypeMapper ticketTypeMapper;

    public EventResponseDto create(EventRequestDto dto) {
        log.info("saveEvent: {}", dto);

        Organisation organisation = organisationService.getFullActiveUser();

        Set<Category> categories = dto.getCategoryIds().stream()
                .map(id -> categoryRepository.findById(id).orElseThrow(
                        () -> new NotFoundException("Category [id: %d] not found".formatted(id))
                )
        ).collect(Collectors.toSet());

        if((dto.getPrice() != null || dto.getCurrencyId() != null) && dto.getTicketTypes() != null) {
            throw new UserException("Event cannot have both price and ticket types");
        }

        Event event = mapper.toEntity(dto);
        event.setHost(organisation);

        setUpTicketTypes(event, dto);

        event.setCategories(categories);
        for (Category category : categories) {
            if(category.getEvents() == null)
                category.setEvents(new HashSet<>());
            category.getEvents().add(event);
        }

        if(dto.getCurrencyId() != null) {
            event.setCurrency(currencyRepository.findById(dto.getCurrencyId()).orElseThrow(
                    () -> new NotFoundException("Currency [id: %d] not found".formatted(dto.getCurrencyId()))
            ));
        }

        event.setIsActive(false);
        event.setCreatedAt(Time.getLocalUTC());

        return mapper.toDtoDetails(repository.save(event));
    }

    public EventResponseDto update(Long id, EventRequestDto dto) {
        log.info("updateEvent: {}, {}", id, dto);

        Event event = repository.findFullById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );
        throwIfNotHost(event);

        if((dto.getPrice() != null || dto.getCurrencyId() != null) && dto.getTicketTypes() != null) {
            throw new UserException("Event cannot have both price and ticket types");
        }

        mapper.partialUpdate(event, dto);
        event.setUpdatedAt(Time.getLocalUTC());

        setUpTicketTypes(event, dto);

        if(dto.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : dto.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId).orElseThrow(
                        () -> new NotFoundException("Category [id: %d] not found".formatted(categoryId))
                );
                categories.add(category);
                if(category.getEvents() == null)
                    category.setEvents(new HashSet<>());
                category.getEvents().add(event);
            }

        if(dto.getCurrencyId() != null) {
            event.setCurrency(currencyRepository.findById(dto.getCurrencyId()).orElseThrow(
                    () -> new NotFoundException("Currency [id: %d] not found".formatted(dto.getCurrencyId()))
            ));
        }

            event.setCategories(categories);
        }

        return mapper.toDtoDetails(repository.save(event));
    }

    public void delete(Long id) {
        log.info("deleteEvent: {}", id);

        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );
        throwIfNotHost(event);

        if(event.getPeopleCount() > 0)
            throw new UserException("Event cannot be deleted because it has participants");

        eventCleanUpService.cleanUpEvent(event);
        repository.deleteById(id);
    }

    public EventResponseDto addPhoto(Long eventId, MultipartFile photo) {
        return addPhoto(eventId, photo, null);
    }

    public EventResponseDto addPhoto(Long eventId, MultipartFile photo, PhotoDimensions dimensions) {
        log.info("addPhotoToEvent: {}, {}", eventId, photo.getOriginalFilename());
        log.info("photo type: {}", photo.getContentType());

        Event event = repository.findFullById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        if(event.getPhotos().size() >= config.getMaxPhotoCount())
            throw new UserException("No more photos for this event is allowed");

        eventPhotoService.save(event, photo, dimensions);
        event.setPhotoCount(event.getPhotoCount() + 1);
        repository.save(event);

        return mapper.toDtoDetails(event);
    }

    public EventResponseDto removePhoto(Long eventId, Long photoId) {
        log.info("removePhotoFromEvent: {}, {}", eventId, photoId);

        Event event = repository.findFullById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );

        throwIfNotHost(event);

        EventPhoto photo = eventPhotoRepository.findById(photoId).orElseThrow(
                () -> new NotFoundException("Photo [id: %d] not found".formatted(photoId))
        );

        if(event.getMainPhoto() != null && Objects.equals(event.getMainPhoto().getHighQualityPhoto().getId(), photoId))
            throw new UserException("Main photo cannot be removed");
        if(event.getIsActive() && event.getPhotos().size() <= 1)
            throw new UserException("Active event must have at least one photo");
        if(!event.getPhotos().contains(photo))
            throw new UserException("Photo [id: %d] is not owned by the event".formatted(photoId));

        eventPhotoService.delete(photo.getId());
        event.getPhotos().remove(photo);
        return mapper.toDtoDetails(repository.save(event));
    }

    public EventResponseDto setPhotoOrder(Long eventId, List<EventPhotoDto> photos) {
        log.info("setPhotoOrder: {}", eventId);

        Event event = repository.findFullById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        Long mainPhotoId = event.getMainPhoto() == null ? null : event.getMainPhoto().getHighQualityPhoto().getId();

        List<EventPhoto> newPhotos = new ArrayList<>();
        for(EventPhotoDto eventPhotoDto : photos) {
            EventPhoto eventPhoto = eventPhotoRepository.findById(eventPhotoDto.getId()).orElseThrow(
                    () -> new NotFoundException("Photo [id: %d] not found".formatted(eventPhotoDto.getId()))
            );

            if(Objects.equals(mainPhotoId, eventPhoto.getPhoto().getId()))
                throw new UserException("Main photo cannot be moved");

            eventPhoto.setOrdinal(eventPhotoDto.getOrdinal());

            newPhotos.add(eventPhoto);
        }

        HashSet<EventPhoto> currentEventPhotos = new HashSet<>();
        for(EventPhoto photo : event.getPhotos()) {
            if(Objects.equals(mainPhotoId, photo.getPhoto().getId()))
                continue;
            currentEventPhotos.add(photo);
        }

        if(!new HashSet<>(newPhotos).equals(currentEventPhotos)) {
            throw new UserException("Photos are not owned by the event");
        }

        event.setPhotos(newPhotos);
        return mapper.toDtoDetails(repository.save(event));
    }

    public EventResponseDto setMainPhoto(Long eventId, Long photoId) {
        log.info("setMainPhoto: {}", photoId);

        Event event = repository.findFullById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        EventMainPhoto eventMainPhoto = eventPhotoService.prepareMainPhoto(event, photoId);
        event.setMainPhoto(eventMainPhoto);
        return mapper.toDtoDetails(event);
    }

    public EventResponseDto removeMainPhoto(Long eventId) {
        log.info("removeMainPhoto: {}", eventId);

        Event event = repository.findFullById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        if(event.getIsActive())
            throw new UserException("Main photo cannot be removed from active event");

        eventPhotoService.removeMainPhoto(event);
        event.setMainPhoto(null);
        return mapper.toDtoDetails(event);
    }

    public EventResponseDto activate(Long eventId) {
        log.info("setActive: {}", eventId);

        Event event = repository.findFullById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        if(event.getPhotos() == null || event.getPhotos().isEmpty())
            throw new UserException("Event must have at least one photo");

        if(event.getMainPhoto() == null)
            throw new UserException("Event must have a main photo");

        event.setIsActive(true);
        return mapper.toDtoDetails(repository.save(event));
    }

    public EventResponseDto deactivate(Long eventId) {
        log.info("setInactive: {}", eventId);

        Event event = repository.findFullById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        if(!event.getIsActive())
            throw new UserException("Event is not active");

        event.setIsActive(false);
        return mapper.toDtoDetails(repository.save(event));
    }

    public EventResponseDto setRegistrationForm(Long id, RegistrationForm registrationForm) {
        log.info("setRegistrationForm: {}, {}", id, registrationForm);

        Event event = repository.findFullById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(event.getPeopleCount() > 0)
            throw new UserException("Cannot change registration form of event with participants");

        throwIfNotHost(event);
        registrationValidator.throwIfFormInvalid(registrationForm);

        event.setRegistrationForm(registrationForm);
        event = repository.save(event);
        return mapper.toDtoDetails(event);
    }

    public EventResponseDto removeRegistrationForm(Long id) {
        log.info("removeRegistrationForm: {}", id);

        Event event = repository.findFullById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        if(event.getPeopleCount() > 0)
            throw new UserException("Cannot remove registration form of event with participants");

        throwIfNotHost(event);
        return setRegistrationForm(id, null);
    }

    private void throwIfNotHost(Event event) {
        if(!event.getHost().getId().equals(organisationService.getFullActiveUser().getId()))
            throw new UserException("Event [id: %d] is not owned by the organisation".formatted(event.getId()));
    }

    private void setUpTicketTypes(Event event, EventRequestDto eventDto) {
        Set<Currency> currencies = new HashSet<>();
        if(eventDto.getTicketTypes() != null) {
            if(event.getTicketTypes() != null)
                event.getTicketTypes().clear();
            else
                event.setTicketTypes(new ArrayList<>());

            for (TicketTypeRequestDto ticketTypeDto : eventDto.getTicketTypes()) {
                TicketType ticketType = ticketTypeMapper.toEntity(ticketTypeDto);
                Currency currency = currencyRepository.findById(ticketTypeDto.getCurrencyId()).orElseThrow(
                        () -> new NotFoundException("Currency [id: %d] not found".formatted(ticketTypeDto.getCurrencyId()))
                );
                if(currencies.isEmpty())
                    currencies.add(currency);
                else if(!currencies.contains(currency))
                    throw new UserException("Event cannot have two ticket types with different currencies");
                ticketType.setCurrency(currency);
                ticketType.setEvent(event);
                ticketType.setPeopleCount(0);
                event.getTicketTypes().add(ticketType);
            }
        }
    }
}
