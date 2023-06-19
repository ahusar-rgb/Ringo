package com.ringo.model.form;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegistrationSubmission {
    private List<Answer> answers;
}
