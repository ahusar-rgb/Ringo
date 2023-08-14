package com.ringo.it.itest.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ReviewRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.template.company.OrganisationTemplate;
import com.ringo.it.template.company.ParticipantTemplate;
import com.ringo.it.template.company.ReviewTemplate;
import com.ringo.it.template.security.LoginTemplate;
import com.ringo.it.util.ItTestConsts;
import com.ringo.mock.dto.OrganisationDtoMock;
import com.ringo.mock.dto.ParticipantDtoMock;
import com.ringo.mock.dto.ReviewDtoMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class ReviewIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ParticipantTemplate participantTemplate;
    @Autowired
    private OrganisationTemplate organisationTemplate;
    @Autowired
    private ReviewTemplate reviewTemplate;
    @Autowired
    private LoginTemplate loginTemplate;


    @Test
    void createReviewSuccess() {
        TokenDto organisationToken = createOrganisation();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto participantToken = createParticipant();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(5);
        OrganisationResponseDto responseDto = reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo((float)reviewDto.getRate());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);

        OrganisationResponseDto found = organisationTemplate.findById(participantToken.getAccessToken(), organisation.getId());
        assertThat(found.getRating()).isEqualTo((float)reviewDto.getRate());

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void createMultipleReviews() {
        TokenDto organisationToken = createOrganisation();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        //review 1
        TokenDto participantToken = createParticipant();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(5);
        OrganisationResponseDto responseDto = reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo((float)reviewDto.getRate());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);

        //review 2
        TokenDto participantToken2 = createParticipant();

        ReviewRequestDto reviewDto2 = ReviewDtoMock.getReviewDtoMock();
        reviewDto2.setRate(1);
        OrganisationResponseDto responseDto2 = reviewTemplate.createReview(participantToken2.getAccessToken(), organisation.getId(), reviewDto2, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto2).isNotNull();
        assertThat(responseDto2.getRating()).isEqualTo((float)(reviewDto2.getRate() + reviewDto.getRate()) / 2);
        assertThat(responseDto2).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);

        participantTemplate.delete(participantToken.getAccessToken());
        participantTemplate.delete(participantToken2.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void createReviewAlreadyRated() {
        TokenDto organisationToken = createOrganisation();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        //review 1
        TokenDto participantToken = createParticipant();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        OrganisationResponseDto responseDto = reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo((float)reviewDto.getRate());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);

        //review 2
        ReviewRequestDto reviewDto2 = ReviewDtoMock.getReviewDtoMock();
        reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto2, ItTestConsts.HTTP_BAD_REQUEST);

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void createReviewUserIsNotParticipant() {
        TokenDto organisationToken = createOrganisation();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto organisationToken2 = createOrganisation();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewTemplate.createReview(organisationToken2.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_NOT_FOUND);

        organisationTemplate.delete(organisationToken.getAccessToken());
        organisationTemplate.delete(organisationToken2.getAccessToken());
    }

    @Test
    void createReviewOrganisationNotFound() {
        TokenDto participantToken = createParticipant();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewTemplate.createReview(participantToken.getAccessToken(), System.currentTimeMillis(), reviewDto, ItTestConsts.HTTP_NOT_FOUND);

        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void updateReviewSuccess() {
        TokenDto organisationToken = createOrganisation();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        //create review
        TokenDto participantToken = createParticipant();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(5);
        OrganisationResponseDto responseDto = reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo((float)reviewDto.getRate());
        assertThat(responseDto).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);

        //update review
        ReviewRequestDto reviewDto2 = ReviewDtoMock.getReviewDtoMock();
        reviewDto2.setRate(1);
        OrganisationResponseDto responseDto2 = reviewTemplate.updateReviews(participantToken.getAccessToken(), organisation.getId(), reviewDto2, ItTestConsts.HTTP_SUCCESS);

        assertThat(responseDto2).isNotNull();
        assertThat(responseDto2.getRating()).isEqualTo((float)reviewDto2.getRate());
        assertThat(responseDto2).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void updateReviewDoesntExist() {
        TokenDto organisationToken = createOrganisation();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        //update review
        TokenDto participantToken = createParticipant();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(1);
        reviewTemplate.updateReviews(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_NOT_FOUND);

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deleteReviewSuccess() {
        TokenDto organisationToken = createOrganisation();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto participantToken = createParticipant();

        ReviewRequestDto reviewDto = ReviewDtoMock.getReviewDtoMock();
        reviewDto.setRate(5);
        reviewTemplate.createReview(participantToken.getAccessToken(), organisation.getId(), reviewDto, ItTestConsts.HTTP_SUCCESS);

        OrganisationResponseDto responseDto = reviewTemplate.deleteReview(participantToken.getAccessToken(), organisation.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isNull();

        OrganisationResponseDto found = organisationTemplate.findById(participantToken.getAccessToken(), organisation.getId());
        assertThat(found.getRating()).isNull();

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void deleteReviewParticipantDoesntExist() {
        TokenDto organisationToken = createOrganisation();
        OrganisationResponseDto organisation = organisationTemplate.getCurrentOrganisation(organisationToken.getAccessToken());

        TokenDto participantToken = createParticipant();

        reviewTemplate.deleteReview(participantToken.getAccessToken(), organisation.getId(), ItTestConsts.HTTP_NOT_FOUND);

        participantTemplate.delete(participantToken.getAccessToken());
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    private TokenDto createOrganisation() {
        OrganisationRequestDto organisationRequestDto = OrganisationDtoMock.getOrganisationMockDto();
        organisationTemplate.create(organisationRequestDto);

        TokenDto organisationToken = loginTemplate.login(organisationRequestDto.getEmail(), organisationRequestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
        loginTemplate.verifyEmail(organisationRequestDto.getEmail(), organisationRequestDto.getPassword());
        organisationTemplate.activate(organisationToken.getAccessToken());

        return organisationToken;
    }

    private TokenDto createParticipant() {
        ParticipantRequestDto participantRequestDto = ParticipantDtoMock.getParticipantMockDto();
        participantTemplate.create(participantRequestDto);

        TokenDto participantToken = loginTemplate.login(participantRequestDto.getEmail(), participantRequestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
        loginTemplate.verifyEmail(participantRequestDto.getEmail(), participantRequestDto.getPassword());
        participantTemplate.activate(participantToken.getAccessToken());

        return participantToken;
    }
}
