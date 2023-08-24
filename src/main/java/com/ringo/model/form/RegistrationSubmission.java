package com.ringo.model.form;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RegistrationSubmission {
    private List<Answer> answers;
}
