package com.ringo.model.form;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Answer {
    private Long questionId;
    private String content;
    private List<Long> optionIds;
}
