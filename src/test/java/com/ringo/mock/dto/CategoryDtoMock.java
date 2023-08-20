package com.ringo.mock.dto;

import com.ringo.dto.company.CategoryDto;
import com.ringo.it.util.IdGenerator;

public class CategoryDtoMock {

    public static CategoryDto getCategoryDtoMock() {
        return CategoryDto.builder()
                .id(IdGenerator.getNewId())
                .name("CategoryDtoMock" + IdGenerator.getNewId())
                .build();
    }
}
