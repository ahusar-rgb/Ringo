package com.ringo.it.template.company;

import com.ringo.dto.company.CurrencyDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Component
public class CurrencyTemplate extends EndpointTemplate {

    @Override
    protected String getEndpoint() {
        return "currencies";
    }

    public CurrencyDto create(String token, CurrencyDto dto) {
        Response response = httpPost(token, dto, ItTestConsts.HTTP_SUCCESS);
        CurrencyDto actual = response.getBody().as(CurrencyDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public CurrencyDto findById(Long id) {
        Response response = httpGetWithParams(null, String.valueOf(id), ItTestConsts.HTTP_SUCCESS);
        return response.getBody().as(CurrencyDto.class);
    }

    public CurrencyDto update(String token, Long id, CurrencyDto dto) {
        Response response = httpPutWithParams(token, id.toString(), dto, ItTestConsts.HTTP_SUCCESS);
        CurrencyDto actual = response.getBody().as(CurrencyDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }
}
