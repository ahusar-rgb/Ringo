package com.ringo.mapper.company;

import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.mapper.common.AbstractUserMapper;
import com.ringo.model.company.Participant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParticipantMapper extends AbstractUserMapper<ParticipantRequestDto, Participant, ParticipantResponseDto> {
}
