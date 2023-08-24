package com.ringo.mock.dto;

import com.ringo.model.form.Answer;
import com.ringo.model.form.RegistrationSubmission;

import java.util.List;

public class RegistrationSubmissionMock {

    public static RegistrationSubmission getRegistrationSubmissionMock() {
        return RegistrationSubmission.builder()
                .answers(List.of(
                        Answer.builder()
                                .questionId(1L)
                                .content("Test")
                                .build(),
                        Answer.builder()
                                .content("Test")
                                .optionIds(List.of(3L))
                                .build(),
                        Answer.builder()
                                .content("Test")
                                .optionIds(List.of(1L, 2L))
                                .build()
                )).build();
    }
}
