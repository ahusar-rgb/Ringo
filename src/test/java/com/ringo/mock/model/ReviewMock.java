package com.ringo.mock.model;

import com.ringo.model.company.Review;

public class ReviewMock {
    public static Review getReviewMock() {
        Review review = new Review();
        review.setId(1L);
        review.setRate(5);
        review.setComment("test");
        return review;
    }
}
