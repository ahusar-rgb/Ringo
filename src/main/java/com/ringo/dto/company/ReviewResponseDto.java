package com.ringo.dto.company;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
public class ReviewResponseDto {
    private Long id;
    private ParticipantResponseDto participant;
    private String comment;
    private Integer rate;
    private LocalDateTime createdAt;
}
