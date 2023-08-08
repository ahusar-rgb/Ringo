package com.ringo.it.template.company;

import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Component
public class ParticipantTemplate extends EndpointTemplate {

    @Override
    protected String getEndpoint() {
        return "participants";
    }

    public ParticipantResponseDto findById(String token, Long id) {
        Response response = httpGetPathParams(token, String.valueOf(id), ItTestConsts.HTTP_SUCCESS);
        return response.getBody().as(ParticipantResponseDto.class);
    }

    public ParticipantResponseDto create(ParticipantRequestDto dto) {
        Response response = httpPostWithParams(null, dto, "sign-up", ItTestConsts.HTTP_SUCCESS);
        ParticipantResponseDto actual = response.getBody().as(ParticipantResponseDto.class);

        assertThat(actual.getEmail()).isEqualTo(dto.getEmail());
        assertThat(actual.getName()).isEqualTo(dto.getName());
        assertThat(actual.getUsername()).isEqualTo(dto.getUsername());
        assertThat(actual.getDateOfBirth()).isEqualTo(dto.getDateOfBirth());
        assertThat(actual.getGender()).isEqualTo(dto.getGender());

        return actual;
    }

    public ParticipantResponseDto update(String token, ParticipantRequestDto dto) {
        Response response = httpPut(token, dto, ItTestConsts.HTTP_SUCCESS);

        assertThat(response.statusCode()).isEqualTo(ItTestConsts.HTTP_SUCCESS);
        return response.getBody().as(ParticipantResponseDto.class);
    }

    public void delete(String token) {
        httpDelete(token, ItTestConsts.HTTP_SUCCESS);
    }
}
