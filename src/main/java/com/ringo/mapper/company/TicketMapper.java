package com.ringo.mapper.company;

import com.ringo.config.Constants;
import com.ringo.dto.company.response.TicketDto;
import com.ringo.mapper.common.SingleDtoEntityMapper;
import com.ringo.model.company.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ParticipantMapper.class, EventMapper.class, TicketTypeMapper.class})
public interface TicketMapper extends SingleDtoEntityMapper<TicketDto, Ticket> {

    @Override
    @Mapping(target = "participant", source = "id.participant")
    @Mapping(target = "event", source = "id.event", qualifiedByName = "toDtoSmall")
    @Mapping(target = "ticketCode", ignore = true)
    @Mapping(target = "timeOfSubmission", source = "timeOfSubmission", dateFormat = Constants.DATE_TIME_FORMAT)
    @Mapping(target = "expiryDate", source = "expiryDate", dateFormat = Constants.DATE_TIME_FORMAT)
    @Mapping(target = "registrationForm", source = "id.event.registrationForm")
    @Mapping(target = "registrationSubmission", source = "registrationSubmission")
    TicketDto toDto(Ticket entity);
}
