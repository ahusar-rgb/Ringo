package com.ringo.dto.company;

import lombok.Data;

@Data
public class ReviewRequestDto {
    private Long id;
    private Long organisationId;
    private String comment;
    private Integer rate;
}
