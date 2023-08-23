package com.ringo.it.itest.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.itest.common.AbstractIntegrationTest;
import com.ringo.it.util.ItTestConsts;
import com.ringo.mock.dto.OrganisationDtoMock;
import com.ringo.mock.dto.ParticipantDtoMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void loginUserNotFound() {
        loginTemplate.login(System.currentTimeMillis() + "@test.com", "password", ItTestConsts.HTTP_UNAUTHORIZED);
    }


    @Test
    void loginWrongPassword() {
        ParticipantRequestDto requestDto = ParticipantDtoMock.getParticipantMockDto();
        createParticipantActivated(requestDto);

        loginTemplate.login(requestDto.getEmail(), "wrong password", ItTestConsts.HTTP_UNAUTHORIZED);

        String token = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS).getAccessToken();
        participantTemplate.delete(token);
    }

    @Test
    void refreshTokenParticipant() {
        ParticipantRequestDto requestDto = ParticipantDtoMock.getParticipantMockDto();
        createParticipantActivated(requestDto);

        TokenDto tokenDto = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
        TokenDto refreshedTokenDto = loginTemplate.refreshToken(tokenDto.getRefreshToken(), ItTestConsts.HTTP_SUCCESS);

        participantTemplate.delete(refreshedTokenDto.getAccessToken());
    }

    @Test
    void refreshTokenOrganisation() {
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationDtoMock();
        createOrganisationActivated(requestDto);

        TokenDto tokenDto = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);
        TokenDto refreshedTokenDto = loginTemplate.refreshToken(tokenDto.getRefreshToken(), ItTestConsts.HTTP_SUCCESS);

        organisationTemplate.delete(refreshedTokenDto.getAccessToken());
    }

    @Test
    void refreshTokenWrongToken() {
        loginTemplate.refreshToken("wrong token", ItTestConsts.HTTP_UNAUTHORIZED);
    }

    @Test
    void changePasswordParticipant() {
        final String newPassword = "new password";
        ParticipantRequestDto requestDto = ParticipantDtoMock.getParticipantMockDto();
        createParticipantActivated(requestDto);

        TokenDto tokenDto = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);

        TokenDto newToken = loginTemplate.changePassword(tokenDto.getAccessToken(), requestDto.getPassword(), newPassword, ItTestConsts.HTTP_SUCCESS);

        loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_UNAUTHORIZED);
        loginTemplate.login(requestDto.getEmail(), newPassword, ItTestConsts.HTTP_SUCCESS);

        participantTemplate.delete(newToken.getAccessToken());
    }

    @Test
    void changePasswordParticipantWrongPassword() {
        final String newPassword = "new password";
        ParticipantRequestDto requestDto = ParticipantDtoMock.getParticipantMockDto();
        createParticipantActivated(requestDto);

        TokenDto tokenDto = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);

        loginTemplate.changePassword(tokenDto.getAccessToken(), "wrong password", newPassword, ItTestConsts.HTTP_UNAUTHORIZED);

        loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);

        participantTemplate.delete(tokenDto.getAccessToken());
    }

    @Test
    void changePasswordOrganisation() {
        final String newPassword = "new password";
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationDtoMock();
        createOrganisationActivated(requestDto);

        TokenDto tokenDto = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);

        TokenDto newToken = loginTemplate.changePassword(tokenDto.getAccessToken(), requestDto.getPassword(), newPassword, ItTestConsts.HTTP_SUCCESS);

        loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_UNAUTHORIZED);
        loginTemplate.login(requestDto.getEmail(), newPassword, ItTestConsts.HTTP_SUCCESS);

        organisationTemplate.delete(newToken.getAccessToken());
    }

    @Test
    void changePasswordOrganisationWrongPassword() {
        final String newPassword = "new password";
        OrganisationRequestDto requestDto = OrganisationDtoMock.getOrganisationDtoMock();
        createOrganisationActivated(requestDto);

        TokenDto tokenDto = loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);

        loginTemplate.changePassword(tokenDto.getAccessToken(), "wrong password", newPassword, ItTestConsts.HTTP_UNAUTHORIZED);

        loginTemplate.login(requestDto.getEmail(), requestDto.getPassword(), ItTestConsts.HTTP_SUCCESS);

        organisationTemplate.delete(tokenDto.getAccessToken());
    }
}
