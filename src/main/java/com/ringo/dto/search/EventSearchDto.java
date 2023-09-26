package com.ringo.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ringo.exception.UserException;
import com.ringo.model.company.*;
import jakarta.persistence.criteria.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class EventSearchDto extends GenericSearchDto<Event>{

    private static final long DEFAULT_CURRENCY_ID = 2L; //EUR
    private String search;
    private Long[] categoryIds;
    private Long hostId;
    private Boolean isActive;
    private Boolean isTicketNeeded;
    private Float priceMin;
    private Float priceMax;
    private Long currencyId;
    private Double latitude;
    private Double longitude;
    private Integer maxDistance;
    private String startTimeMin;
    private String startTimeMax;
    private String endTimeMin;
    private String endTimeMax;

    @Override
    @JsonIgnore
    public Sort getSortSpec() {
        if(Objects.equals(sort, "distance") ||
                Objects.equals(sort, "string") ||
                Objects.equals(sort, "price"))
            return Sort.unsorted();
        return super.getSortSpec();
    }

    @Override
    @JsonIgnore
    public Specification<Event> getSpecification() {
        return (root, query, criteriaBuilder) -> {
            Specification<Event> specification = super.getSpecification();
            addOrderByDistance(root, query, criteriaBuilder);
            addOrderByName(root, query, criteriaBuilder);
            addOrderByPrice(root, query, criteriaBuilder);
            return specification.toPredicate(root, query, criteriaBuilder);
        };
    }

    @Override
    protected void addFilters(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, List<Predicate> filters) {
        if(currencyId == null && (priceMin != null || priceMax != null))
            throw new UserException("Currency is required when filtering by price");

        if (search != null) {
            filters.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + search.toLowerCase() + "%")
            ));
        }
        if (hostId != null) {
            filters.add(criteriaBuilder.equal(root.get("host").get("id"), hostId));
        }
        if (isTicketNeeded != null) {
            filters.add(criteriaBuilder.equal(root.get("isTicketNeeded"), isTicketNeeded));
        }
        if(isActive != null) {
            filters.add(criteriaBuilder.equal(root.get("isActive"), isActive));
        }

        if(currencyId != null) {

            Expression<Float> minPrice = getMinPriceExpression(root, query, criteriaBuilder);
            Expression<Float> maxPrice = getMaxPriceExpression(root, query, criteriaBuilder);

            if (priceMin != null) {
                filters.add(criteriaBuilder.greaterThanOrEqualTo(maxPrice, priceMin));
            }
            if (priceMax != null) {
                filters.add(criteriaBuilder.lessThanOrEqualTo(minPrice, priceMax));
            }
        }

        if (categoryIds != null) {
            List<Predicate> predicates = new ArrayList<>();
            for(Long categoryId : categoryIds) {
                Subquery<Long> ids = query.subquery(Long.class);
                Root<Event> subqueryEvent = ids.from(Event.class);
                Join<Category, Event> categories = subqueryEvent.join("categories");

                ids.select(subqueryEvent.get("id")).where(criteriaBuilder.equal(categories.get("id"), categoryId));

                predicates.add(criteriaBuilder.in(root.get("id")).value(ids));
            }
            filters.add(criteriaBuilder.or(predicates.toArray(new Predicate[0])));
        }
        if(maxDistance != null) {
            if(latitude == null || longitude == null)
                throw new UserException("Latitude and longitude are required when filtering by distance");
            query.where(criteriaBuilder.isNotNull(root.get("latitude")), criteriaBuilder.isNotNull(root.get("longitude")));

            filters.add(criteriaBuilder.lessThanOrEqualTo(
                    criteriaBuilder.function(
                            "get_distance",
                            Double.class,
                            criteriaBuilder.literal(latitude),
                            criteriaBuilder.literal(longitude),
                            root.get("latitude"),
                            root.get("longitude")),
                    Double.valueOf(maxDistance)
                )
            );
        }
        if(startTimeMin != null) {
            filters.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), LocalDateTime.parse(startTimeMin)));
        }
        if(startTimeMax != null) {
            filters.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), LocalDateTime.parse(startTimeMax)));
        }
        if(endTimeMin != null) {
            filters.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), LocalDateTime.parse(endTimeMin)));
        }
        if(endTimeMax != null) {
            filters.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), LocalDateTime.parse(endTimeMax)));
        }
    }

    private void addOrderByDistance(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if(Objects.equals(sort, "distance")) {

            if(latitude == null || longitude == null)
                throw new UserException("Latitude and longitude are required when filtering by distance");

            Expression<Double> byDistance = criteriaBuilder.function(
                    "get_distance",
                    Double.class,
                    criteriaBuilder.literal(latitude),
                    criteriaBuilder.literal(longitude),
                    root.get("latitude"),
                    root.get("longitude")
            );
            query.where(criteriaBuilder.isNotNull(root.get("latitude")), criteriaBuilder.isNotNull(root.get("longitude")));
            if(dir == Sort.Direction.ASC)
                query.orderBy(criteriaBuilder.asc(byDistance));
            else
                query.orderBy(criteriaBuilder.desc(byDistance));
        }
    }

    private void addOrderByName(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if(Objects.equals(sort, "string")) {
            if(search == null)
                return;
            String searchStringLower = search.toLowerCase();
            query.orderBy(criteriaBuilder.asc(criteriaBuilder.selectCase()
                    .when(criteriaBuilder.equal(root.get("name"), search), 1)
                    .when(criteriaBuilder.equal(criteriaBuilder.lower(root.get("name")), searchStringLower), 2)
                    .when(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + searchStringLower + "%"), 3)
                    .when(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "% " + searchStringLower + " %"), 4)
                    .when(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchStringLower + "%"), 5)
                    .otherwise(6)
            ));
        }
    }

    private void addOrderByPrice(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (Objects.equals(sort, "price")) {

            Expression<Float> price = criteriaBuilder.selectCase()
                            .when( getMinPriceExpression(root, query, criteriaBuilder).isNull(), root.get("price"))
                            .otherwise(getMinPriceExpression(root, query, criteriaBuilder))
                            .as(Float.class);

            if (dir == Sort.Direction.ASC)
                query.orderBy(criteriaBuilder.asc(price));
            else
                query.orderBy(criteriaBuilder.desc(price));
        }
    }

    private Expression<Float> getMinPriceExpression(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Subquery<Float> ticketMinPriceSubquery = getMinPriceSubquery(root, query, criteriaBuilder);
        Subquery<Currency> ticketCurrencySubquery = getCurrencySubquery(root, query, criteriaBuilder, ticketMinPriceSubquery);

        return getPriceExpression(root, ticketCurrencySubquery, ticketMinPriceSubquery, query, criteriaBuilder);
    }

    private Expression<Float> getMaxPriceExpression(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Subquery<Float> ticketMaxPriceSubquery = getMaxPriceSubquery(root, query, criteriaBuilder);
        Subquery<Currency> ticketCurrencySubquery = getCurrencySubquery(root, query, criteriaBuilder, ticketMaxPriceSubquery);

        return getPriceExpression(root, ticketCurrencySubquery, ticketMaxPriceSubquery, query, criteriaBuilder);
    }

    private Expression<Float> getPriceExpression(Root<Event> root, Expression<Currency> ticketCurrencyExpression, Subquery<Float> ticketTypeSubquery, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Subquery<Float> exchangeRateSubquery = getExchangeRateSubquery(query, criteriaBuilder, ticketCurrencyExpression);

        Expression<Float> ticketPriceExpression = ticketTypeSubquery.getSelection();

        return criteriaBuilder.selectCase()
                .when(criteriaBuilder.isNull(ticketPriceExpression), root.get("price"))
                .when(criteriaBuilder.isNotNull(exchangeRateSubquery.getSelection()),
                        criteriaBuilder.prod(exchangeRateSubquery.getSelection(), ticketPriceExpression))
                .otherwise(ticketPriceExpression).as(Float.class);
    }

    private Subquery<Float> getMinPriceSubquery(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Subquery<Float> ticketMinPriceSubquery = query.subquery(Float.class);
        Root<TicketType> ticketMinPriceRoot = ticketMinPriceSubquery.from(TicketType.class);
        ticketMinPriceSubquery.select(criteriaBuilder.min(ticketMinPriceRoot.get("price")))
                .distinct(true)
                .where(criteriaBuilder.equal(ticketMinPriceRoot.get("event"), root));

        return ticketMinPriceSubquery;
    }

    private Subquery<Float> getMaxPriceSubquery(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Subquery<Float> ticketMaxPriceSubquery = query.subquery(Float.class);
        Root<TicketType> ticketMinPriceRoot = ticketMaxPriceSubquery.from(TicketType.class);
        ticketMaxPriceSubquery.select(criteriaBuilder.max(ticketMinPriceRoot.get("price")))
                .distinct(true)
                .where(criteriaBuilder.equal(ticketMinPriceRoot.get("event"), root));

        return ticketMaxPriceSubquery;
    }

    private Subquery<Currency> getCurrencySubquery(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Subquery<Float> ticketMinPriceSubquery) {
        Subquery<Currency> ticketCurrencySubquery = query.subquery(Currency.class);
        Root<TicketType> ticketCurrencyRoot = ticketCurrencySubquery.from(TicketType.class);
        ticketCurrencySubquery.select(ticketCurrencyRoot.get("currency"))
                .distinct(true)
                .where(criteriaBuilder.equal(ticketCurrencyRoot.get("price"), ticketMinPriceSubquery.getSelection()),
                        criteriaBuilder.equal(ticketCurrencyRoot.get("event"), root));

        return ticketCurrencySubquery;
    }

    private Subquery<Float> getExchangeRateSubquery(CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Expression<Currency> originalCurrencyExpression) {
        Subquery<Float> exchangeRateSubquery = query.subquery(Float.class);
        Root<ExchangeRate> exchangeRateRoot = exchangeRateSubquery.from(ExchangeRate.class);

        exchangeRateSubquery.select(exchangeRateRoot.get("rate"))
                .where(
                        criteriaBuilder.equal(exchangeRateRoot.get("id").get("from"), originalCurrencyExpression),
                        criteriaBuilder.equal(exchangeRateRoot.get("id").get("to").get("id"), currencyId == null ? DEFAULT_CURRENCY_ID : currencyId)
                );

        return exchangeRateSubquery;
    }
}
