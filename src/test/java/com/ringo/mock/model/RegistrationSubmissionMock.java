package com.ringo.mock.model;

import com.ringo.model.form.Answer;
import com.ringo.model.form.RegistrationSubmission;

import java.util.ArrayList;
import java.util.List;

public class RegistrationSubmissionMock {

    public static RegistrationSubmission getRegistrationSubmissionMock() {
        return RegistrationSubmission.builder()
                .answers(new ArrayList<>(List.of(
                        Answer.builder()
                                .questionId(0)
                                .content("Test")
                                .build(),
                        Answer.builder()
                                .questionId(1)
                                .optionIds(List.of(2L))
                                .build(),
                        Answer.builder()
                                .questionId(2)
                                .optionIds(List.of(0L, 1L))
                                .build())
                )).build();
    }
}
