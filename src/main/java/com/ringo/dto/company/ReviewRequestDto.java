package com.ringo.dto.company;

import lombok.Data;

@Data
public class ReviewRequestDto {
    private Long id;
    private String comment;
    private Integer rate;
}
