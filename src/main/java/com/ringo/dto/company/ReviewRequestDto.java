package com.ringo.dto.company;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRequestDto {
    private Long id;
    private String comment;
    private Integer rate;
}
