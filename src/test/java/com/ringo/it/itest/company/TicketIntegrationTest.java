package com.ringo.it.itest.company;

import com.ringo.dto.company.CategoryDto;
import com.ringo.dto.company.response.EventResponseDto;
import com.ringo.dto.company.response.TicketDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractEventIntegrationTest;
import com.ringo.it.template.company.TicketTemplate;
import com.ringo.it.util.ItTestConsts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Comparator;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class TicketIntegrationTest extends AbstractEventIntegrationTest {

    @Autowired
    private TicketTemplate ticketTemplate;

    @Test
    void scanTicketSuccess() {
        TokenDto organisationToken = createOrganisationActivated();
        String adminToken = loginTemplate.getAdminToken();
        EventResponseDto event = createEventActivated(adminToken, organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        TicketDto ticket = eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), event.getTicketTypes().get(0).getId(), ItTestConsts.HTTP_SUCCESS);

        TicketDto scanned = ticketTemplate.scanTicket(organisationToken.getAccessToken(), ticket.getTicketCode(), ItTestConsts.HTTP_SUCCESS);

        assertThat(scanned).usingRecursiveComparison().ignoringFields("ticketCode", "categories").isEqualTo(ticket);
        assertThat(scanned.getEvent().getCategories().stream().sorted(Comparator.comparing(CategoryDto::getId))
                    .collect(Collectors.toList()))
                .isEqualTo(event.getCategories().stream().sorted(Comparator.comparing(CategoryDto::getId))
                        .collect(Collectors.toList()));

        participantTemplate.delete(participantToken.getAccessToken());
        cleanUpEvent(adminToken, organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void scanTicketNotHost() {
        TokenDto organisationToken = createOrganisationActivated();
        String adminToken = loginTemplate.getAdminToken();
        EventResponseDto event = createEventActivated(adminToken, organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        TicketDto ticket = eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), event.getTicketTypes().get(0).getId(), ItTestConsts.HTTP_SUCCESS);

        TokenDto organisationToken2 = createOrganisationActivated();
        ticketTemplate.scanTicket(organisationToken2.getAccessToken(), ticket.getTicketCode(), ItTestConsts.HTTP_BAD_REQUEST);

        participantTemplate.delete(participantToken.getAccessToken());
        cleanUpEvent(adminToken, organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
        organisationTemplate.delete(organisationToken2.getAccessToken());
    }

    @Test
    void scanTicketInvalidTicketCode() {
        String ticketCode = "invalidToken";

        TokenDto organisationToken = createOrganisationActivated();
        ticketTemplate.scanTicket(organisationToken.getAccessToken(), ticketCode, ItTestConsts.HTTP_BAD_REQUEST);

        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void validateTicketSuccess() {
        TokenDto organisationToken = createOrganisationActivated();
        String adminToken = loginTemplate.getAdminToken();
        EventResponseDto event = createEventActivated(adminToken, organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        TicketDto ticket = eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), event.getTicketTypes().get(0).getId(), ItTestConsts.HTTP_SUCCESS);

        ticketTemplate.validateTicket(organisationToken.getAccessToken(), ticket.getTicketCode(), ItTestConsts.HTTP_SUCCESS);

        participantTemplate.delete(participantToken.getAccessToken());
        cleanUpEvent(adminToken, organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
    }

    @Test
    void validateTicketNotHost() {
        TokenDto organisationToken = createOrganisationActivated();
        String adminToken = loginTemplate.getAdminToken();
        EventResponseDto event = createEventActivated(adminToken, organisationToken.getAccessToken());

        TokenDto participantToken = createParticipantActivated();

        TicketDto ticket = eventTemplate.joinEvent(participantToken.getAccessToken(), event.getId(), event.getTicketTypes().get(0).getId(), ItTestConsts.HTTP_SUCCESS);

        TokenDto organisationToken2 = createOrganisationActivated();
        ticketTemplate.validateTicket(organisationToken2.getAccessToken(), ticket.getTicketCode(), ItTestConsts.HTTP_BAD_REQUEST);

        participantTemplate.delete(participantToken.getAccessToken());
        cleanUpEvent(adminToken, organisationToken.getAccessToken(), event);
        organisationTemplate.delete(organisationToken.getAccessToken());
        organisationTemplate.delete(organisationToken2.getAccessToken());
    }

    @Test
    void validateTicketInvalidToken() {
        String ticketCode = "invalidToken";

        TokenDto organisationToken = createOrganisationActivated();
        ticketTemplate.validateTicket(organisationToken.getAccessToken(), ticketCode, ItTestConsts.HTTP_BAD_REQUEST);

        organisationTemplate.delete(organisationToken.getAccessToken());
    }
}
