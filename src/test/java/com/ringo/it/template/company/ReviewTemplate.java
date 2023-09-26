package com.ringo.it.template.company;

import com.ringo.dto.ReviewPageRequestDto;
import com.ringo.dto.company.request.ReviewRequestDto;
import com.ringo.dto.company.response.OrganisationResponseDto;
import com.ringo.dto.company.response.ReviewResponseDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewTemplate extends EndpointTemplate {
    @Override
    protected String getEndpoint() {
        return "organisations";
    }

    public OrganisationResponseDto createReview(String token, Long organisationId, ReviewRequestDto dto, int expectedHttpCode) {
        Response response = httpPostWithParams(token, dto, organisationId + "/reviews", expectedHttpCode);
        if(expectedHttpCode != ItTestConsts.HTTP_SUCCESS) {
            return null;
        }
        return response.getBody().as(OrganisationResponseDto.class);
    }

    public OrganisationResponseDto updateReviews(String token, Long organisationId, ReviewRequestDto dto, int expectedHttpCode) {
        Response response = httpPutWithParams(token, organisationId + "/reviews", dto, expectedHttpCode);
        if(expectedHttpCode != ItTestConsts.HTTP_SUCCESS) {
            return null;
        }
        return response.getBody().as(OrganisationResponseDto.class);
    }

    public OrganisationResponseDto deleteReview(String token, Long organisationId, int expectedHttpCode) {
        Response response = httpDeleteWithParams(token, organisationId + "/reviews", expectedHttpCode);
        if(expectedHttpCode != ItTestConsts.HTTP_SUCCESS) {
            return null;
        }
        return response.getBody().as(OrganisationResponseDto.class);
    }

    public List<ReviewResponseDto> findAllByOrganisation(String token, Long organisationId, ReviewPageRequestDto dto) {
        Response response = httpGetWithParams(
                token,
                organisationId + "/reviews?page=" + dto.getPage() + "&size=" + dto.getSize(),
                ItTestConsts.HTTP_SUCCESS
        );
        return response.getBody().as(List.class);
    }
}
