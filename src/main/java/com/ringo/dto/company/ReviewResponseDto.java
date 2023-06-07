package com.ringo.dto.company;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponseDto {
    private Long id;
    private ParticipantResponseDto participant;
    private String comment;
    private Integer rate;
    private LocalDateTime createdAt;
}
