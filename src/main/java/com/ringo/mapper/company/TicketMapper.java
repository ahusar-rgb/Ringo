package com.ringo.mapper.company;

import com.ringo.config.Constants;
import com.ringo.dto.company.TicketDto;
import com.ringo.mapper.common.SingleDtoEntityMapper;
import com.ringo.model.company.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", uses = {ParticipantMapper.class, EventMapper.class})
public interface TicketMapper extends SingleDtoEntityMapper<TicketDto, Ticket> {

    @Override
    @Mapping(target = "participant", source = "id.participant")
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "ticketCode", ignore = true)
    @Mapping(target = "timeOfSubmission", source = "timeOfSubmission", qualifiedByName = "DateToString")
    @Mapping(target = "expiryDate", source = "expiryDate", qualifiedByName = "DateToString")
    TicketDto toDto(Ticket entity);

    @Named("DateToString")
    default String toString(Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);
        return formatter.format(instant.atZone(ZoneOffset.UTC));
    }
}
