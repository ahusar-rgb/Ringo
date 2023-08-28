package com.ringo.it.template.company;

import com.ringo.dto.common.TicketCode;
import com.ringo.dto.company.TicketDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class TicketTemplate extends EndpointTemplate {
    @Override
    protected String getEndpoint() {
        return "tickets";
    }

    public TicketDto scanTicket(String token, String ticketCode, int expectedHttpCode) {
        Response response = httpPostWithParams(token, new TicketCode(ticketCode), "scan", expectedHttpCode);
        if(expectedHttpCode != ItTestConsts.HTTP_SUCCESS) {
            return null;
        }
        return response.getBody().as(TicketDto.class);
    }

    public void validateTicket(String token, String ticketCode, int expectedHttpCode) {
        httpPostWithParams(token, new TicketCode(ticketCode), "validate", expectedHttpCode);
    }
}
