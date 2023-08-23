package com.ringo.it.itest.company;

import com.ringo.dto.company.EventResponseDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractEventIntegrationTest;
import com.ringo.it.util.ItTestConsts;
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
    }
}
