package com.ringo.it.template.security;

import com.ringo.dto.auth.ChangePasswordForm;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Component
public class LoginTemplate extends EndpointTemplate {

    public TokenDto login(String email, String password, int expectedStatusCode) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail(email);
        userRequestDto.setPassword(password);

        Response response = httpPostWithParams(ItTestConsts.NO_TOKEN, userRequestDto, "login", expectedStatusCode);

        if(expectedStatusCode == ItTestConsts.HTTP_SUCCESS) {
            TokenDto tokenDto = response.getBody().as(TokenDto.class);
            assertThat(tokenDto.getAccessToken()).isNotNull();
            assertThat(tokenDto.getRefreshToken()).isNotNull();
            return tokenDto;
        } else {
            return null;
        }
    }

    public TokenDto refreshToken(String token, int expectedStatusCode) {
        Response response = httpGetPathParams(token, "refresh-token", expectedStatusCode);
        if(expectedStatusCode != ItTestConsts.HTTP_SUCCESS) {
            return null;
        }

        TokenDto tokenDto = response.getBody().as(TokenDto.class);
        assertThat(tokenDto.getAccessToken()).isNotNull();
        assertThat(tokenDto.getRefreshToken()).isNotNull();

        return tokenDto;
    }

    public TokenDto changePassword(String token, String password, String newPassword, int expectedStatusCode) {
        ChangePasswordForm dto = new ChangePasswordForm();
        dto.setPassword(password);
        dto.setNewPassword(newPassword);

        Response response = httpPostWithParams(token, dto, "change-password", expectedStatusCode);
        if(expectedStatusCode != ItTestConsts.HTTP_SUCCESS) {
            return null;
        } else {
            assertThat(response.getBody().as(TokenDto.class).getAccessToken()).isNotNull();
            assertThat(response.getBody().as(TokenDto.class).getRefreshToken()).isNotNull();
        }
        return response.getBody().as(TokenDto.class);
    }

    @Override
    protected String getEndpoint() {
        return "auth";
    }
}
