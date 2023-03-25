package com.ringo.mapper.common;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.model.common.AbstractEntity;

import java.util.List;

public interface EntityMapper<E extends AbstractEntityDto, T extends AbstractEntity> {

    E toDto(T entity);

    List<E> toDtos(List<T> entities);

    T toEntity(E entityDto);

    List<T> toEntities(List<E> entityDtos);
}
