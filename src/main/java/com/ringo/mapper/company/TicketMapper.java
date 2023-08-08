package com.ringo.mapper.company;

import com.ringo.dto.company.TicketDto;
import com.ringo.mapper.common.SingleDtoEntityMapper;
import com.ringo.model.company.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ParticipantMapper.class, EventMapper.class})
public interface TicketMapper extends SingleDtoEntityMapper<TicketDto, Ticket> {

    @Override
    @Mapping(target = "participant", source = "id.participant")
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "ticketCode", ignore = true)
    TicketDto toDto(Ticket entity);
}
