package com.ringo.model.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
@NoArgsConstructor
public class RegistrationForm {
    private String title;
    private String description;
    private List<Question> questions;
}
