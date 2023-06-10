package com.ringo.model.form;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationForm {
    private String title;
    private String description;
    private List<Question> questions;
}
