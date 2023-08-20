package com.ringo.unit;

import com.ringo.config.ApplicationProperties;
import com.ringo.config.Constants;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.*;
import com.ringo.mock.common.MultipartFileMock;
import com.ringo.mock.dto.EventDtoMock;
import com.ringo.mock.model.*;
import com.ringo.model.company.Category;
import com.ringo.model.company.Currency;
import com.ringo.model.company.Event;
import com.ringo.model.company.Organisation;
import com.ringo.model.photo.EventMainPhoto;
import com.ringo.model.photo.EventPhoto;
import com.ringo.repository.CategoryRepository;
import com.ringo.repository.CurrencyRepository;
import com.ringo.repository.EventRepository;
import com.ringo.repository.photo.EventPhotoRepository;
import com.ringo.service.company.OrganisationService;
import com.ringo.service.company.RegistrationValidator;
import com.ringo.service.company.event.EventCleanUpService;
import com.ringo.service.company.event.EventPhotoService;
import com.ringo.service.company.event.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventPhotoService eventPhotoService;
    @Spy
    private EventMapper eventMapper;
    @Mock
    private ApplicationProperties config;
    @Mock
    private EventPhotoRepository eventPhotoRepository;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventCleanUpService eventCleanUpService;
    @Mock
    private RegistrationValidator registrationValidator;
    @InjectMocks
    private EventService eventService;
    @Captor
    private ArgumentCaptor<Event> eventCaptor;


    @BeforeEach
    void init() {
        eventMapper = new EventMapperImpl();

        ReflectionTestUtils.setField(eventMapper, "eventMainPhotoMapper", new EventMainPhotoMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "currencyMapper", new CurrencyMapperImpl());

        OrganisationMapper organisationMapper = new OrganisationMapperImpl();
        ReflectionTestUtils.setField(organisationMapper, "labelMapper", new LabelMapperImpl());
        ReflectionTestUtils.setField(eventMapper, "organisationMapper", organisationMapper);

        ReflectionTestUtils.setField(eventService, "mapper", eventMapper);
    }

    @Test
    void createEventSuccess() {
        //given
        Event event = EventMock.getEventMock();

        Organisation organisation = OrganisationMock.getOrganisationMock();
        Currency currency = CurrencyMock.getCurrencyMock();
        List<Category> categories = List.of(
                CategoryMock.getCategoryMock(),
                CategoryMock.getCategoryMock(),
                CategoryMock.getCategoryMock()
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCategoryIds(List.of(categories.get(0).getId(), categories.get(1).getId(), categories.get(2).getId()));
        eventRequestDto.setCurrencyId(currency.getId());

        //when
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(currencyRepository.findById(eventRequestDto.getCurrencyId())).thenReturn(Optional.of(currency));
        when(categoryRepository.findById(eventRequestDto.getCategoryIds().get(0))).thenReturn(Optional.of(categories.get(0)));
        when(categoryRepository.findById(eventRequestDto.getCategoryIds().get(1))).thenReturn(Optional.of(categories.get(1)));
        when(categoryRepository.findById(eventRequestDto.getCategoryIds().get(2))).thenReturn(Optional.of(categories.get(2)));
        when(eventRepository.save(eventCaptor.capture())).thenReturn(event);

        //then
        EventResponseDto responseDto = eventService.create(eventRequestDto);

        Event saved = eventCaptor.getValue();
        assertThat(saved).usingRecursiveComparison().ignoringFields(
                "id",
                "createdAt",
                "currency",
                "startTime",
                "endTime",
                "categories",
                "host",
                "isActive"
        ).isEqualTo(event);

        assertThat(saved.getCurrency()).usingRecursiveComparison().ignoringFields("id").isEqualTo(currency);
        assertThat(saved.getCategories().size()).isEqualTo(3);
        assertThat(saved.getCategories().contains(categories.get(0))).isTrue();
        assertThat(saved.getCategories().contains(categories.get(1))).isTrue();
        assertThat(saved.getCategories().contains(categories.get(2))).isTrue();
        assertThat(saved.getHost()).usingRecursiveComparison().ignoringFields("id").isEqualTo(organisation);
        assertThat(saved.getStartTime().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT))).isEqualTo(eventRequestDto.getStartTime());
        assertThat(saved.getEndTime().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT))).isEqualTo(eventRequestDto.getEndTime());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getIsActive()).isFalse();

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getCoordinates().longitude()).isEqualTo(saved.getLongitude());
        assertThat(responseDto.getCoordinates().latitude()).isEqualTo(saved.getLatitude());
    }


    @Test
    void createEventCurrencyNotFound() {
        //given
        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();

        //when
        when(organisationService.getFullUser()).thenReturn(OrganisationMock.getOrganisationMock());
        when(currencyRepository.findById(eventRequestDto.getCurrencyId())).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> eventService.create(eventRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Currency [id: %d] not found".formatted(eventRequestDto.getCurrencyId()));
    }

    @Test
    void createEventCategoryNotFound() {
        //given
        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCategoryIds(List.of(1L));

        //when
        when(organisationService.getFullUser()).thenReturn(OrganisationMock.getOrganisationMock());
        when(currencyRepository.findById(eventRequestDto.getCurrencyId())).thenReturn(Optional.of(CurrencyMock.getCurrencyMock()));
        when(categoryRepository.findById(eventRequestDto.getCategoryIds().get(0))).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> eventService.create(eventRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category [id: %d] not found".formatted(eventRequestDto.getCategoryIds().get(0)));
    }

    @Test
    void updateEventChangeAllFieldsSuccess() {
        //given
        Event event = EventMock.getEventMock();

        Category category1 = CategoryMock.getCategoryMock();
        Category category2 = CategoryMock.getCategoryMock();
        Category category3 = CategoryMock.getCategoryMock();

        Currency currency = CurrencyMock.getCurrencyMock();
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        EventRequestDto eventRequestDto = new EventRequestDto();
        eventRequestDto.setName("New name");
        eventRequestDto.setDescription("New description");
        eventRequestDto.setAddress("New address");
        eventRequestDto.setPrice(2.0f);
        eventRequestDto.setCapacity(200);
        eventRequestDto.setCoordinates(new Coordinates(60.0, 65.0));
        eventRequestDto.setIsTicketNeeded(false);
        eventRequestDto.setStartTime("1990-01-01T01:01:00");
        eventRequestDto.setEndTime("2090-01-01T02:01:00.100");
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(List.of(category1.getId(), category2.getId(), category3.getId()));

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(currencyRepository.findById(eventRequestDto.getCurrencyId())).thenReturn(Optional.of(currency));
        when(categoryRepository.findById(eventRequestDto.getCategoryIds().get(0))).thenReturn(Optional.of(category1));
        when(categoryRepository.findById(eventRequestDto.getCategoryIds().get(1))).thenReturn(Optional.of(category2));
        when(categoryRepository.findById(eventRequestDto.getCategoryIds().get(2))).thenReturn(Optional.of(category3));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventRepository.save(eventCaptor.capture())).thenReturn(event);

        //then
        EventResponseDto responseDto = eventService.update(event.getId(), eventRequestDto);

        Event saved = eventCaptor.getValue();
        assertThat(saved.getName()).isEqualTo(eventRequestDto.getName());
        assertThat(saved.getDescription()).isEqualTo(eventRequestDto.getDescription());
        assertThat(saved.getAddress()).isEqualTo(eventRequestDto.getAddress());
        assertThat(saved.getPrice()).isEqualTo(eventRequestDto.getPrice());
        assertThat(saved.getCapacity()).isEqualTo(eventRequestDto.getCapacity());
        assertThat(saved.getLatitude()).isEqualTo(eventRequestDto.getCoordinates().latitude());
        assertThat(saved.getLongitude()).isEqualTo(eventRequestDto.getCoordinates().longitude());
        assertThat(saved.getIsTicketNeeded()).isEqualTo(eventRequestDto.getIsTicketNeeded());
        assertThat(saved.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))).isEqualTo(eventRequestDto.getStartTime());
        assertThat(saved.getEndTime().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT))).isEqualTo(eventRequestDto.getEndTime());
        assertThat(saved.getCurrency()).usingRecursiveComparison().ignoringFields("id").isEqualTo(currency);
        assertThat(saved.getCategories().size()).isEqualTo(3);
        assertThat(saved.getCategories().contains(category1)).isTrue();
        assertThat(saved.getCategories().contains(category2)).isTrue();
        assertThat(saved.getCategories().contains(category3)).isTrue();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getHost().getId()).isEqualTo(organisation.getId());
    }

    @Test
    void updateEventChangeOneField() {
        //given
        Event event = EventMock.getEventMock();

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        EventRequestDto eventRequestDto = new EventRequestDto();
        eventRequestDto.setName("New name");

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventRepository.save(eventCaptor.capture())).thenReturn(event);

        //then
        EventResponseDto responseDto = eventService.update(event.getId(), eventRequestDto);

        Event saved = eventCaptor.getValue();
        assertThat(saved.getName()).isEqualTo(eventRequestDto.getName());
        assertThat(saved).usingRecursiveComparison().ignoringFields("name").isEqualTo(event);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getHost().getId()).isEqualTo(organisation.getId());
    }

    @Test
    void updateHostNotAuthor() {
        //given
        Event event = EventMock.getEventMock();

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId() + 1);

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);

        //then
        assertThatThrownBy(() -> eventService.update(event.getId(), EventDtoMock.getEventDtoMock()))
                .isInstanceOf(UserException.class)
                .hasMessage("Event [id: %d] is not owned by the organisation".formatted(event.getId()));
    }

    @Test
    void updateNotFound() {
        //given
        Event event = EventMock.getEventMock();

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> eventService.update(event.getId(), EventDtoMock.getEventDtoMock()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event [id: %d] not found".formatted(event.getId()));
    }

    @Test
    void deleteEventSuccess() {
        //given
        Event event = EventMock.getEventMock();
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);

        //then
        eventService.delete(event.getId());
        verify(eventRepository, times(1)).deleteById(event.getId());
        verify(eventRepository, times(1)).findById(event.getId());
        verify(eventCleanUpService, times(1)).cleanUpEvent(event);
    }

    @Test
    void deleteEventNotFound() {
        //given
        Event event = EventMock.getEventMock();

        //when
        when(eventRepository.findById(event.getId())).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> eventService.delete(event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event [id: %d] not found".formatted(event.getId()));

        verify(eventPhotoRepository, never()).delete(any());
        verify(eventPhotoService, never()).removeMainPhoto(any());
        verify(eventRepository, never()).deleteById(any());
    }

    @Test
    void deleteEventUserNotAuthor() {
        //given
        Event event = EventMock.getEventMock();
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId() + 1);

        //when
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);

        //then
        assertThatThrownBy(() -> eventService.delete(event.getId()))
                .isInstanceOf(UserException.class)
                .hasMessage("Event [id: %d] is not owned by the organisation".formatted(event.getId()));

        verify(eventPhotoRepository, never()).delete(any());
        verify(eventPhotoService, never()).removeMainPhoto(any());
        verify(eventRepository, never()).deleteById(any());
    }

    @Test
    void addPhotoSuccess() {
        //given
        Event event = EventMock.getEventMock();
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());
        MultipartFile photo = MultipartFileMock.getMockMultipartFile();

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(config.getMaxPhotoCount()).thenReturn(10);

        //then
        eventService.addPhoto(event.getId(), photo);

        verify(eventPhotoService, times(1)).save(event, photo);
        verify(eventRepository, times(1)).save(eventCaptor.capture());

        Event saved = eventCaptor.getValue();
        assertThat(saved.getPhotoCount()).isEqualTo(1);
    }


    @Test
    void changePhotoSuccess() {
        //given
        Event event = EventMock.getEventMock();
        List<EventPhoto> photoList = List.of(
                EventPhotoMock.getEventPhotoMock(),
                EventPhotoMock.getEventPhotoMock(),
                EventPhotoMock.getEventPhotoMock()
        );
        event.setPhotos(List.copyOf(photoList));
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        List<EventPhotoDto> setPhotoOrderDto = List.of(
                EventPhotoDto.builder()
                                .id(event.getPhotos().get(1).getId())
                                .ordinal(0)
                                .build(),
                EventPhotoDto.builder()
                                .id(event.getPhotos().get(2).getId())
                                .ordinal(1)
                                .build(),
                EventPhotoDto.builder()
                                .id(event.getPhotos().get(0).getId())
                                .ordinal(2)
                                .build()
        );

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventPhotoRepository.findById(setPhotoOrderDto.get(0).getId())).thenReturn(Optional.of(event.getPhotos().get(1)));
        when(eventPhotoRepository.findById(setPhotoOrderDto.get(1).getId())).thenReturn(Optional.of(event.getPhotos().get(2)));
        when(eventPhotoRepository.findById(setPhotoOrderDto.get(2).getId())).thenReturn(Optional.of(event.getPhotos().get(0)));

        //then
        eventService.setPhotoOrder(event.getId(), setPhotoOrderDto);

        verify(eventRepository, times(1)).save(eventCaptor.capture());
        verify(eventPhotoService, never()).save(any(), any());

        Event saved = eventCaptor.getValue();
        assertThat(saved.getPhotos().size()).isEqualTo(3);
        assertThat(saved.getPhotos().get(0).getId()).isEqualTo(photoList.get(1).getId());
        assertThat(saved.getPhotos().get(1).getId()).isEqualTo(photoList.get(2).getId());
        assertThat(saved.getPhotos().get(2).getId()).isEqualTo(photoList.get(0).getId());
    }


    @Test
    void changePhotoNotOwnedByEvent() {
        //given
        Event event = EventMock.getEventMock();
        event.setPhotos(List.of(
                EventPhotoMock.getEventPhotoMock(),
                EventPhotoMock.getEventPhotoMock(),
                EventPhotoMock.getEventPhotoMock()
        ));
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        List<EventPhotoDto> setPhotoOrderDto = List.of(
                EventPhotoDto.builder()
                        .id(event.getPhotos().get(1).getId())
                        .ordinal(0)
                        .build(),
                EventPhotoDto.builder()
                        .id(event.getPhotos().get(2).getId())
                        .ordinal(1)
                        .build(),
                EventPhotoDto.builder()
                        .id(event.getPhotos().get(0).getId())
                        .ordinal(2)
                        .build()
        );

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventPhotoRepository.findById(setPhotoOrderDto.get(0).getId())).thenReturn(Optional.of(EventPhotoMock.getEventPhotoMock()));
        when(eventPhotoRepository.findById(setPhotoOrderDto.get(1).getId())).thenReturn(Optional.of(EventPhotoMock.getEventPhotoMock()));
        when(eventPhotoRepository.findById(setPhotoOrderDto.get(2).getId())).thenReturn(Optional.of(EventPhotoMock.getEventPhotoMock()));

        //then
        assertThatThrownBy(() -> eventService.setPhotoOrder(event.getId(), setPhotoOrderDto))
                .isInstanceOf(UserException.class)
                .hasMessage("Photos are not owned by the event");
    }


    @Test
    void setPhotoOrderIncludedMain() {
        //given
        Event event = EventMock.getEventMock();
        event.setPhotos(List.of(
                EventPhotoMock.getEventPhotoMock(),
                EventPhotoMock.getEventPhotoMock(),
                EventPhotoMock.getEventPhotoMock()
        ));
        event.setMainPhoto(EventMainPhoto.builder()
                .highQualityPhoto(event.getPhotos().get(2).getPhoto()).build());
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        List<EventPhotoDto> setPhotoOrderDto = List.of(
                EventPhotoDto.builder()
                        .id(event.getPhotos().get(1).getId())
                        .ordinal(0)
                        .build(),
                EventPhotoDto.builder()
                        .id(event.getPhotos().get(2).getId())
                        .ordinal(1)
                        .build(),
                EventPhotoDto.builder()
                        .id(event.getPhotos().get(0).getId())
                        .ordinal(2)
                        .build()
        );

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventPhotoRepository.findById(setPhotoOrderDto.get(0).getId())).thenReturn(Optional.of(event.getPhotos().get(1)));
        when(eventPhotoRepository.findById(setPhotoOrderDto.get(1).getId())).thenReturn(Optional.of(event.getPhotos().get(2)));

        //then
        assertThatThrownBy(() -> eventService.setPhotoOrder(event.getId(), setPhotoOrderDto))
                .isInstanceOf(UserException.class)
                .hasMessage("Main photo cannot be moved");
    }


    @Test
    void addPhotoMoreThanMax() {
        //given
        Event event = EventMock.getEventMock();

        event.setPhotos(new ArrayList<>());
        for(int i = 0; i < 10; i++) {
            event.getPhotos().add(EventPhotoMock.getEventPhotoMock());
        }

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());
        MultipartFile photo = MultipartFileMock.getMockMultipartFile();

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(config.getMaxPhotoCount()).thenReturn(10);

        //then
        assertThatThrownBy(() -> eventService.addPhoto(event.getId(), photo))
                .isInstanceOf(UserException.class)
                .hasMessage("No more photos for this event is allowed");

        verify(eventPhotoService, never()).save(any(), any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void setMainPhotoSuccess() {
        //given
        Event event = EventMock.getEventMock();
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventPhotoService.prepareMainPhoto(event, 1L)).thenReturn(new EventMainPhoto());
        //then
        EventResponseDto dto = eventService.setMainPhoto(event.getId(), 1L);
        assertThat(dto.getMainPhoto()).isNotNull();

        verify(eventPhotoService, times(1)).prepareMainPhoto(event, 1L);
    }

    @Test
    void removeMainPhotoSuccess() {
        //given
        Event event = EventMock.getEventMock();
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);

        //then
        EventResponseDto dto = eventService.removeMainPhoto(event.getId());
        assertThat(dto.getMainPhoto()).isNull();

        verify(eventPhotoService, times(1)).removeMainPhoto(event);
    }

    @Test
    void removePhotoSuccess() {
        //given
        Event event = EventMock.getEventMock();

        event.setPhotos(new ArrayList<>());
        event.getPhotos().add(EventPhotoMock.getEventPhotoMock());
        event.getPhotos().add(EventPhotoMock.getEventPhotoMock());


        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());


        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventPhotoRepository.findById(event.getPhotos().get(0).getId())).thenReturn(Optional.of(event.getPhotos().get(0)));

        //then
        Long photoId = event.getPhotos().get(0).getId();
        eventService.removePhoto(event.getId(), photoId);

        verify(eventRepository, times(1)).save(eventCaptor.capture());
        verify(eventPhotoService, times(1)).delete(photoId);

        Event saved = eventCaptor.getValue();
        assertThat(saved.getPhotos().size()).isEqualTo(1);
    }

    @Test
    void removePhotoNotOwnedByEvent() {
        //given
        Event event = EventMock.getEventMock();
        event.setPhotos(new ArrayList<>());
        event.getPhotos().add(EventPhotoMock.getEventPhotoMock());
        event.getPhotos().add(EventPhotoMock.getEventPhotoMock());

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        EventPhoto photo = EventPhotoMock.getEventPhotoMock();
        Long photoId = 1L;

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventPhotoRepository.findById(photoId)).thenReturn(Optional.of(photo));

        //then
        assertThatThrownBy(() -> eventService.removePhoto(event.getId(), photoId))
                .isInstanceOf(UserException.class)
                .hasMessage("Photo [id: %d] is not owned by the event".formatted(photoId));

        verify(eventRepository, never()).save(any());
        verify(eventPhotoService, never()).delete(any());
    }

    @Test
    void removePhotoOnlyOnePhotoLeft() {
        //given
        Event event = EventMock.getEventMock();
        event.setPhotos(new ArrayList<>());
        event.getPhotos().add(EventPhotoMock.getEventPhotoMock());

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);
        when(eventPhotoRepository.findById(event.getPhotos().get(0).getId())).thenReturn(Optional.of(event.getPhotos().get(0)));

        //then
        assertThatThrownBy(() -> eventService.removePhoto(event.getId(), event.getPhotos().get(0).getId()))
                .isInstanceOf(UserException.class)
                .hasMessage("Event must have at least one photo");

        verify(eventRepository, never()).save(any());
        verify(eventPhotoService, never()).delete(any());
    }

    @Test
    void activateSuccess() {
        //given
        Event event = EventMock.getEventMock();
        event.setIsActive(false);
        event.setMainPhoto(new EventMainPhoto());
        event.setPhotos(List.of(EventPhoto.builder().id(1L).build(), EventPhoto.builder().id(2L).build()));

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);

        //then
        eventService.activate(event.getId());

        verify(eventRepository, times(1)).save(eventCaptor.capture());

        Event saved = eventCaptor.getValue();
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    void activateNoMainPhoto() {
        //given
        Event event = EventMock.getEventMock();
        event.setIsActive(false);
        event.setPhotos(List.of(EventPhoto.builder().id(1L).build(), EventPhoto.builder().id(2L).build()));

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);

        //then
        assertThatThrownBy(() -> eventService.activate(event.getId()))
                .isInstanceOf(UserException.class)
                .hasMessage("Event must have a main photo");

        verify(eventRepository, never()).save(any());
    }

    @Test
    void deactivateSuccess() {
        //given
        Event event = EventMock.getEventMock();
        event.setIsActive(true);
        event.setMainPhoto(new EventMainPhoto());

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);

        //then
        eventService.deactivate(event.getId());

        verify(eventRepository, times(1)).save(eventCaptor.capture());
        Event saved = eventCaptor.getValue();
        assertThat(saved.getIsActive()).isFalse();
    }

    @Test
    void deactivateEventNotActive() {
        //given
        Event event = EventMock.getEventMock();
        event.setIsActive(false);

        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(event.getHost().getId());

        //when
        when(eventRepository.findFullById(event.getId())).thenReturn(Optional.of(event));
        when(organisationService.getFullUser()).thenReturn(organisation);

        //then
        assertThatThrownBy(() -> eventService.deactivate(event.getId()))
                .isInstanceOf(UserException.class)
                .hasMessage("Event is not active");

        verify(eventRepository, never()).save(any());
    }
}
