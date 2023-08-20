package com.ringo.mock.model;

import com.ringo.it.util.IdGenerator;
import com.ringo.model.company.Category;

public class CategoryMock {

    public static Category getCategoryMock() {
        return Category.builder()
                .id(IdGenerator.getNewId())
                .name("Test")
                .build();
    }
}
