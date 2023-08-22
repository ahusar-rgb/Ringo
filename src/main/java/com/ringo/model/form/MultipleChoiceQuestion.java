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
public class MultipleChoiceQuestion extends Question {

    public MultipleChoiceQuestion() {
        super(false, QuestionType.MULTIPLE_CHOICE);
    }
    private List<Option> options;
}
