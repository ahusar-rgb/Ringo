package com.ringo.it.itest.company;

import com.ringo.dto.company.LabelDto;
import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.template.company.OrganisationTemplate;
import com.ringo.it.template.security.LoginTemplate;
import com.ringo.it.util.ItTestConsts;
import com.ringo.mock.dto.OrganisationDtoMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        String token = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS).getAccessToken();
        OrganisationResponseDto actual = organisationTemplate.getCurrentOrganisation(token);

        assertThat(actual).isEqualTo(responseDto);

        organisationTemplate.delete(token);
    }


    @Test
    void createWithContactsSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationMockDto();

        List<LabelDto> contacts = new ArrayList<>();
        contacts.add(LabelDto.builder().ordinal(2).title("title2").content("content2").build());
        contacts.add(LabelDto.builder().ordinal(1).title("title1").content("content1").build());
        contacts.add(LabelDto.builder().ordinal(3).title("title3").content("content3").build());
        requestDto.setContacts(contacts);

        OrganisationResponseDto responseDto = organisationTemplate.create(requestDto);

        String token = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS).getAccessToken();
        OrganisationResponseDto actual = organisationTemplate.getCurrentOrganisation(token);

        requestDto.getContacts().sort(Comparator.comparingInt(LabelDto::getOrdinal));
        assertThat(actual.getContacts()).usingRecursiveComparison().ignoringFields("id").isEqualTo(requestDto.getContacts());

        organisationTemplate.delete(token);
    }

    @Test
    void updateContactsSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationMockDto();

        List<LabelDto> contacts = new ArrayList<>();
        contacts.add(LabelDto.builder().ordinal(2).title("title2").content("content2").build());
        contacts.add(LabelDto.builder().ordinal(1).title("title1").content("content1").build());
        contacts.add(LabelDto.builder().ordinal(3).title("title3").content("content3").build());
        requestDto.setContacts(contacts);

        OrganisationResponseDto responseDto = organisationTemplate.create(requestDto);

        String token = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS).getAccessToken();

        OrganisationRequestDto updateDto = new OrganisationRequestDto();

        List<LabelDto> updatedContacts = new ArrayList<>();
        updatedContacts.add(LabelDto.builder().ordinal(3).title("new_title3").content("new_content3").build());
        updatedContacts.add(LabelDto.builder().ordinal(2).title("new_title2").content("new_content2").build());
        updatedContacts.add(LabelDto.builder().ordinal(1).title("new_title1").content("new_content1").build());
        updateDto.setContacts(updatedContacts);

        OrganisationResponseDto updatedDto = organisationTemplate.update(token, updateDto);
        OrganisationResponseDto actual = organisationTemplate.getCurrentOrganisation(token);

        updatedDto.getContacts().sort(Comparator.comparingInt(LabelDto::getOrdinal));
        assertThat(actual.getContacts()).isEqualTo(updatedDto.getContacts());
        assertThat(actual.getContacts()).usingRecursiveComparison().ignoringFields("id").isEqualTo(updatedDto.getContacts());

        organisationTemplate.delete(token);
    }

    @Test
    void updateSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationMockDto();
        OrganisationResponseDto responseDto = organisationTemplate.create(requestDto);

        String token = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS).getAccessToken();

        OrganisationRequestDto updateDto = new OrganisationRequestDto();
        updateDto.setName("new name");
        updateDto.setDescription("new description");

        OrganisationResponseDto updatedDto = organisationTemplate.update(token, updateDto);
        OrganisationResponseDto actual = organisationTemplate.getCurrentOrganisation(token);

        assertThat(actual.getName()).isEqualTo(updatedDto.getName());
        assertThat(actual.getContacts()).isEqualTo(updatedDto.getContacts());
        assertThat(actual.getDescription()).isEqualTo(updatedDto.getDescription());

        organisationTemplate.delete(token);
    }

    @Test
    void setPhotoSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationMockDto();
        OrganisationResponseDto responseDto = organisationTemplate.create(requestDto);

        String token = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS).getAccessToken();

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

        String token = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS).getAccessToken();

        File profilePicture = new File("src/test/java/com/ringo/resources/test_profile_picture.jpeg");
        organisationTemplate.setPhoto(token, profilePicture, "image/jpeg");

        OrganisationResponseDto actual = organisationTemplate.removePhoto(token);
        assertThat(actual.getProfilePictureId()).isNull();
        assertThat(actual).usingRecursiveComparison().ignoringFields("profilePictureId").isEqualTo(responseDto);

        organisationTemplate.delete(token);
    }
}
