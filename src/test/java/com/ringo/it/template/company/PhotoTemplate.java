package com.ringo.it.template.company;

import com.ringo.it.template.common.EndpointTemplate;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class PhotoTemplate extends EndpointTemplate {

    @Override
    protected String getEndpoint() {
        return "photos";
    }

    public byte[] findPhoto(Long id, int expectedStatusCode) {
        Response response = httpGetWithParams(null, id.toString(), expectedStatusCode);
        return response.getBody().asByteArray();
    }

    @Override
    public void delete(String token, Long id) {
        throw new UnsupportedOperationException();
    }
}
