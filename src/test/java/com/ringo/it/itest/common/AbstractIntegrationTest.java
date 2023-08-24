package com.ringo.it.itest.common;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.config.TestConfiguration;
import com.ringo.it.template.company.OrganisationTemplate;
import com.ringo.it.template.company.ParticipantTemplate;
import com.ringo.it.template.security.LoginTemplate;
import com.ringo.it.util.ItTestConsts;
import com.ringo.mock.dto.OrganisationDtoMock;
import com.ringo.mock.dto.ParticipantDtoMock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected ParticipantTemplate participantTemplate;
    @Autowired
    protected OrganisationTemplate organisationTemplate;
    @Autowired
    protected LoginTemplate loginTemplate;

    protected TokenDto createOrganisationActivated(OrganisationRequestDto organisationRequestDto) {
        organisationTemplate.create(organisationRequestDto);

        loginTemplate.verifyEmail(organisationRequestDto.getEmail(), organisationRequestDto.getUsername());
        return loginTemplate.login(organisationRequestDto.getEmail(), organisationRequestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
    }

    protected TokenDto createOrganisationActivated() {
        OrganisationRequestDto organisationRequestDto = OrganisationDtoMock.getOrganisationDtoMock();
        organisationTemplate.create(organisationRequestDto);

        loginTemplate.verifyEmail(organisationRequestDto.getEmail(), organisationRequestDto.getUsername());
        return loginTemplate.login(organisationRequestDto.getEmail(), organisationRequestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
    }

    protected TokenDto createParticipantActivated(ParticipantRequestDto participantRequestDto) {
        participantTemplate.create(participantRequestDto);

        loginTemplate.verifyEmail(participantRequestDto.getEmail(), participantRequestDto.getUsername());
        return loginTemplate.login(participantRequestDto.getEmail(), participantRequestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
    }

    protected TokenDto createParticipantActivated() {
        ParticipantRequestDto participantRequestDto = ParticipantDtoMock.getParticipantMockDto();
        participantTemplate.create(participantRequestDto);

        loginTemplate.verifyEmail(participantRequestDto.getEmail(), participantRequestDto.getUsername());
        return loginTemplate.login(participantRequestDto.getEmail(), participantRequestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
    }
}
