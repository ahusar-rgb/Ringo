package com.ringo.it.template.security;

import com.ringo.dto.company.UserRequestDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class LoginTemplate extends EndpointTemplate {

    public String getToken(String email, String password) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail(email);
        userRequestDto.setPassword(password);

        Response response = httpPost(null, userRequestDto, ItTestConsts.HTTP_SUCCESS);
        return response.getBody().jsonPath().getString("accessToken");
    }

    @Override
    protected String getEndpoint() {
        return "auth/login";
    }
}
