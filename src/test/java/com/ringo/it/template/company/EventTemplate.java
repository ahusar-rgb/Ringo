package com.ringo.it.template.company;

import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Component
public class EventTemplate extends EndpointTemplate {

    @Override
    protected String getEndpoint() {
        return "events";
    }


    public EventResponseDto create(String token, EventRequestDto dto) {
        Response response = httpPost(token, dto, ItTestConsts.HTTP_SUCCESS);
        EventResponseDto actual = response.getBody().as(EventResponseDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public EventResponseDto activate(String token, Long id) {
        Response response = httpPostWithParams(token, null, id.toString() + "/activate", ItTestConsts.HTTP_SUCCESS);
        EventResponseDto actual = response.getBody().as(EventResponseDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public EventResponseDto deactivate(String token, Long id) {
        Response response = httpPostWithParams(token, null, id.toString() + "/deactivate", ItTestConsts.HTTP_SUCCESS);
        EventResponseDto actual = response.getBody().as(EventResponseDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public EventResponseDto addPhoto(String token, Long id, File photo, String contentType) {
        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token);
        request.contentType("multipart/form-data");
        request.multiPart("file", photo, contentType);
        Response response = request.post(getEndpointUrl() + "/" + id + "/photos");
        EventResponseDto actual = response.getBody().as(EventResponseDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public EventResponseDto removePhoto(String token, Long id, Long photoId) {
        Response response = httpDeleteWithParams(token, id.toString() + "/photos/" + photoId.toString(), ItTestConsts.HTTP_SUCCESS);
        EventResponseDto actual = response.getBody().as(EventResponseDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public EventResponseDto setMainPhoto(String token, Long id, Long photoId) {
        Response response = httpPostWithParams(token, null, id.toString() + "/photos/main/" + photoId.toString(), ItTestConsts.HTTP_SUCCESS);
        EventResponseDto actual = response.getBody().as(EventResponseDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public EventResponseDto removeMainPhoto(String token, Long id) {
        Response response = httpDeleteWithParams(token, id.toString() + "/photos/main", ItTestConsts.HTTP_SUCCESS);
        EventResponseDto actual = response.getBody().as(EventResponseDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public EventResponseDto findById(Long id, int expectedStatusCode) {
        Response response = httpGetWithParams(null, String.valueOf(id), expectedStatusCode);
        if(expectedStatusCode == ItTestConsts.HTTP_NOT_FOUND)
            return null;
        return response.getBody().as(EventResponseDto.class);
    }

    public EventResponseDto findById(String token, Long id, int expectedStatusCode) {
        Response response = httpGetWithParams(token, String.valueOf(id), expectedStatusCode);
        if(expectedStatusCode == ItTestConsts.HTTP_NOT_FOUND)
            return null;
        return response.getBody().as(EventResponseDto.class);
    }

    public EventResponseDto update(String token, Long id, EventRequestDto dto) {
        Response response = httpPutWithParams(token, id.toString(), dto, ItTestConsts.HTTP_SUCCESS);
        EventResponseDto actual = response.getBody().as(EventResponseDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }
}
