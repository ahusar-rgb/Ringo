package com.ringo.it.itest.company;

import com.ringo.dto.company.*;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractEventIntegrationTest;
import com.ringo.it.util.ItTestConsts;
import com.ringo.mock.model.RegistrationFormMock;
import com.ringo.mock.model.RegistrationSubmissionMock;
import com.ringo.model.form.RegistrationSubmission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class EventInteractionIntegrationTest extends AbstractEventIntegrationTest {

    @Test
    void saveEventSuccess() {
        TokenDto organisation = createOrganisationActivated();
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisation.getAccessToken());

        TokenDto participant = createParticipantActivated();

        EventResponseDto dto = eventTemplate.saveEvent(participant.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(dto.getIsSaved()).isTrue();
        assertThat(dto.getPeopleSaved()).isEqualTo(1);

        EventResponseDto found = eventTemplate.findById(participant.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getIsSaved()).isTrue();
        assertThat(found.getPeopleSaved()).isEqualTo(1);

        cleanUpEvent(loginTemplate.getAdminToken(), organisation.getAccessToken(), event);
        organisationTemplate.delete(organisation.getAccessToken());
        participantTemplate.delete(participant.getAccessToken());
    }

    @Test
    void unsaveEventSuccess() {
        TokenDto organisation = createOrganisationActivated();
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisation.getAccessToken());

        TokenDto participant = createParticipantActivated();

        eventTemplate.saveEvent(participant.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);


        EventResponseDto dto = eventTemplate.unsave(participant.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(dto.getIsSaved()).isFalse();
        assertThat(dto.getPeopleSaved()).isEqualTo(0);

        EventResponseDto found = eventTemplate.findById(participant.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        assertThat(found.getIsSaved()).isFalse();
        assertThat(found.getPeopleSaved()).isEqualTo(0);

        cleanUpEvent(loginTemplate.getAdminToken(), organisation.getAccessToken(), event);
        organisationTemplate.delete(organisation.getAccessToken());
        participantTemplate.delete(participant.getAccessToken());
    }

    @Test
    void joinEventSuccess() {
        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();
        ParticipantResponseDto participant = participantTemplate.getCurrentParticipant(participantToken.getAccessToken());
        TicketDto ticket = eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        assertThat(ticket).isNotNull();

        assertThat(ticket.getEvent()).isNotNull();
        assertThat(ticket.getEvent().getId()).isEqualTo(event.getId());
        assertThat(ticket.getEvent().getName()).isEqualTo(event.getName());

        assertThat(ticket.getParticipant()).isEqualTo(participant);
        assertThat(ticket.getIsValidated()).isFalse();
        assertThat(ticket.getRegistrationSubmission()).isNull();
        assertThat(ticket.getTimeOfSubmission()).isNotNull();
        assertThat(ticket.getExpiryDate()).isEqualTo(event.getEndTime());
        assertThat(ticket.getTicketCode()).isNotNull();

        EventResponseDto found = eventTemplate.findById(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getPeopleCount()).isEqualTo(1);

        EventSmallDto eventSmallDto = eventTemplate.leaveEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(eventSmallDto.getName()).isEqualTo(event.getName());
        assertThat(eventSmallDto.getId()).isEqualTo(event.getId());
        assertThat(eventSmallDto.getPeopleCount()).isEqualTo(0);

        cleanUpEvent(loginTemplate.getAdminToken(), organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void joinEventWithRegistrationFormSuccess() {
        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisationToken.getAccessToken());

        eventTemplate.addRegistrationForm(organisationToken.getAccessToken(), event.getId(), RegistrationFormMock.getRegistrationFormMock(), ItTestConsts.HTTP_SUCCESS);

        TokenDto participantToken = createParticipantActivated();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();
        ParticipantResponseDto participant = participantTemplate.getCurrentParticipant(participantToken.getAccessToken());
        TicketDto ticket = eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), submission, ItTestConsts.HTTP_SUCCESS);

        assertThat(ticket).isNotNull();

        assertThat(ticket.getEvent()).isNotNull();
        assertThat(ticket.getEvent().getId()).isEqualTo(event.getId());
        assertThat(ticket.getEvent().getName()).isEqualTo(event.getName());

        assertThat(ticket.getParticipant()).isEqualTo(participant);
        assertThat(ticket.getIsValidated()).isFalse();
        assertThat(ticket.getRegistrationSubmission()).usingRecursiveComparison().isEqualTo(submission);
        assertThat(ticket.getTimeOfSubmission()).isNotNull();
        assertThat(ticket.getExpiryDate()).isEqualTo(event.getEndTime());
        assertThat(ticket.getTicketCode()).isNotNull();

        EventResponseDto found = eventTemplate.findById(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getPeopleCount()).isEqualTo(1);

        EventSmallDto eventSmallDto = eventTemplate.leaveEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(eventSmallDto.getName()).isEqualTo(event.getName());
        assertThat(eventSmallDto.getId()).isEqualTo(event.getId());
        assertThat(eventSmallDto.getPeopleCount()).isEqualTo(0);

        cleanUpEvent(loginTemplate.getAdminToken(), organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void joinEventAlreadyJoined() {
        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();
        eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_BAD_REQUEST);

        EventResponseDto found = eventTemplate.findById(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getPeopleCount()).isEqualTo(1);

        EventSmallDto eventSmallDto = eventTemplate.leaveEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(eventSmallDto.getName()).isEqualTo(event.getName());
        assertThat(eventSmallDto.getId()).isEqualTo(event.getId());
        assertThat(eventSmallDto.getPeopleCount()).isEqualTo(0);

        cleanUpEvent(loginTemplate.getAdminToken(), organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void joinEventFull() {
        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisationToken.getAccessToken());
        EventRequestDto requestDto = new EventRequestDto();
        requestDto.setCapacity(1);
        eventTemplate.update(organisationToken.getAccessToken(), event.getId(), requestDto);

        TokenDto participantToken = createParticipantActivated();
        eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);

        TokenDto participantToken2 = createParticipantActivated();
        eventTemplate.joinEvent(participantToken2.getAccessToken(), event.getId(), ItTestConsts.HTTP_BAD_REQUEST);

        EventResponseDto found = eventTemplate.findById(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getPeopleCount()).isEqualTo(1);

        EventSmallDto eventSmallDto = eventTemplate.leaveEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(eventSmallDto.getName()).isEqualTo(event.getName());
        assertThat(eventSmallDto.getId()).isEqualTo(event.getId());
        assertThat(eventSmallDto.getPeopleCount()).isEqualTo(0);

        cleanUpEvent(loginTemplate.getAdminToken(), organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
        participantTemplate.delete(participantToken.getAccessToken());
        participantTemplate.delete(participantToken2.getAccessToken());
    }

    @Test
    void joinEventNotFound() {
        TokenDto participantToken = createParticipantActivated();
        eventTemplate.joinEvent(participantToken.getAccessToken(), System.currentTimeMillis(), ItTestConsts.HTTP_NOT_FOUND);

        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void joinEventInvalidSubmission() {
        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisationToken.getAccessToken());
        eventTemplate.addRegistrationForm(organisationToken.getAccessToken(), event.getId(), RegistrationFormMock.getRegistrationFormMock(), ItTestConsts.HTTP_SUCCESS);

        TokenDto participantToken = createParticipantActivated();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();
        submission.getAnswers().get(1).setOptionIds(null);
        eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), submission, ItTestConsts.HTTP_BAD_REQUEST);

        EventResponseDto found = eventTemplate.findById(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getPeopleCount()).isEqualTo(0);

        cleanUpEvent(loginTemplate.getAdminToken(), organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void leaveEventNotFound() {
        TokenDto participantToken = createParticipantActivated();
        eventTemplate.leaveEvent(participantToken.getAccessToken(), System.currentTimeMillis(), ItTestConsts.HTTP_NOT_FOUND);

        participantTemplate.delete(participantToken.getAccessToken());
    }

    @Test
    void leaveEventNotRegistered() {
        TokenDto organisationToken = createOrganisationActivated();
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();
        eventTemplate.leaveEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_BAD_REQUEST);

        EventResponseDto found = eventTemplate.findById(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
        assertThat(found.getPeopleCount()).isEqualTo(0);

        cleanUpEvent(loginTemplate.getAdminToken(), organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
        participantTemplate.delete(participantToken.getAccessToken());
    }

//    @Test
//    void leaveEventPaidTicket() {
//        TokenDto organisationToken = createOrganisationActivated();
//        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisationToken.getAccessToken());
//        EventRequestDto requestDto = new EventRequestDto();
//        requestDto.setPrice(10f);
//        eventTemplate.update(organisationToken.getAccessToken(), event.getId(), requestDto);
//
//        TokenDto participantToken = createParticipantActivated();
//        eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
//
//        eventTemplate.leaveEvent(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_BAD_REQUEST);
//
//        EventResponseDto found = eventTemplate.findById(participantToken.getAccessToken(), event.getId(), ItTestConsts.HTTP_SUCCESS);
//        assertThat(found.getPeopleCount()).isEqualTo(1);
//    }
}
