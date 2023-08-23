package com.ringo.model.form;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ringo.model.form.utils.QuestionDeserializer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@JsonDeserialize(using = QuestionDeserializer.class)
public class Question {

    public Question(boolean isMultipleOptionsAllowed, QuestionType type) {
        this.isMultipleOptionsAllowed = isMultipleOptionsAllowed;
        this.type = type;
    }

    protected Long id;
    protected String content;
    protected boolean isRequired = false;
    protected final boolean isMultipleOptionsAllowed;
    protected final QuestionType type;
}
