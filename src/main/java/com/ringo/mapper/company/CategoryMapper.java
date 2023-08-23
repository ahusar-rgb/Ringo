package com.ringo.mapper.company;

import com.ringo.dto.company.CategoryDto;
import com.ringo.mapper.common.SingleDtoEntityMapper;
import com.ringo.model.company.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface CategoryMapper extends SingleDtoEntityMapper<CategoryDto, Category> {

    @Mapping(target = "id", ignore = true)
    void partialUpdate(@MappingTarget Category entity, CategoryDto dto);
}
