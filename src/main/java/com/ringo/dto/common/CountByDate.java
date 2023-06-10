package com.ringo.dto.common;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CountByDate {
    private LocalDate date;
    private Integer count;
}
