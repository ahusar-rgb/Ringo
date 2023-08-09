package com.ringo.it.itest.company;

import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.template.company.ParticipantTemplate;
import com.ringo.it.template.security.LoginTemplate;
import com.ringo.mock.dto.ParticipantDtoMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class ParticipantIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ParticipantTemplate participantTemplate;

    @Autowired
    private LoginTemplate loginTemplate;

    @Test
    void createSuccess() {
        ParticipantRequestDto requestDto = ParticipantDtoMock.getParticipantMockDto();
        ParticipantResponseDto responseDto = participantTemplate.create(requestDto);

        String token = loginTemplate.getToken(requestDto.getEmail(), requestDto.getPassword());
        ParticipantResponseDto actual = participantTemplate.findById(token, responseDto.getId());

        assertThat(actual).isEqualTo(responseDto);

        participantTemplate.delete(token);
    }

    @Test
    void updateSuccess() {
        ParticipantRequestDto requestDto = ParticipantDtoMock.getParticipantMockDto();
        ParticipantResponseDto responseDto = participantTemplate.create(requestDto);

        String token = loginTemplate.getToken(requestDto.getEmail(), requestDto.getPassword());

        ParticipantRequestDto updateDto = new ParticipantRequestDto();
        updateDto.setName("new name");
        updateDto.setGender("FEMALE");
        updateDto.setDateOfBirth("1990-01-01");

        ParticipantResponseDto updatedDto = participantTemplate.update(token, updateDto);
        ParticipantResponseDto actual = participantTemplate.findById(token, responseDto.getId());

        assertThat(actual.getName()).isEqualTo(updatedDto.getName());
        assertThat(actual.getGender()).isEqualTo(updatedDto.getGender());
        assertThat(actual.getDateOfBirth()).isEqualTo(updatedDto.getDateOfBirth());

        participantTemplate.delete(token);
    }

    @Test
    void setPhotoSuccess() {
        ParticipantRequestDto requestDto = ParticipantDtoMock.getParticipantMockDto();
        ParticipantResponseDto responseDto = participantTemplate.create(requestDto);

        String token = loginTemplate.getToken(requestDto.getEmail(), requestDto.getPassword());

        File profilePicture = new File("src/test/java/com/ringo/resources/test_profile_picture.jpeg");

        ParticipantResponseDto actual = participantTemplate.setPhoto(token, profilePicture, "image/jpeg");
        assertThat(actual.getProfilePictureId()).isNotNull();
        assertThat(actual).usingRecursiveComparison().ignoringFields("profilePictureId").isEqualTo(responseDto);

        participantTemplate.delete(token);
    }

    @Test
    void removePhotoSuccess() {
        ParticipantRequestDto requestDto = ParticipantDtoMock.getParticipantMockDto();
        ParticipantResponseDto responseDto = participantTemplate.create(requestDto);

        String token = loginTemplate.getToken(requestDto.getEmail(), requestDto.getPassword());

        File profilePicture = new File("src/test/java/com/ringo/resources/test_profile_picture.jpeg");
        participantTemplate.setPhoto(token, profilePicture, "image/jpeg");

        ParticipantResponseDto actual = participantTemplate.removePhoto(token);
        assertThat(actual.getProfilePictureId()).isNull();
        assertThat(actual).usingRecursiveComparison().ignoringFields("profilePictureId").isEqualTo(responseDto);

        participantTemplate.delete(token);
    }
}
