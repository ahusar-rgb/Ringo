package com.ringo.model.form;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class CheckboxQuestion extends Question {

    public CheckboxQuestion() {
        super(true, QuestionType.CHECKBOX);
    }
    private List<Option> options;
}
