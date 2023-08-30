package com.ringo.mock.dto;

import com.ringo.dto.company.ReviewRequestDto;

public class ReviewDtoMock {
    public static ReviewRequestDto getReviewDtoMock() {
        return ReviewRequestDto.builder()
                .comment("test")
                .rate(5)
                .build();
    }
}
