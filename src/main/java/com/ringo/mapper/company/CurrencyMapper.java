package com.ringo.mapper.company;

import com.ringo.dto.company.CurrencyDto;
import com.ringo.mapper.common.SingleDtoEntityMapper;
import com.ringo.model.company.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CurrencyMapper extends SingleDtoEntityMapper<CurrencyDto, Currency> {

    @Mapping(target = "id", ignore = true)
    void partialUpdate(@MappingTarget Currency entity, CurrencyDto dto);
}
