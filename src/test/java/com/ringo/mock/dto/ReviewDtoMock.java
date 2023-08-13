package com.ringo.mock.dto;

import com.ringo.dto.company.ReviewRequestDto;

public class ReviewDtoMock {
    public static ReviewRequestDto getReviewDtoMock() {
        ReviewRequestDto dto = new ReviewRequestDto();
        dto.setRate(5);
        dto.setComment("comment");
        return dto;
    }
}
