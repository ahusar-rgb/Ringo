package com.ringo.mapper.common;

import org.mapstruct.Named;

import java.util.List;

public interface EntityMapper<RequestDto, ResponseDto,  T> {

    ResponseDto toDto(T entity);

    @Named("toDtoDetails")
    ResponseDto toDtoDetails(T entity);

    List<ResponseDto> toDtoList(List<T> entities);

    @Named("toDtoDetailsList")
    List<ResponseDto> toDtoDetailsList(List<T> entities);

    T toEntity(RequestDto dto);

    List<T> toEntityList(List<RequestDto> dtoList);
}
