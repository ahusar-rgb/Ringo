package com.ringo.it.itest.company;

import com.ringo.dto.company.request.ReviewRequestDto;
import com.ringo.dto.company.response.OrganisationResponseDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.template.company.ReviewTemplate;
import com.ringo.it.util.ItTestConsts;
import com.ringo.mock.dto.ReviewDtoMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class ReviewIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private ReviewTemplate reviewTemplate;


    @Test
    void createReviewSuccess() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(5);
        OrganisationResponseDto responseDto = reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo((float)reviewDto.getRate());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("rating", "email").isEqualTo(organisation);
        assertThat(responseDto.getEmail()).isNull();

        OrganisationResponseDto found = organisationTemplate.findById(participantToken.getAccessToken(), organisation.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getRating()).isEqualTo((float)reviewDto.getRate());

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void createMultipleReviews() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        //review 1
        TokenDto participantToken = createParticipantActivated();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(5);
        OrganisationResponseDto responseDto = reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo((float)reviewDto.getRate());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("rating", "email").isEqualTo(organisation);
        assertThat(responseDto.getEmail()).isNull();

        //review 2
        TokenDto participantToken2 = createParticipantActivated();

        ReviewRequestDto reviewDto2 = ReviewDtoMock.getReviewDtoMock();
        reviewDto2.setRate(1);
        OrganisationResponseDto responseDto2 = reviewTemplate.createReview(participantToken2.getAccessToken(), organisation.getId(), reviewDto2, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto2).isNotNull();
        assertThat(responseDto2.getRating()).isEqualTo((float)(reviewDto2.getRate() + reviewDto.getRate()) / 2);
        assertThat(responseDto2).usingRecursiveComparison().ignoringFields("rating", "email").isEqualTo(organisation);
        assertThat(responseDto.getEmail()).isNull();

        participantTemplate.delete(participantToken.getAccessToken());
        participantTemplate.delete(participantToken2.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void createReviewAlreadyRated() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        //review 1
        TokenDto participantToken = createParticipantActivated();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        OrganisationResponseDto responseDto = reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo((float)reviewDto.getRate());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("rating", "email").isEqualTo(organisation);
        assertThat(responseDto.getEmail()).isNull();

        //review 2
        ReviewRequestDto reviewDto2 = ReviewDtoMock.getReviewDtoMock();
        reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto2, ItTestConsts.HTTP_BAD_REQUEST);

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void createReviewUserIsNotParticipant() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto organisationToken2 = createOrganisationActivated();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewTemplate.createReview(organisationToken2.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_NOT_FOUND);

        organisationTemplate.delete(organisationToken.getAccessToken());
        organisationTemplate.delete(organisationToken2.getAccessToken());
    }

    @Test
    void createReviewOrganisationNotFound() {
        TokenDto participantToken = createParticipantActivated();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewTemplate.createReview(participantToken.getAccessToken(), System.currentTimeMillis(), reviewDto, ItTestConsts.HTTP_NOT_FOUND);

        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void updateReviewSuccess() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        //create review
        TokenDto participantToken = createParticipantActivated();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(5);
        OrganisationResponseDto responseDto = reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo((float)reviewDto.getRate());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("rating", "email").isEqualTo(organisation);
        assertThat(responseDto.getEmail()).isNull();

        //update review
        ReviewRequestDto reviewDto2 = ReviewDtoMock.getReviewDtoMock();
        reviewDto2.setRate(1);
        OrganisationResponseDto responseDto2 = reviewTemplate.updateReviews(participantToken.getAccessToken(), organisation.getId(), reviewDto2, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto2).isNotNull();
        assertThat(responseDto2.getRating()).isEqualTo((float)reviewDto2.getRate());
        assertThat(responseDto2).usingRecursiveComparison().ignoringFields("rating", "email").isEqualTo(organisation);
        assertThat(responseDto.getEmail()).isNull();

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void updateReviewDoesntExist() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        //update review
        TokenDto participantToken = createParticipantActivated();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(1);
        reviewTemplate.updateReviews(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_NOT_FOUND);

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deleteReviewSuccess() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(5);
        reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        OrganisationResponseDto responseDto = reviewTemplate.deleteReview(participantToken.getAccessToken(), organisation.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isNull();

        OrganisationResponseDto found = organisationTemplate.findById(participantToken.getAccessToken(), organisation.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getRating()).isNull();

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deleteReviewParticipantDoesntExist() {
        TokenDto organisationToken = createOrganisationActivated();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        reviewTemplate.deleteReview(participantToken.getAccessToken(), organisation.getId(), ItTestConsts.HTTP_NOT_FOUND);

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }
}
