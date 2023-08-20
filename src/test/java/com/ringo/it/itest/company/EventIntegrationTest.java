package com.ringo.it.itest.company;

import com.ringo.dto.company.*;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.net.SocketException;
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

        addPhotoAndActivate(organisationToken.getAccessToken(), event.getId());

        EventResponseDto beforeRemove = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(beforeRemove.getMainPhoto()).isNotNull();

        EventResponseDto removePhotoResponseDto = eventTemplate.removeMainPhoto(organisationToken.getAccessToken(), event.getId());
        assertThat(removePhotoResponseDto.getMainPhoto()).isNull();
        assertThat(removePhotoResponseDto.getPhotos().size()).isEqualTo(1);

        EventResponseDto found = eventTemplate.findById(event.getId(), ItTestConsts.HTTP_SUCCESS);
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

    private void addPhotoAndActivate(String token, Long eventId) {
        File photo = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");
        EventResponseDto addPhotoResponseDto = eventTemplate.addPhoto(token, eventId, photo, "image/jpeg");
        eventTemplate.setMainPhoto(token, eventId, addPhotoResponseDto.getPhotos().get(0).getId());
        eventTemplate.activate(token, eventId);
    }
}