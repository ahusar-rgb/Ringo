package com.ringo.it.template.company;

import com.ringo.dto.company.CategoryDto;
import com.ringo.it.template.common.EndpointTemplate;
import com.ringo.it.util.ItTestConsts;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Component
public class CategoryTemplate extends EndpointTemplate {
    @Override
    protected String getEndpoint() {
        return "categories";
    }

    public CategoryDto create(String token, CategoryDto dto) {
        Response response = httpPost(token, dto, ItTestConsts.HTTP_SUCCESS);
        CategoryDto actual = response.getBody().as(CategoryDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }

    public CategoryDto findById(Long id) {
        Response response = httpGetWithParams(null, String.valueOf(id), ItTestConsts.HTTP_SUCCESS);
        return response.getBody().as(CategoryDto.class);
    }

    public CategoryDto update(String token, Long id, CategoryDto dto) {
        Response response = httpPutWithParams(token, id.toString(), dto, ItTestConsts.HTTP_SUCCESS);
        CategoryDto actual = response.getBody().as(CategoryDto.class);
        assertThat(actual).isNotNull();
        return actual;
    }
}
