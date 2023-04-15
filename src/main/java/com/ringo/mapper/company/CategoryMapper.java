package com.ringo.mapper.company;

import com.ringo.dto.company.CategoryDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.company.Category;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface CategoryMapper extends EntityMapper<CategoryDto, CategoryDto, Category> {
}
