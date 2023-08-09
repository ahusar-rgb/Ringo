package com.ringo.it.itest.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.template.company.OrganisationTemplate;
import com.ringo.it.template.security.LoginTemplate;
import com.ringo.mock.dto.OrganisationDtoMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class OrganisationIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private OrganisationTemplate organisationTemplate;

    @Autowired
    private LoginTemplate loginTemplate;

    @Test
    void createSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationMockDto();
        OrganisationResponseDto responseDto = organisationTemplate.create(requestDto);

        String token = loginTemplate.getToken(requestDto.getEmail(), requestDto.getPassword());
        OrganisationResponseDto actual = organisationTemplate.findById(token, responseDto.getId());

        assertThat(actual).isEqualTo(responseDto);

        organisationTemplate.delete(token);
    }

    @Test
    void updateSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationMockDto();
        OrganisationResponseDto responseDto = organisationTemplate.create(requestDto);

        String token = loginTemplate.getToken(requestDto.getEmail(), requestDto.getPassword());

        OrganisationRequestDto updateDto = new OrganisationRequestDto();
        updateDto.setName("new name");
        updateDto.setDescription("new description");
        updateDto.setContacts("new contacts");

        OrganisationResponseDto updatedDto = organisationTemplate.update(token, updateDto);
        OrganisationResponseDto actual = organisationTemplate.findById(token, responseDto.getId());

        assertThat(actual.getName()).isEqualTo(updatedDto.getName());
        assertThat(actual.getContacts()).isEqualTo(updatedDto.getContacts());
        assertThat(actual.getDescription()).isEqualTo(updatedDto.getDescription());

        organisationTemplate.delete(token);
    }

    @Test
    void setPhotoSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationMockDto();
        OrganisationResponseDto responseDto = organisationTemplate.create(requestDto);

        String token = loginTemplate.getToken(requestDto.getEmail(), requestDto.getPassword());

        File profilePicture = new File("src/test/java/com/ringo/resources/test_profile_picture.jpeg");

        OrganisationResponseDto actual = organisationTemplate.setPhoto(token, profilePicture, "image/jpeg");
        assertThat(actual.getProfilePictureId()).isNotNull();
        assertThat(actual).usingRecursiveComparison().ignoringFields("profilePictureId").isEqualTo(responseDto);

        organisationTemplate.delete(token);
    }

    @Test
    void removePhotoSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationMockDto();
        OrganisationResponseDto responseDto = organisationTemplate.create(requestDto);

        String token = loginTemplate.getToken(requestDto.getEmail(), requestDto.getPassword());

        File profilePicture = new File("src/test/java/com/ringo/resources/test_profile_picture.jpeg");
        organisationTemplate.setPhoto(token, profilePicture, "image/jpeg");

        OrganisationResponseDto actual = organisationTemplate.removePhoto(token);
        assertThat(actual.getProfilePictureId()).isNull();
        assertThat(actual).usingRecursiveComparison().ignoringFields("profilePictureId").isEqualTo(responseDto);

        organisationTemplate.delete(token);
    }
}
