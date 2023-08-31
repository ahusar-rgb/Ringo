package com.ringo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ringo.model.company.Review;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@Data
public class ReviewPageRequestDto {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private Integer page;
    private Integer size;

    @JsonIgnore
    public Pageable getPageable() {
        return PageRequest.of(
                (page != null) ? page : 0,
                size != null ? size : DEFAULT_PAGE_SIZE,
                Sort.unsorted()
        );
    }

    public Specification<Review> getSpecification(Long organisationId, Long userId) {
        return (root, query, criteriaBuilder) -> {

            if(userId != null) {
                query.orderBy(
                        criteriaBuilder.desc(
                                criteriaBuilder.selectCase()
                                        .when(criteriaBuilder.equal(root.get("participant").get("id"), userId), LocalDateTime.now())
                                        .otherwise(root.get("createdAt"))
                        )
                );
            }

            return criteriaBuilder.equal(root.get("organisation").get("id"), organisationId);
        };
    }
}
