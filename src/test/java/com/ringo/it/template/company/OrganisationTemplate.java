package com.ringo.it.template.company;

import com.ringo.dto.company.request.OrganisationRequestDto;
import com.ringo.dto.company.response.OrganisationResponseDto;
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
        Response response = httpPostWithParams(ItTestConsts.NO_TOKEN, dto, "sign-up", ItTestConsts.HTTP_SUCCESS);
        OrganisationResponseDto actual = response.getBody().as(OrganisationResponseDto.class);

        assertThat(actual.getEmail()).isEqualTo(dto.getEmail());
        assertThat(actual.getName()).isEqualTo(dto.getName());
        assertThat(actual.getUsername()).isEqualTo(dto.getUsername());
        assertThat(actual.getDescription()).isEqualTo(dto.getDescription());
        assertThat(actual.getContacts()).usingRecursiveComparison().ignoringFields("id").isEqualTo(dto.getContacts());

        return actual;
    }

    public OrganisationResponseDto update(String token, OrganisationRequestDto dto) {
        Response response = httpPut(token, dto, ItTestConsts.HTTP_SUCCESS);
        assertThat(response.statusCode()).isEqualTo(ItTestConsts.HTTP_SUCCESS);

        return response.getBody().as(OrganisationResponseDto.class);
    }

    public OrganisationResponseDto findById(String token, Long id, int expectedStatusCode) {
        Response response = httpGetWithParams(token, String.valueOf(id), expectedStatusCode);
        if (expectedStatusCode != ItTestConsts.HTTP_SUCCESS) {
            return null;
        }
        return response.getBody().as(OrganisationResponseDto.class);
    }

    public void delete(String token) {
        httpDelete(token, ItTestConsts.HTTP_SUCCESS);
    }

    public void delete(String token, int expectedStatusCode) {
        httpDelete(token, expectedStatusCode);
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
        Response response = httpPutWithParams(token, "profile-picture/remove", null, ItTestConsts.HTTP_SUCCESS);
        return response.getBody().as(OrganisationResponseDto.class);
    }

    public OrganisationResponseDto getCurrentOrganisation(String accessToken) {
        Response response = httpGetWithParams(accessToken, "", ItTestConsts.HTTP_SUCCESS);
        return response.getBody().as(OrganisationResponseDto.class);
    }

    public void createPaymentAccount(String token) {
        Response response = httpPostWithParams(token, null, "payment-account", ItTestConsts.HTTP_SUCCESS);
        assertThat(response.statusCode()).isEqualTo(ItTestConsts.HTTP_SUCCESS);
    }
}
