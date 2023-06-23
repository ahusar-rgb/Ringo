package com.ringo.mapper.company;

import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.model.company.Participant;
import com.ringo.model.enums.Gender;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ParticipantMapper {

    public Participant toEntity(ParticipantRequestDto dto) {
        return Participant.builder()
                .name(dto.getName() == null ? null : dto.getName())
                .username(dto.getUsername() == null ? null : dto.getUsername())
                .email(dto.getEmail())
                .gender(dto.getGender() == null ? null : Gender.valueOf(dto.getGender()))
                .dateOfBirth(dto.getDateOfBirth() == null ? null: LocalDate.parse(dto.getDateOfBirth()))
                .build();
    }

    public ParticipantResponseDto toDto(Participant participant) {
        ParticipantResponseDto dto = ParticipantResponseDto.builder()
                .id(participant.getId())
                .name(participant.getName())
                .username(participant.getUsername())
                .dateOfBirth(participant.getDateOfBirth() == null ? null : participant.getDateOfBirth().toString())
                .gender(participant.getGender() == null ? null : participant.getGender().toString())
                .isActive(participant.getIsActive())
                .build();

        if(participant.getProfilePicture() != null)
            dto.setProfilePicture(participant.getProfilePicture().getId());
        return dto;
    }

    public void partialUpdate(Participant participant, ParticipantRequestDto dto) {
        if(dto.getName() != null) participant.setName(dto.getName());
        if(dto.getUsername() != null) participant.setUsername(dto.getUsername());
        if(dto.getDateOfBirth() != null) participant.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        if(dto.getGender() != null) participant.setGender(Gender.valueOf(dto.getGender()));
    }
}
