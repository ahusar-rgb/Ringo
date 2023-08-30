package com.ringo.it.itest.company;

import com.ringo.dto.ReviewPageRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.dto.company.ReviewRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.template.company.ParticipantTemplate;
import com.ringo.it.template.company.ReviewTemplate;
import com.ringo.it.template.security.LoginTemplate;
import com.ringo.it.util.ItTestConsts;
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

    @Autowired
    private ReviewTemplate reviewTemplate;

    @Test
    void createSuccess() {
        ParticipantRequestDto participantRequestDto = ParticipantDtoMock.getParticipantMockDto();
        ParticipantResponseDto responseDto = participantTemplate.create(participantRequestDto);

        loginTemplate.verifyEmail(participantRequestDto.getEmail(), participantRequestDto.getUsername());
        TokenDto token = loginTemplate.login(participantRequestDto.getEmail(), participantRequestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);

        ParticipantResponseDto actual = participantTemplate.getCurrentParticipant(token.getAccessToken());

        assertThat(actual).isEqualTo(responseDto);

        participantTemplate.delete(token.getAccessToken());
    }

    @Test
    void updateSuccess() {
        TokenDto token = createParticipantActivated();

        ParticipantRequestDto updateDto = new ParticipantRequestDto();
        updateDto.setName("new name");
        updateDto.setGender("FEMALE");
        updateDto.setDateOfBirth("1990-01-01");

        ParticipantResponseDto updatedDto = participantTemplate.update(token.getAccessToken(), updateDto);
        ParticipantResponseDto actual = participantTemplate.getCurrentParticipant(token.getAccessToken());

        assertThat(actual.getName()).isEqualTo(updatedDto.getName());
        assertThat(actual.getGender()).isEqualTo(updatedDto.getGender());
        assertThat(actual.getDateOfBirth()).isEqualTo(updatedDto.getDateOfBirth());

        participantTemplate.delete(token.getAccessToken());
    }

    @Test
    void setPhotoSuccess() {
        TokenDto token = createParticipantActivated();
        File profilePicture = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");

        ParticipantResponseDto actual = participantTemplate.setPhoto(token.getAccessToken(), profilePicture, "image/jpeg");
        assertThat(actual.getProfilePictureId()).isNotNull();

        participantTemplate.delete(token.getAccessToken());
    }

    @Test
    void removePhotoSuccess() {
        TokenDto token = createParticipantActivated();
        File profilePicture = new File("src/test/java/com/ringo/resources/test_picture_1.jpeg");

        participantTemplate.setPhoto(token.getAccessToken(), profilePicture, "image/jpeg");

        ParticipantResponseDto actual = participantTemplate.removePhoto(token.getAccessToken());
        assertThat(actual.getProfilePictureId()).isNull();

        participantTemplate.delete(token.getAccessToken());
    }

    @Test
    void deleteParticipantWithReviews() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), ReviewRequestDto.builder()
                .rate(5)
                .comment("test review")
                .build(),
                ItTestConsts.HTTP_SUCCESS);

        OrganisationResponseDto actual = organisationTemplate.findById(null, organisation.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(actual.getRating()).isEqualTo(5.0f);

        participantTemplate.delete(participantToken.getAccessToken());

        actual = organisationTemplate.findById(null, organisation.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(actual.getRating()).isNull();

        ReviewPageRequestDto request = new ReviewPageRequestDto();
        request.setPage(0);
        request.setSize(10);
        assertThat(reviewTemplate.findAllByOrganisation(null, organisation.getId(), request).size()).isEqualTo(0);

        organisationTemplate.delete(organisationToken.getAccessToken());
    }
}
