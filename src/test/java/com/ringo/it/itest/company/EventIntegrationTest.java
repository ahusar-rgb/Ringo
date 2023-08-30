package com.ringo.it.itest.company;

import com.ringo.dto.company.*;
import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.template.company.CategoryTemplate;
import com.ringo.it.template.company.CurrencyTemplate;
import com.ringo.it.template.company.EventTemplate;
import com.ringo.it.template.company.PhotoTemplate;
import com.ringo.it.util.ItTestConsts;
import com.ringo.mock.dto.CategoryDtoMock;
import com.ringo.mock.dto.CurrencyDtoMock;
import com.ringo.mock.dto.EventDtoMock;
import com.ringo.mock.model.RegistrationFormMock;
import com.ringo.model.form.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
public class EventIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventTemplate eventTemplate;
    @Autowired
    private CurrencyTemplate currencyTemplate;
    @Autowired
    private CategoryTemplate categoryTemplate;
    @Autowired
    private PhotoTemplate photoTemplate;


    @Test
    public void createActivatedEvent() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);
        assertThat(event.getName()).isEqualTo(eventRequestDto.getName());
        assertThat(event.getDescription()).isEqualTo(eventRequestDto.getDescription());
        assertThat(event.getAddress()).isEqualTo(eventRequestDto.getAddress());
        assertThat(event.getPrice()).isEqualTo(eventRequestDto.getPrice());
        assertThat(event.getCapacity()).isEqualTo(eventRequestDto.getCapacity());
        assertThat(event.getCoordinates()).isEqualTo(eventRequestDto.getCoordinates());
        assertThat(event.getIsTicketNeeded()).isEqualTo(eventRequestDto.getIsTicketNeeded());
        assertThat(event.getStartTime()).isEqualTo(eventRequestDto.getStartTime());
        assertThat(event.getEndTime()).isEqualTo(eventRequestDto.getEndTime());
        assertThat(event.getCurrency()).usingRecursiveComparison().ignoringFields("id").isEqualTo(currency);
        assertThat(event.getCategories()).isEqualTo(categories);
        assertThat(event.getHost()).usingRecursiveComparison().ignoringFields("email").isEqualTo(organisation);
        assertThat(event.getHost().getEmail()).isNull();

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());
        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found).isEqualTo(event);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    public void updateEvent() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);


        EventRequestDto updateEventRequestDto = new EventRequestDto();
        updateEventRequestDto.setName("new name");

        EventResponseDto updatedEvent = eventTemplate.update(organisationToken.getAccessToken(), event.getId(), updateEventRequestDto);
        assertThat(updatedEvent.getName()).isEqualTo(updateEventRequestDto.getName());
        assertThat(updatedEvent).usingRecursiveComparison().ignoringFields("name").isEqualTo(event);

        addPhotoAndActivate(organisationToken.getAccessToken(), updatedEvent.getId());

        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found).isEqualTo(updatedEvent);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void addPhotoSuccess() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());
        File photo = new File("src/test/java/com/ringo/resources/test_picture_2.jpeg");
        EventResponseDto addPhotoResponseDto = eventTemplate.addPhoto(organisationToken.getAccessToken(), event.getId(), photo, "image/jpeg");

        assertThat(addPhotoResponseDto.getPhotos().size()).isEqualTo(1);

        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found).isEqualTo(addPhotoResponseDto);

        assertThat(photoTemplate.findPhoto(addPhotoResponseDto.getPhotos().get(0).getNormalId(), ItTestConsts.HTTP_SUCCESS)).isNotNull();
        assertThat(photoTemplate.findPhoto(addPhotoResponseDto.getPhotos().get(0).getLazyId(), ItTestConsts.HTTP_SUCCESS)).isNotNull();

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());

        photoTemplate.findPhoto(addPhotoResponseDto.getPhotos().get(0).getNormalId(), ItTestConsts.HTTP_NOT_FOUND);
        photoTemplate.findPhoto(addPhotoResponseDto.getPhotos().get(0).getLazyId(), ItTestConsts.HTTP_NOT_FOUND);
        photoTemplate.findPhoto(addPhotoResponseDto.getMainPhoto().getHighQualityId(), ItTestConsts.HTTP_NOT_FOUND);
        photoTemplate.findPhoto(addPhotoResponseDto.getMainPhoto().getMediumQualityId(), ItTestConsts.HTTP_NOT_FOUND);
        photoTemplate.findPhoto(addPhotoResponseDto.getMainPhoto().getLowQualityId(), ItTestConsts.HTTP_NOT_FOUND);
    }

    @Test
    void setPhotoOrderSuccess() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());
        File photo1 = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");
        File photo2 = new File("src/test/java/com/ringo/resources/test_picture_2.jpeg");
        File photo3 = new File("src/test/java/com/ringo/resources/test_picture_3.png");
        eventTemplate.addPhoto(organisationToken.getAccessToken(), event.getId(), photo1, "image/jpeg");
        eventTemplate.addPhoto(organisationToken.getAccessToken(), event.getId(), photo2, "image/jpeg");
        EventResponseDto withPhotos = eventTemplate.addPhoto(organisationToken.getAccessToken(), event.getId(), photo3, "image/png");

        List<EventPhotoDto> photos = List.of(
                EventPhotoDto.builder()
                                .id(withPhotos.getPhotos().get(2).getId())
                                .ordinal(0)
                                .build(),
                EventPhotoDto.builder()
                                .id(withPhotos.getPhotos().get(0).getId())
                                .ordinal(1)
                                .build(),
                EventPhotoDto.builder()
                                .id(withPhotos.getPhotos().get(1).getId())
                                .ordinal(2)
                                .build()
        );

        EventResponseDto setPhotoOrderResponseDto = eventTemplate.setPhotoOrder(organisationToken.getAccessToken(), event.getId(), photos, ItTestConsts.HTTP_SUCCESS);
        assertThat(setPhotoOrderResponseDto.getPhotos().size()).isEqualTo(3);
        assertThat(setPhotoOrderResponseDto.getPhotos().get(0)).isEqualTo(withPhotos.getPhotos().get(2));
        assertThat(setPhotoOrderResponseDto.getPhotos().get(1)).isEqualTo(withPhotos.getPhotos().get(0));
        assertThat(setPhotoOrderResponseDto.getPhotos().get(2)).isEqualTo(withPhotos.getPhotos().get(1));

        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found).isEqualTo(setPhotoOrderResponseDto);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void setPhotoOrderPhotoNotFound() {

        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        List<EventPhotoDto> photos = List.of(
                EventPhotoDto.builder()
                        .id(System.currentTimeMillis())
                        .ordinal(0)
                        .build(),
                EventPhotoDto.builder()
                        .id(2L)
                        .ordinal(1)
                        .build(),
                EventPhotoDto.builder()
                        .id(3L)
                        .ordinal(2)
                        .build()
        );

        eventTemplate.setPhotoOrder(organisationToken.getAccessToken(), event.getId(), photos, ItTestConsts.HTTP_NOT_FOUND);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void setPhotoOrderNotOwnedByEvent() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);
        Long photoOfEvent1 = eventTemplate.addPhoto(organisationToken.getAccessToken(), event.getId(), new File("src/test/java/com/ringo/resources/test_picture_1.jpeg"), "image/jpeg")
                .getPhotos().get(0).getId();

        EventResponseDto event2 = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);
        Long photoOfEvent2 = eventTemplate.addPhoto(organisationToken.getAccessToken(), event2.getId(), new File("src/test/java/com/ringo/resources/test_picture_2.jpeg"), "image/jpeg")
                .getPhotos().get(0).getId();


        List<EventPhotoDto> photos = List.of(
                EventPhotoDto.builder()
                        .id(photoOfEvent2)
                        .ordinal(0)
                        .build(),
                EventPhotoDto.builder()
                        .id(photoOfEvent1)
                        .ordinal(1)
                        .build()
        );

        eventTemplate.setPhotoOrder(organisationToken.getAccessToken(), event.getId(), photos, ItTestConsts.HTTP_BAD_REQUEST);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        eventTemplate.delete(organisationToken.getAccessToken(), event2.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }


    @Test
    void addPhotoTooBig() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());
        File photo = new File("src/test/java/com/ringo/resources/test_picture_20mb.png");
        assertThatThrownBy(() -> eventTemplate.addPhoto(organisationToken.getAccessToken(), event.getId(), photo, "image/png"))
                .isInstanceOf(SocketException.class);


        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getPhotos().size()).isEqualTo(0);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void removePhotoSuccess() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());
        File photo = new File("src/test/java/com/ringo/resources/test_picture_2.jpeg");
        EventResponseDto addPhotoResponseDto = eventTemplate.addPhoto(organisationToken.getAccessToken(), event.getId(), photo, "image/jpeg");

        assertThat(addPhotoResponseDto.getPhotos().size()).isEqualTo(1);
        photoTemplate.findPhoto(addPhotoResponseDto.getPhotos().get(0).getNormalId(), ItTestConsts.HTTP_SUCCESS);

        EventResponseDto removePhotoResponseDto = eventTemplate.removePhoto(organisationToken.getAccessToken(), event.getId(), addPhotoResponseDto.getPhotos().get(0).getId());
        assertThat(removePhotoResponseDto.getPhotos().size()).isEqualTo(0);

        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found).isEqualTo(removePhotoResponseDto);

        photoTemplate.findPhoto(addPhotoResponseDto.getPhotos().get(0).getNormalId(), ItTestConsts.HTTP_NOT_FOUND);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void removeMainPhotoSuccess() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        File photo = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");
        EventResponseDto addPhotoResponseDto = eventTemplate.addPhoto(organisationToken.getAccessToken(), event.getId(), photo, "image/jpeg");
        eventTemplate.setMainPhoto(organisationToken.getAccessToken(), event.getId(), addPhotoResponseDto.getPhotos().get(0).getId());

        EventResponseDto beforeRemove = eventTemplate.findById(organisationToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(beforeRemove.getMainPhoto()).isNotNull();

        EventResponseDto removePhotoResponseDto = eventTemplate.removeMainPhoto(organisationToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(removePhotoResponseDto.getMainPhoto()).isNull();
        assertThat(removePhotoResponseDto.getPhotos().size()).isEqualTo(1);

        EventResponseDto found = eventTemplate.findById(organisationToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found).isEqualTo(removePhotoResponseDto);

        photoTemplate.findPhoto(removePhotoResponseDto.getPhotos().get(0).getNormalId(), ItTestConsts.HTTP_SUCCESS);
        photoTemplate.findPhoto(removePhotoResponseDto.getPhotos().get(0).getLazyId(), ItTestConsts.HTTP_SUCCESS);
        photoTemplate.findPhoto(beforeRemove.getMainPhoto().getMediumQualityId(), ItTestConsts.HTTP_NOT_FOUND);
        photoTemplate.findPhoto(beforeRemove.getMainPhoto().getLowQualityId(), ItTestConsts.HTTP_NOT_FOUND);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void removeMainPhotoActive() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());

        eventTemplate.removeMainPhoto(organisationToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_BAD_REQUEST);
        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getMainPhoto()).isNotNull();

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deactivateSuccess() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());

        EventResponseDto deactivateResponseDto = eventTemplate.deactivate(organisationToken.getAccessToken(), event.getId());
        assertThat(deactivateResponseDto.getIsActive()).isFalse();

        eventTemplate.findById(event.getId(), ItTestConsts.HTTP_NOT_FOUND);

        EventResponseDto found = eventTemplate.findById(organisationToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found).isEqualTo(deactivateResponseDto);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deleteOrganisationWithEventsSuccess() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);
        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());

        EventResponseDto activatedEvent = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);

        organisationTemplate.delete(organisationToken.getAccessToken());

        eventTemplate.findById(activatedEvent.getId(), ItTestConsts.HTTP_NOT_FOUND);
        photoTemplate.findPhoto(activatedEvent.getMainPhoto().getHighQualityId(), ItTestConsts.HTTP_NOT_FOUND);
        photoTemplate.findPhoto(activatedEvent.getMainPhoto().getMediumQualityId(), ItTestConsts.HTTP_NOT_FOUND);
        photoTemplate.findPhoto(activatedEvent.getMainPhoto().getLowQualityId(), ItTestConsts.HTTP_NOT_FOUND);
        organisationTemplate.findById(null, organisation.getId(), ItTestConsts.HTTP_NOT_FOUND);

        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
    }

    @Test
    void deleteOrganisationWithEventsWithTickets() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);
        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());

        TokenDto participantToken = createParticipantActivated();
        eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        organisationTemplate.delete(organisationToken.getAccessToken(), ItTestConsts.HTTP_BAD_REQUEST);

        eventTemplate.leaveEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void deleteCategoryWithEvents() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deleteCurrencyWithEvents() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        currencyTemplate.delete(adminToken, currency.getId(), ItTestConsts.HTTP_BAD_REQUEST);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deleteWithTickets() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);
        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());

        TokenDto participantToken = createParticipantActivated();

        eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_BAD_REQUEST);

        eventTemplate.leaveEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        participantTemplate.delete(participantToken.getAccessToken());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deleteParticipantWithTickets() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());


        TokenDto participantToken = createParticipantActivated();
        eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        participantTemplate.delete(participantToken.getAccessToken());

        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getPeopleCount()).isEqualTo(0);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void addRegistrationFormSuccess() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);
        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());

        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        EventResponseDto registrationFormResponseDto = eventTemplate.addRegistrationForm(organisationToken.getAccessToken(), event.getId(), registrationForm, ItTestConsts.HTTP_SUCCESS);

        assertThat(registrationFormResponseDto.getRegistrationForm()).usingRecursiveComparison().ignoringFields("id", "questions").isEqualTo(registrationForm);
        assertThat(registrationFormResponseDto.getRegistrationForm().getQuestions().size()).isEqualTo(registrationForm.getQuestions().size());
        assertThat(registrationFormResponseDto.getRegistrationForm().getQuestions().stream().allMatch(question -> question.getId() != null)).isTrue();

        List<Option> options = new ArrayList<>();
        for(Question question : registrationFormResponseDto.getRegistrationForm().getQuestions()) {
            if(question instanceof MultipleChoiceQuestion)
                options.addAll(((MultipleChoiceQuestion) question).getOptions());
            if(question instanceof CheckboxQuestion)
                options.addAll(((CheckboxQuestion) question).getOptions());
        }
        assertThat(options.stream().allMatch(option -> option.getId() != null)).isTrue();

        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found).isEqualTo(registrationFormResponseDto);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void addRegistrationForNotHost() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        TokenDto organisationToken2 = createOrganisationActivated();
        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        eventTemplate.addRegistrationForm(organisationToken2.getAccessToken(), event.getId(), registrationForm, ItTestConsts.HTTP_BAD_REQUEST);


        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken2.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void removeRegistrationFormSuccess() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);
        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());

        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        eventTemplate.addRegistrationForm(organisationToken.getAccessToken(), event.getId(), registrationForm, ItTestConsts.HTTP_SUCCESS);

        eventTemplate.removeRegistrationForm(organisationToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getRegistrationForm()).isNull();

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void removeRegistrationFormNotHost() {
        String adminToken = loginTemplate.getAdminToken();

        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.setCurrencyId(currency.getId());
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        TokenDto organisationToken = createOrganisationActivated();
        TokenDto organisationToken2 = createOrganisationActivated();
        EventResponseDto event = eventTemplate.create(organisationToken.getAccessToken(), eventRequestDto);

        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        eventTemplate.addRegistrationForm(organisationToken.getAccessToken(), event.getId(), registrationForm, ItTestConsts.HTTP_SUCCESS);

        eventTemplate.removeRegistrationForm(organisationToken2.getAccessToken(), event.getId(), ItTestConsts.HTTP_BAD_REQUEST);

        eventTemplate.delete(organisationToken.getAccessToken(), event.getId());
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
        organisationTemplate.delete(organisationToken2.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    private void addPhotoAndActivate(String token, Long eventId) {
        File photo = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");
        EventResponseDto addPhotoResponseDto = eventTemplate.addPhoto(token, eventId, photo, "image/jpeg");
        eventTemplate.setMainPhoto(token, eventId, addPhotoResponseDto.getPhotos().get(0).getId());
        eventTemplate.activate(token, eventId);
    }
}
