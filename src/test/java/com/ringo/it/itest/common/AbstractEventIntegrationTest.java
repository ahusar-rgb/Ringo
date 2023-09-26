package com.ringo.it.itest.common;

import com.ringo.dto.company.CategoryDto;
import com.ringo.dto.company.CurrencyDto;
import com.ringo.dto.company.request.EventRequestDto;
import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.it.template.company.CategoryTemplate;
import com.ringo.it.template.company.CurrencyTemplate;
import com.ringo.it.template.company.EventTemplate;
import com.ringo.mock.dto.CategoryDtoMock;
import com.ringo.mock.dto.CurrencyDtoMock;
import com.ringo.mock.dto.EventDtoMock;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractEventIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected EventTemplate eventTemplate;
    @Autowired
    private CurrencyTemplate currencyTemplate;
    @Autowired
    private CategoryTemplate categoryTemplate;


    protected EventResponseDto createEventActivated(String adminToken, String organisationToken) {
        CurrencyDto currency = currencyTemplate.create(adminToken, CurrencyDtoMock.getCurrencyDtoMock());
        List<CategoryDto> categories = List.of(
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock()),
                categoryTemplate.create(adminToken, CategoryDtoMock.getCategoryDtoMock())
        );

        EventRequestDto eventRequestDto = EventDtoMock.getEventDtoMock();
        eventRequestDto.getTicketTypes().forEach(ticketTypeRequestDto -> ticketTypeRequestDto.setCurrencyId(currency.getId()));
        eventRequestDto.getTicketTypes().get(0).setPrice(0f);
        eventRequestDto.setCategoryIds(categories.stream().map(CategoryDto::getId).toList());

        EventResponseDto created = eventTemplate.create(organisationToken, eventRequestDto);

        File photo = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");
        EventResponseDto addPhotoResponseDto = eventTemplate.addPhoto(organisationToken, created.getId(), photo, "image/jpeg");
        eventTemplate.setMainPhoto(organisationToken, created.getId(), addPhotoResponseDto.getPhotos().get(0).getId());

        return eventTemplate.activate(organisationToken, created.getId());
    }

    protected void cleanUpEvent(String adminToken, String organisationToken, EventResponseDto eventResponseDto) {
        eventTemplate.delete(organisationToken, eventResponseDto.getId());
        eventResponseDto.getCategories().forEach(category -> categoryTemplate.delete(adminToken, category.getId()));

        Set<Long> deletedCurrencies = new HashSet<>();
        eventResponseDto.getTicketTypes().forEach(ticketType -> {
            if(deletedCurrencies.contains(ticketType.getCurrency().getId()))
                return;

            currencyTemplate.delete(adminToken, ticketType.getCurrency().getId());
            deletedCurrencies.add(ticketType.getCurrency().getId());
        });
    }
}
