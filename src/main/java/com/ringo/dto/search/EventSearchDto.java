package com.ringo.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ringo.exception.UserException;
import com.ringo.model.company.Category;
import com.ringo.model.company.Event;
import jakarta.persistence.criteria.*;
import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
public class EventSearchDto extends GenericSearchDto<Event>{

    private String searchString;
    private Long[] categoryIds;
    private Long hostId;
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
        if(Objects.equals(sort, "distance")) {
            return Sort.unsorted();
        }
        return super.getSortSpec();
    }

    @Override
    @JsonIgnore
    public Specification<Event> getSpecification() {
        return (root, query, criteriaBuilder) -> {
            Specification<Event> specification = super.getSpecification();
            addOrderByDistance(root, query, criteriaBuilder);
            return specification.toPredicate(root, query, criteriaBuilder);
        };
    }

    @Override
    protected void addFilters(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, List<Predicate> filters) {
        if(currencyId == null && (priceMin != null || priceMax != null))
            throw new UserException("Currency is required when filtering by price");

        if (searchString != null) {
            searchString = searchString.toLowerCase();
            filters.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + searchString + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchString + "%")
            ));
        }
        if (hostId != null) {
            filters.add(criteriaBuilder.equal(root.get("host").get("id"), hostId));
        }
        if (currencyId != null) {
            filters.add(criteriaBuilder.equal(root.get("currency").get("id"), currencyId));
        }
        if (isTicketNeeded != null) {
            filters.add(criteriaBuilder.equal(root.get("isTicketNeeded"), isTicketNeeded));
        }
        if (priceMin != null) {
            filters.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), priceMin));
        }
        if (priceMax != null) {
            filters.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), priceMax));
        }
        if (categoryIds != null) {
            for(Long categoryId : categoryIds) {
                Subquery<Long> ids = query.subquery(Long.class);
                Root<Event> subqueryEvent = ids.from(Event.class);
                Join<Category, Event> categories = subqueryEvent.join("categories");

                ids.select(subqueryEvent.get("id")).where(criteriaBuilder.equal(categories.get("id"), categoryId));

                filters.add(criteriaBuilder.in(root.get("id")).value(ids));
            }
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
}
