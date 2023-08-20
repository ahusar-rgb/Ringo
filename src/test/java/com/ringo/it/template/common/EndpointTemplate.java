package com.ringo.it.template.common;

import com.ringo.it.config.EnvVars;
import com.ringo.it.util.ItTestConsts;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class EndpointTemplate {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EndpointTemplate.class);

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
        log.info("Sending POST request to {}", getEndpointUrl());

        RequestSpecification request = setupRequest(token, data);
        Response response = request.post(getEndpointUrl());
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public <T> Response httpPostWithParams(String token, T data, String params, int expectedHttpCode) {
        String url = getEndpointUrl() + "/" + params;
        log.info("Sending POST request to {}", url);

        RequestSpecification request = setupRequest(token, data);
        Response response = request.post(url);
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public <T> Response httpPut(String token, T data, int expectedHttpCode) {
        log.info("Sending PUT request to {}", getEndpointUrl());
        RequestSpecification request = setupRequest(token, data);
        Response response = request.put(getEndpointUrl());
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public <T> Response httpPutWithParams(String token, String path, T data, int expectedHttpCode) {
        String url = getEndpointUrl() + "/" + path;
        log.info("Sending PUT request to {}", url);

        RequestSpecification request = setupRequest(token, data);
        Response response = request.put(getEndpointUrl() + "/" + path);
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public <T> Response httpPatch(String token, T data, int expectedHttpCode) {
        log.info("Sending PATCH request to {}", getEndpointUrl());

        RequestSpecification request = setupRequest(token, data);
        Response response = request.patch(getEndpointUrl());
        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public Response httpDeleteById(String token, Long id, int expectedCode) {

        String url = getEndpointUrl() + "/" + id;
        log.info("Sending DELETE request to {}", url);

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token);
        Response response = request.delete(url);
        assertThat(response.getStatusCode()).isEqualTo(expectedCode);
        return response;
    }

    public Response httpDelete(String token, int expectedCode) {
        log.info("Sending DELETE request to {}", getEndpointUrl());

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token);
        Response response = request.delete(getEndpointUrl());
        assertThat(response.getStatusCode()).isEqualTo(expectedCode);
        return response;
    }

    public Response httpDeleteWithParams(String token, String pathParams, int expectedCode) {
        String url = getEndpointUrl() + "/" + pathParams;
        log.info("Sending DELETE request to {}", url);

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token);
        Response response = request.delete(url);
        assertThat(response.getStatusCode()).isEqualTo(expectedCode);
        return response;
    }

    public Response httpGetWithParams(String token, String pathParams, int expectedHttpCode) {
        String url = getEndpointUrl() + "/" + pathParams;
        log.info("Sending GET request to {}", url);

        RequestSpecification request = setupRequest(token);
        Response response = StringUtils.isNotEmpty(pathParams) ?
                request.get(url) :
                request.get(getEndpointUrl());

        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    public <T> Response httpGetQueryString(String token, String queryString, int expectedHttpCode) {

        RequestSpecification request = setupRequest(token);

        Response response = StringUtils.isNotEmpty(queryString) ?
                request.get(getEndpointUrl() + "?" + queryString) :
                request.get(getEndpointUrl());

        assertThat(response.getStatusCode()).isEqualTo(expectedHttpCode);
        return response;
    }

    private <T> RequestSpecification setupRequest(String token, T data) {
        RequestSpecification request = setupRequest(token);
        if (data != null)
            request.body(data);
        return request;
    }

    private RequestSpecification setupRequest(String token) {
        if (token == null)
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

    public void activate(String token) {
        httpPostWithParams(token,null, "activate", ItTestConsts.HTTP_SUCCESS);
    }
}
