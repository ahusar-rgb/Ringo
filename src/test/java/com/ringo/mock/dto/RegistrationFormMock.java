package com.ringo.mock.dto;

import com.ringo.model.form.*;

import java.util.List;

public class RegistrationFormMock {

    public static RegistrationForm getRegistrationFormMock() {
        return RegistrationForm.builder()
                .title("Test")
                .description("Test")
                .questions(List.of(
                        new InputFieldQuestion().toBuilder()
                                .content("Test")
                                .isRequired(true)
                                .build(),
                        new MultipleChoiceQuestion().toBuilder()
                                .content("Test")
                                .isRequired(true)
                                .options(List.of(
                                        Option.builder()
                                                .content("Test")
                                                .build(),
                                        Option.builder()
                                                .content("Test2")
                                                .build(),
                                        Option.builder()
                                                .content("Test3")
                                                .build()
                                ))
                                .build(),
                        new CheckboxQuestion().toBuilder()
                                .content("Test")
                                .isRequired(false)
                                .options(List.of(
                                        Option.builder()
                                                .content("Test")
                                                .build(),
                                        Option.builder()
                                                .content("Test2")
                                                .build(),
                                        Option.builder()
                                                .content("Test3")
                                                .build()
                                ))
                                .build()
                    )
                )
                .build();
    }
}
