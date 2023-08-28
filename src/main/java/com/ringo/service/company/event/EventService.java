package com.ringo.service.company.event;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventMapper;
import com.ringo.model.company.Category;
import com.ringo.model.company.Currency;
import com.ringo.model.company.Event;
import com.ringo.model.company.Organisation;
import com.ringo.model.form.RegistrationForm;
import com.ringo.model.photo.EventMainPhoto;
import com.ringo.model.photo.EventPhoto;
import com.ringo.repository.CategoryRepository;
import com.ringo.repository.CurrencyRepository;
import com.ringo.repository.EventRepository;
import com.ringo.repository.photo.EventPhotoRepository;
import com.ringo.service.company.OrganisationService;
import com.ringo.service.company.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    public EventResponseDto create(EventRequestDto eventDto) {
        log.info("saveEvent: {}", eventDto);

        Organisation organisation = organisationService.getFullActiveUser();

        Currency currency = currencyRepository.findById(eventDto.getCurrencyId()).orElseThrow(
                () -> new NotFoundException("Currency [id: %d] not found".formatted(eventDto.getCurrencyId()))
        );
        Set<Category> categories = eventDto.getCategoryIds().stream()
                .map(id -> categoryRepository.findById(id).orElseThrow(
                        () -> new NotFoundException("Category [id: %d] not found".formatted(id))
                )
        ).collect(Collectors.toSet());


        Event event = mapper.toEntity(eventDto);
        event.setHost(organisation);
        event.setCurrency(currency);

        event.setCategories(categories);
        for (Category category : categories) {
            if(category.getEvents() == null)
                category.setEvents(new HashSet<>());
            category.getEvents().add(event);
        }

        event.setIsActive(false);
        event.setCreatedAt(LocalDateTime.now());

        return mapper.toDtoDetails(repository.save(event));
    }

    public EventResponseDto update(Long id, EventRequestDto dto) {
        log.info("updateEvent: {}, {}", id, dto);

        Event event = repository.findFullById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );
        throwIfNotHost(event);

        mapper.partialUpdate(event, dto);
        event.setUpdatedAt(LocalDateTime.now());

        if(dto.getCurrencyId() != null) {
            Currency currency = currencyRepository.findById(dto.getCurrencyId()).orElseThrow(
                    () -> new NotFoundException("Currency [id: %d] not found".formatted(dto.getCurrencyId()))
            );
            event.setCurrency(currency);
        }

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
        log.info("addPhotoToEvent: {}, {}", eventId, photo.getOriginalFilename());
        log.info("photo type: {}", photo.getContentType());

        Event event = repository.findFullById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        if(event.getPhotos().size() >= config.getMaxPhotoCount())
            throw new UserException("No more photos for this event is allowed");

        eventPhotoService.save(event, photo);
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
        if(event.getPhotos().size() <= 1)
            throw new UserException("Event must have at least one photo");
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

            eventPhoto.setOrdinal(eventPhoto.getOrdinal());

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
        throwIfNotHost(event);
        return setRegistrationForm(id, null);
    }

    private void throwIfNotHost(Event event) {
        if(!event.getHost().getId().equals(organisationService.getFullActiveUser().getId()))
            throw new UserException("Event [id: %d] is not owned by the organisation".formatted(event.getId()));
    }
}
