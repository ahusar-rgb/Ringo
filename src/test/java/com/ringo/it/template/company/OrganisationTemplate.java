package com.ringo.it.template.company;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Component
public class OrganisationTemplate extends EndpointTemplate {

    @Override
    protected String getEndpoint() {
        return "organisations";
    }

    public OrganisationResponseDto create(OrganisationRequestDto dto) {
        Response response = httpPostWithParams(null, dto, "sign-up", ItTestConsts.HTTP_SUCCESS);
        OrganisationResponseDto actual = response.getBody().as(OrganisationResponseDto.class);

        assertThat(actual.getEmail()).isEqualTo(dto.getEmail());
        assertThat(actual.getName()).isEqualTo(dto.getName());
        assertThat(actual.getUsername()).isEqualTo(dto.getUsername());
        assertThat(actual.getDescription()).isEqualTo(dto.getDescription());
        assertThat(actual.getContacts()).isEqualTo(dto.getContacts());

        return actual;
    }

    public OrganisationResponseDto update(String token, OrganisationRequestDto dto) {
        Response response = httpPut(token, dto, ItTestConsts.HTTP_SUCCESS);
        assertThat(response.statusCode()).isEqualTo(ItTestConsts.HTTP_SUCCESS);

        return response.getBody().as(OrganisationResponseDto.class);
    }

    public OrganisationResponseDto findById(String token, Long id) {
        Response response = httpGetPathParams(token, String.valueOf(id), ItTestConsts.HTTP_SUCCESS);
        return response.getBody().as(OrganisationResponseDto.class);
    }

    public void delete(String token) {
        httpDelete(token, ItTestConsts.HTTP_SUCCESS);
    }

    public OrganisationResponseDto setPhoto(String token, File file, String contentType) {
        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token);
        request.contentType("multipart/form-data");
        request.multiPart("file", file, contentType);

        Response response = request.put(getEndpointUrl() + "/profile-picture");
        assertThat(response.statusCode()).isEqualTo(ItTestConsts.HTTP_SUCCESS);

        return response.getBody().as(OrganisationResponseDto.class);
    }

    public OrganisationResponseDto removePhoto(String token) {
        Response response = httpPut(token, "profile-picture/remove", null, ItTestConsts.HTTP_SUCCESS);
        return response.getBody().as(OrganisationResponseDto.class);
    }
}
