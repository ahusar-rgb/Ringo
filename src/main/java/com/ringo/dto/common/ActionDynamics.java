package com.ringo.dto.common;

import lombok.Data;

import java.util.List;

@Data
public class ActionDynamics {
    private String action;
    private List<CountByDate> data;
}
