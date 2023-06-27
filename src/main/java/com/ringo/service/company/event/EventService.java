package com.ringo.service.company.event;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventMapper;
import com.ringo.model.common.AbstractEntity;
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
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    private final UserService userService;
    private final RegistrationValidator registrationValidator;

    public EventResponseDto create(EventRequestDto eventDto) {
        log.info("saveEvent: {}", eventDto);

        Organisation organisation = organisationService.getCurrentUserAsOrganisationIfActive();

        Currency currency = currencyRepository.findById(eventDto.getCurrencyId()).orElseThrow(
                () -> new NotFoundException("Currency [id: %d] not found".formatted(eventDto.getCurrencyId()))
        );
        List<Category> categories = eventDto.getCategoryIds().stream()
                .map(id -> categoryRepository.findById(id).orElseThrow(
                        () -> new NotFoundException("Category [id: %d] not found".formatted(id))
                )
        ).toList();


        Event event = mapper.toEntity(eventDto);
        event.setHost(organisation);
        event.setCurrency(currency);
        event.setCategories(categories);
        event.setIsActive(false);
        event.setCreatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(event));
    }

    public EventResponseDto update(Long id, EventRequestDto dto) {
        log.info("updateEvent: {}, {}", id, dto);

        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );
        throwIfNotHost(event);

        mapper.partialUpdate(event, dto);
        event.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(event));
    }

    public void delete(Long id) {
        log.info("deleteEvent: {}", id);

        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );
        throwIfNotHost(event);

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
        throwIfNotHost(event);

        if(event.getPhotos().size() >= config.getMaxPhotoCount())
            throw new UserException("No more photos for this event is allowed");

        eventPhotoService.save(event, photo);
        event.setPhotoCount(event.getPhotoCount() + 1);
        repository.save(event);

        return mapper.toDto(event);
    }

    public EventResponseDto removePhoto(Long eventId, Long photoId) {
        log.info("removePhotoFromEvent: {}, {}", eventId, photoId);

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );

        EventPhoto photo = eventPhotoRepository.findById(photoId).orElseThrow(
                () -> new NotFoundException("Photo [id: %d] not found".formatted(photoId))
        );

        if(Objects.equals(event.getMainPhoto().getHighQualityPhoto().getId(), photoId))
            throw new UserException("Main photo cannot be removed");

        throwIfNotHost(event);

        eventPhotoService.delete(photo.getId());
        return mapper.toDto(repository.save(event));
    }

    public EventResponseDto setMainPhoto(Long eventId, Long photoId) {
        log.info("setMainPhoto: {}", photoId);

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        EventMainPhoto eventMainPhoto = eventPhotoService.prepareMainPhoto(event, photoId);
        event.setMainPhoto(eventMainPhoto);
        return mapper.toDto(event);
    }

    public EventResponseDto removeMainPhoto(Long eventId) {
        log.info("removeMainPhoto: {}", eventId);

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

        eventPhotoService.removeMainPhoto(event);
        event.setMainPhoto(null);
        return mapper.toDto(event);
    }

    public EventResponseDto activate(Long eventId) {
        log.info("setActive: {}", eventId);

        Event event = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(eventId))
        );
        throwIfNotHost(event);

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
        throwIfNotHost(event);

        event.setIsActive(false);
        return mapper.toDto(repository.save(event));
    }

    public EventResponseDto setRegistrationForm(Long id, RegistrationForm registrationForm) {
        log.info("setRegistrationForm: {}, {}", id, registrationForm);

        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );

        throwIfNotHost(event);
        registrationValidator.throwIfFormInvalid(registrationForm);

        event.setRegistrationForm(registrationForm);
        event = repository.save(event);
        return mapper.toDto(event);
    }

    public EventResponseDto removeRegistrationForm(Long id) {
        log.info("removeRegistrationForm: {}", id);

        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );
        throwIfNotHost(event);
        return setRegistrationForm(id, null);
    }

    private void throwIfNotHost(Event event) {
        if(!event.getHost().equals(userService.getCurrentUserIfActive()))
            throw new UserException("Event [id: %d] is not owned by the organisation".formatted(event.getId()));
    }
}
