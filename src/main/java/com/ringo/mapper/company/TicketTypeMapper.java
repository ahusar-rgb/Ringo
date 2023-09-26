package com.ringo.mapper.company;

import com.ringo.config.Constants;
import com.ringo.dto.company.request.TicketTypeRequestDto;
import com.ringo.dto.company.response.TicketTypeResponseDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.company.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CurrencyMapper.class})
public interface TicketTypeMapper extends EntityMapper<TicketTypeRequestDto, TicketTypeResponseDto, TicketType> {

    @Override
    @Mapping(target = "salesStopTime", source = "salesStopTime", dateFormat = Constants.DATE_TIME_FORMAT)
    TicketTypeResponseDto toDto(TicketType entity);

    @Override
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "salesStopTime", source = "salesStopTime", dateFormat = Constants.DATE_TIME_FORMAT)
    TicketType toEntity(TicketTypeRequestDto ticketTypeRequestDto);
}
