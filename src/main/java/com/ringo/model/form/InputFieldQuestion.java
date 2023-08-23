package com.ringo.model.form;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class InputFieldQuestion extends Question {

    public InputFieldQuestion() {
        super(false, QuestionType.INPUT_FIELD);
    }
    private Integer maxCharacters = 64;
}
