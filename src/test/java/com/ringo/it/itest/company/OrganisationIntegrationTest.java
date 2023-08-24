package com.ringo.it.itest.company;

import com.ringo.dto.company.LabelDto;
import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.dto.security.TokenDto;
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
        OrganisationRequestDto organisationRequestDto = OrganisationDtoMock.getOrganisationDtoMock();
        OrganisationResponseDto responseDto = organisationTemplate.create(organisationRequestDto);

        loginTemplate.verifyEmail(organisationRequestDto.getEmail(), organisationRequestDto.getUsername());
        TokenDto tokenDto = loginTemplate.login(organisationRequestDto.getEmail(), organisationRequestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
        OrganisationResponseDto actual = organisationTemplate.getCurrentOrganisation(tokenDto.getAccessToken());

        assertThat(actual).isEqualTo(responseDto);

        organisationTemplate.delete(tokenDto.getAccessToken());
    }


    @Test
    void createWithContactsSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationDtoMock();

        List<LabelDto> contacts = new ArrayList<>();
        contacts.add(LabelDto.builder().ordinal(2).title("title2").content("content2").build());
        contacts.add(LabelDto.builder().ordinal(1).title("title1").content("content1").build());
        contacts.add(LabelDto.builder().ordinal(3).title("title3").content("content3").build());
        requestDto.setContacts(contacts);

        TokenDto token = createOrganisationActivated(requestDto);
        OrganisationResponseDto actual = organisationTemplate.getCurrentOrganisation(token.getAccessToken());

        requestDto.getContacts().sort(Comparator.comparingInt(LabelDto::getOrdinal));
        assertThat(actual.getContacts()).usingRecursiveComparison().ignoringFields("id").isEqualTo(requestDto.getContacts());

        organisationTemplate.delete(token.getAccessToken());
    }

    @Test
    void updateContactsSuccess() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationDtoMock();

        List<LabelDto> contacts = new ArrayList<>();
        contacts.add(LabelDto.builder().ordinal(2).title("title2").content("content2").build());
        contacts.add(LabelDto.builder().ordinal(1).title("title1").content("content1").build());
        contacts.add(LabelDto.builder().ordinal(3).title("title3").content("content3").build());
        requestDto.setContacts(contacts);

        TokenDto token = createOrganisationActivated(requestDto);

        OrganisationRequestDto updateDto = new OrganisationRequestDto();

        List<LabelDto> updatedContacts = new ArrayList<>();
        updatedContacts.add(LabelDto.builder().ordinal(3).title("new_title3").content("new_content3").build());
        updatedContacts.add(LabelDto.builder().ordinal(2).title("new_title2").content("new_content2").build());
        updatedContacts.add(LabelDto.builder().ordinal(1).title("new_title1").content("new_content1").build());
        updateDto.setContacts(updatedContacts);

        OrganisationResponseDto updatedDto = organisationTemplate.update(token.getAccessToken(), updateDto);
        OrganisationResponseDto actual = organisationTemplate.getCurrentOrganisation(token.getAccessToken());

        updatedDto.getContacts().sort(Comparator.comparingInt(LabelDto::getOrdinal));
        assertThat(actual.getContacts()).isEqualTo(updatedDto.getContacts());
        assertThat(actual.getContacts()).usingRecursiveComparison().ignoringFields("id").isEqualTo(updatedDto.getContacts());

        organisationTemplate.delete(token.getAccessToken());
    }

    @Test
    void updateSuccess() {
        TokenDto token = createOrganisationActivated();

        OrganisationRequestDto updateDto = new OrganisationRequestDto();
        updateDto.setName("new name");
        updateDto.setDescription("new description");

        OrganisationResponseDto updatedDto = organisationTemplate.update(token.getAccessToken(), updateDto);
        OrganisationResponseDto actual = organisationTemplate.getCurrentOrganisation(token.getAccessToken());

        assertThat(actual.getName()).isEqualTo(updatedDto.getName());
        assertThat(actual.getContacts()).isEqualTo(updatedDto.getContacts());
        assertThat(actual.getDescription()).isEqualTo(updatedDto.getDescription());

        organisationTemplate.delete(token.getAccessToken());
    }

    @Test
    void setPhotoSuccess() {
        TokenDto token = createOrganisationActivated();

        File profilePicture = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");

        OrganisationResponseDto actual = organisationTemplate.setPhoto(token.getAccessToken(), profilePicture, "image/jpeg");
        assertThat(actual.getProfilePictureId()).isNotNull();

        organisationTemplate.delete(token.getAccessToken());
    }

    @Test
    void removePhotoSuccess() {
        TokenDto token = createOrganisationActivated();

        File profilePicture = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");
        organisationTemplate.setPhoto(token.getAccessToken(), profilePicture, "image/jpeg");

        OrganisationResponseDto actual = organisationTemplate.removePhoto(token.getAccessToken());
        assertThat(actual.getProfilePictureId()).isNull();

        organisationTemplate.delete(token.getAccessToken());
    }
}
