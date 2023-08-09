package com.ringo.it.template.common;

import com.ringo.it.config.EnvVars;
import com.ringo.it.util.ItTestConsts;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public abstract class EndpointTemplate {

    @Autowired
    private Environment environment;

    protected String getBaseUrl() {
        return environment.getProperty(EnvVars.BASE_URL);
    }

    protected abstract String getEndpoint();

    public void delete(String token, Long id) {
        httpDeleteById(token, id, ItTestConsts.HTTP_SUCCESS);
    }

    protected String getEndpointUrl() {
        System.out.println(getBaseUrl() + "/" + getEndpoint());
        return getBaseUrl() + "/" + getEndpoint();
    }

    public <T> Response httpPost(String token, T data, int expectedHttpCode) {

        //log.info("Sending POST request to {}", getEndpointUrl());

        RequestSpecification request = setupRequest(token, data);
        Response response = request.post(getEndpointUrl());
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public <T> Response httpPostWithParams(String token, T data, String params, int expectedHttpCode) {

        //log.info("Sending POST request to {}", getEndpointUrl());

        RequestSpecification request = setupRequest(token, data);
        Response response = request.post(getEndpointUrl() + "/" + params);
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public  <T> Response httpPut(String token, T data, int expectedHttpCode) {

        RequestSpecification request = setupRequest(token, data);
        Response response = request.put(getEndpointUrl());
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public  <T> Response httpPut(String token, String path, T data, int expectedHttpCode) {

        RequestSpecification request = setupRequest(token, data);

        Response response = request.put(getEndpointUrl() + "/" + path);
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public  <T> Response httpPatch(String token, T data, int expectedHttpCode) {

        RequestSpecification request = setupRequest(token, data);
        Response response = request.patch(getEndpointUrl());
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public  <T> Response httpPatch(String token, String path, T data, int expectedHttpCode) {

        RequestSpecification request = setupRequest(token, data);
        Response response = request.patch(getEndpointUrl() + "/" + path);
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public Response httpDeleteById(String token, Long id, int expectedCode) {

        //log.info("Sending DELETE request to {}", getEndpointUrl() + "/" + id);

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token);
        Response response = request.delete(getEndpointUrl() + "/" + id);
        assertThat(response.getStatusCode()).isEqualTo(expectedCode);
        return response;
    }

    public Response httpDelete(String token, int expectedCode) {
        //log.info("Sending DELETE request to {}", getEndpointUrl());

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token);
        Response response = request.delete(getEndpointUrl());
        assertThat(response.getStatusCode()).isEqualTo(expectedCode);
        return response;
    }

    public  <T> Response httpGetPathParams(String token, String pathParams, int expectedHttpCode) {

        //log.info("Sending GET request to {}", getEndpointUrl() + "/" + pathParams);

        RequestSpecification request = setupRequest(token);

        Response response = StringUtils.isNotEmpty(pathParams) ?
                request.get(getEndpointUrl() + "/" + pathParams) :
                request.get(getEndpointUrl());

        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public  <T> Response httpGetQueryString(String token, String queryString, int expectedHttpCode) {

        RequestSpecification request = setupRequest(token);

        Response response = StringUtils.isNotEmpty(queryString) ?
                request.get(getEndpointUrl() + "?" + queryString) :
                request.get(getEndpointUrl());

        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    private <T> RequestSpecification setupRequest(String token, T data) {
        RequestSpecification request = setupRequest(token);
        if(data != null)
            request.body(data);
        return request;
    }

    private RequestSpecification setupRequest(String token) {
        if(token == null)
            return unAuthorizedRequest();
        return StringUtils.isEmpty(token) ? unAuthorizedRequest() : authorizedRequest(token);
    }

    private RequestSpecification authorizedRequest(String token) {
        RequestSpecification request = unAuthorizedRequest();
        request.header("Authorization", "Bearer " + token);
        return request;
    }

    private RequestSpecification unAuthorizedRequest() {
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        return request;
    }

}
