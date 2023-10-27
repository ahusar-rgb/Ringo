package com.ringo.it.itest.company;

import com.ringo.dto.company.JoinEventResult;
import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractEventIntegrationTest;
import com.ringo.it.util.ItTestConsts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@ExtendWith(SpringExtension.class)
public class JoiningIntentIntegrationTest extends AbstractEventIntegrationTest {

    @Test
    void joinEventExpired() {
        TokenDto organisationToken = createOrganisationActivated();
        organisationTemplate.createPaymentAccount(organisationToken.getAccessToken());
        EventResponseDto event = createEventActivated(loginTemplate.getAdminToken(), organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        JoinEventResult joinEventResult = eventTemplate.joinEvent(
                participantToken.getAccessToken(),
                event.getId(),
                event.getTicketTypes().get(1).getId(),
                ItTestConsts.HTTP_SUCCESS);

        assertThat(joinEventResult.getTicket()).isNull();
        assertThat(joinEventResult.getOrganisationAccountId()).isNotNull();
        assertThat(joinEventResult.getPaymentIntentClientSecret()).isNotNull();
    }
}
