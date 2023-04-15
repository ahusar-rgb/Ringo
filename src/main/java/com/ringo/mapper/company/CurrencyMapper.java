package com.ringo.mapper.company;

import com.ringo.dto.company.CurrencyDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.company.Currency;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyMapper extends EntityMapper<CurrencyDto, CurrencyDto, Currency> {
}
