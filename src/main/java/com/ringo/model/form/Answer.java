package com.ringo.model.form;

import lombok.Data;

import java.util.List;

@Data
public class Answer {
    private Long questionId;
    private String content;
    private List<Long> optionIds;
}
