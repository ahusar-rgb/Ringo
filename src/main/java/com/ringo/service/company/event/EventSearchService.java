package com.ringo.service.company.event;

import com.ringo.config.ApplicationProperties;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.EventGroup;
import com.ringo.dto.company.EventGroupDto;
import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.dto.company.response.EventSmallDto;
import com.ringo.dto.search.EventSearchDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.CurrencyMapper;
import com.ringo.mapper.company.EventGroupMapper;
import com.ringo.mapper.company.EventMapper;
import com.ringo.mapper.company.EventPersonalizedMapper;
import com.ringo.model.company.Currency;
import com.ringo.model.company.Event;
import com.ringo.model.company.Organisation;
import com.ringo.model.company.TicketType;
import com.ringo.repository.company.CurrencyRepository;
import com.ringo.repository.company.EventRepository;
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
    private final CurrencyMapper currencyMapper;
    private final ApplicationProperties config;
    private final EventGroupMapper groupMapper;


    public EventResponseDto findById(Long id) {
        log.info("findEventById: {}", id);

        Event event = repository.findFullById(id).orElseThrow(
                () -> new NotFoundException("Event [id: %d] not found".formatted(id))
        );


        Organisation organisation;
        try {
            organisation = organisationService.getFullActiveUser();
        } catch (NotFoundException | UserException e) {
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
            organisation = organisationService.getFullActiveUser();
        } catch (NotFoundException | UserException e) {
            organisation = null;
        }

        Specification<Event> specification = buildSpecification(searchDto, organisation);

        List<Event> events = repository.findAll(specification, searchDto.getPageable()).getContent();

        return events.stream()
                .map(event -> toSmallDtoWithDependencies(event, searchDto))
                .collect(Collectors.toList());
    }


    private Specification<Event> buildSpecification(EventSearchDto searchDto, Organisation organisation) {
        Specification<Event> specification = searchDto.getSpecification();

        if(organisation != null) {
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.equal(root.get("host").get("id"), organisation.getId()),
                    builder.isTrue(root.get("isActive")))
            );
        } else {
            specification = specification.and((root, query, builder) -> builder.isTrue(root.get("isActive")));
        }
        return specification;
    }


    private EventSmallDto toSmallDtoWithDependencies(Event event, EventSearchDto searchDto) {
        EventSmallDto dto = mapper.toDtoSmall(event);
        if(searchDto.getLatitude() != null
                && searchDto.getLongitude() != null
                && dto.getCoordinates() != null
                && dto.getCoordinates().longitude() != null
                && dto.getCoordinates().latitude() != null)
            dto.setDistance(
                    getDistance(new Coordinates(searchDto.getLatitude(), searchDto.getLongitude()),
                            dto.getCoordinates())
            );
        if(searchDto.getCurrencyId() != null && event.getTicketTypes() != null) {
            TicketType ticketType = event.getTicketTypes().stream()
                    .min((o1, o2) -> (int) (o1.getPrice() - o2.getPrice()))
                    .orElseThrow(() -> new NotFoundException("Ticket type not found"));
            dto.setPrice(
                    currencyExchanger.exchange(
                            ticketType.getCurrency(),
                            currencyRepository.findById(searchDto.getCurrencyId()).orElseThrow(
                                    () -> new NotFoundException("Currency [id: %d] not found".formatted(searchDto.getCurrencyId()))),
                            ticketType.getPrice()
                    )
            );
            Currency currency = currencyRepository.findById(searchDto.getCurrencyId()).orElseThrow(
                    () -> new NotFoundException("Currency [id: %d] not found".formatted(searchDto.getCurrencyId())));
            dto.setCurrency(currencyMapper.toDto(currency));
        }

        return dto;
    }
}
