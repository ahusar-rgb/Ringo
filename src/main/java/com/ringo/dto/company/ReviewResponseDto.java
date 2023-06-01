package com.ringo.dto.company;

import lombok.Data;

@Data
public class ReviewResponseDto {
    private Long id;
    private ParticipantResponseDto participant;
    private String comment;
    private Integer rate;
}
