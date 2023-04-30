package com.ringo.mapper.company;

import com.ringo.dto.company.CurrencyDto;
import com.ringo.mapper.common.SingleDtoEntityMapper;
import com.ringo.model.company.Currency;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyMapper extends SingleDtoEntityMapper<CurrencyDto, Currency> {
}
