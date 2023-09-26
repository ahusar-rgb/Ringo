package com.ringo.dto.company.response;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class ReviewResponseDto {
    private Long id;
    private ParticipantResponseDto participant;
    private String comment;
    private Integer rate;
    private String createdAt;
}
