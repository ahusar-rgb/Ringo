package com.ringo.it.itest.company;

import com.ringo.dto.company.CategoryDto;
import com.ringo.dto.company.CurrencyDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.dto.company.EventSmallDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.template.company.CategoryTemplate;
import com.ringo.it.template.company.CurrencyTemplate;
import com.ringo.it.template.company.EventTemplate;
import com.ringo.mock.dto.CategoryDtoMock;
import com.ringo.mock.dto.CurrencyDtoMock;
import com.ringo.mock.dto.EventDtoMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class EventSearchIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventTemplate eventTemplate;
    @Autowired
    private CurrencyTemplate currencyTemplate;
    @Autowired
    private CategoryTemplate categoryTemplate;

    @Test
    void searchActive() {
        String adminToken = loginTemplate.getAdminToken();
        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        String organisationToken = createOrganisationActivated().getAccessToken();

        //creating events
        List<EventResponseDto> events = Stream.of(
                EventDtoMock.getEventDtoMock().toBuilder()
                        .currencyId(currency.getId())
                        .categoryIds(List.of(categories.get(0).getId()))
                .build(),
                EventDtoMock.getEventDtoMock().toBuilder()
                        .currencyId(currency.getId())
                        .categoryIds(List.of(categories.get(0).getId(), categories.get(1).getId()))
                .build(),
                EventDtoMock.getEventDtoMock().toBuilder()
                        .currencyId(currency.getId())
                        .categoryIds(List.of(categories.get(0).getId(), categories.get(1).getId(), categories.get(2).getId()))
                .build()
        ).map(event -> eventTemplate.create(organisationToken, event)).toList();
        events.forEach(event -> addPhotoAndActivate(organisationToken, event.getId()));
        //events created

        //searching
        List<EventSmallDto> foundWithCategory0 = eventTemplate.search(null, "categoryIds=" + categories.get(0).getId());
        List<EventSmallDto> foundWithCategory1 = eventTemplate.search(null, "categoryIds=" + categories.get(1).getId());
        List<EventSmallDto> foundWithCategory1or2 = eventTemplate.search(null, "categoryIds=" + categories.get(1).getId() + "," + categories.get(2).getId());
        List<EventSmallDto> foundByName = eventTemplate.search(null, "search=" + events.get(0).getName());
        List<EventSmallDto> foundByDescription = eventTemplate.search(null, "search=" + events.get(0).getDescription());

        //checking
        assertThat(foundWithCategory0).hasSize(3);
        assertThat(foundWithCategory1).hasSize(2);
        assertThat(foundWithCategory1or2).hasSize(2);

        assertThat(foundByName).hasSize(3);
        assertThat(foundByName.get(0).getName()).isEqualTo(events.get(0).getName());

        assertThat(foundByDescription).hasSize(3);
        assertThat(foundByDescription.get(0).getDescription()).isEqualTo(events.get(0).getDescription());

        events.forEach(event -> eventTemplate.delete(organisationToken, event.getId()));
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
    }


    @Test
    void searchOneInactive() {
        String adminToken = loginTemplate.getAdminToken();
        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        String organisationToken = createOrganisationActivated().getAccessToken();

        //creating events
        List<EventResponseDto> events = Stream.of(
                EventDtoMock.getEventDtoMock().toBuilder()
                        .currencyId(currency.getId())
                        .categoryIds(List.of(categories.get(0).getId()))
                        .build(),
                EventDtoMock.getEventDtoMock().toBuilder()
                        .currencyId(currency.getId())
                        .categoryIds(List.of(categories.get(0).getId(), categories.get(1).getId()))
                        .build(),
                EventDtoMock.getEventDtoMock().toBuilder()
                        .currencyId(currency.getId())
                        .categoryIds(List.of(categories.get(0).getId(), categories.get(1).getId(), categories.get(2).getId()))
                        .build()
        ).map(event -> eventTemplate.create(organisationToken, event)).toList();
        addPhotoAndActivate(organisationToken, events.get(0).getId());
        addPhotoAndActivate(organisationToken, events.get(1).getId());
        //events created

        //searching
        List<EventSmallDto> foundByUser = eventTemplate.search(null, null);
        List<EventSmallDto> foundByOrganisation = eventTemplate.search(organisationToken, null);

        //checking
        assertThat(foundByUser).hasSize(2);
        assertThat(foundByOrganisation).hasSize(3);

        events.forEach(event -> eventTemplate.delete(organisationToken, event.getId()));
        categories.forEach(category -> categoryTemplate.delete(adminToken, category.getId()));
        currencyTemplate.delete(adminToken, currency.getId());
    }

    private void addPhotoAndActivate(String token, Long eventId) {
        File photo = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");
        EventResponseDto addPhotoResponseDto = eventTemplate.addPhoto(token, eventId, photo, "image/jpeg");
        eventTemplate.setMainPhoto(token, eventId, addPhotoResponseDto.getPhotos().get(0).getId());
        eventTemplate.activate(token, eventId);
    }
}
