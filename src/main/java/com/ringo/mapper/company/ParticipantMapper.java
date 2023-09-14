package com.ringo.mapper.company;

import com.ringo.dto.company.request.ParticipantRequestDto;
import com.ringo.dto.company.response.ParticipantResponseDto;
import com.ringo.mapper.common.AbstractUserMapper;
import com.ringo.model.company.Participant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ParticipantMapper extends AbstractUserMapper<ParticipantRequestDto, Participant, ParticipantResponseDto> {
    @Override
    @Mapping(target = "gender", expression = "java(entity.getGender() == null ? null : entity.getGender().name())")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "profilePictureId", source = "profilePicture.id")
    ParticipantResponseDto toDto(Participant entity);

    @Override
    @Named("toDtoDetails")
    @Mapping(target = "gender", expression = "java(entity.getGender() == null ? null : entity.getGender().name())")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "profilePictureId", source = "profilePicture.id")
    ParticipantResponseDto toDtoDetails(Participant entity);
}
