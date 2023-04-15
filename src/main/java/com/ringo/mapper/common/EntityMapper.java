package com.ringo.mapper.common;

import java.util.List;

public interface EntityMapper<RequestDto, ResponseDto,  T> {

    ResponseDto toDto(T entity);

    List<ResponseDto> toDtos(List<T> entities);

    T toEntity(RequestDto entityDto);

    List<T> toEntities(List<RequestDto> entityDtos);
}
