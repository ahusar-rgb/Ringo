package com.ringo.service.company.event;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.*;
import com.ringo.dto.search.EventSearchDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.EventGroupMapper;
import com.ringo.mapper.company.EventMapper;
import com.ringo.mapper.company.EventPersonalizedMapper;
import com.ringo.model.company.Currency;
import com.ringo.model.company.Event;
import com.ringo.model.company.Organisation;
import com.ringo.repository.CurrencyRepository;
import com.ringo.repository.EventRepository;
import com.ringo.service.common.CurrencyExchanger;
import com.ringo.service.company.OrganisationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ringo.utils.Geography.getDistance;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventSearchService {

    private final EventRepository repository;
    private final OrganisationService organisationService;
    private final EventMapper mapper;
    private final EventPersonalizedMapper personalizedMapper;
    private final CurrencyExchanger currencyExchanger;
    private final CurrencyRepository currencyRepository;
    private final ApplicationProperties config;
    private final EventGroupMapper groupMapper;

    public EventResponseDto findById(Long id) {
        log.info("findEventById: {}", id);

        Event event = repository.findFullById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );


        Organisation organisation;
        try {
            organisation = organisationService.getFullUser();
        } catch (UserException e) {
            organisation = null;
        }

        if (!event.getIsActive()) {
            if (organisation == null || !event.getHost().getId().equals(organisation.getId()))
                throw new NotFoundException("Event [id: %d] not found".formatted(id));
        }

        return personalizedMapper.toPersonalizedDto(event);
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

        return groupMapper.toDtoList(groupEvents(groups,
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

        Organisation organisation;
        try {
            organisation = organisationService.getFullUser();
        } catch (UserException e) {
            organisation = null;
        }

        Specification<Event> specification = searchDto.getSpecification();

        if(organisation != null) {
            Organisation finalOrganisation = organisation;
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.equal(root.get("host").get("id"), finalOrganisation.getId()),
                    builder.isTrue(root.get("isActive")))
            );
        } else {
            specification = specification.and((root, query, builder) -> builder.isTrue(root.get("isActive")));
        }

        List<Event> events = repository.findAll(specification, searchDto.getPageable()).getContent();

        return events.stream()
                .map(event -> toSmallDtoWithDependencies(event, searchDto))
                .collect(Collectors.toList());
    }

    private EventSmallDto toSmallDtoWithDependencies(Event event, EventSearchDto searchDto) {
        EventSmallDto dto = mapper.toDtoSmall(event);
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
                                    () -> new NotFoundException("Currency [id: %d] not found".formatted(searchDto.getCurrencyId()))),
                            event.getPrice()
                    )
            );
            Currency currency = currencyRepository.findById(searchDto.getCurrencyId()).orElseThrow(
                    () -> new NotFoundException("Currency [id: %d] not found".formatted(searchDto.getCurrencyId())));
            dto.setCurrency(CurrencyDto.builder().name(currency.getName()).id(currency.getId()).symbol(currency.getSymbol()).build());
        }

        return dto;
    }
}
