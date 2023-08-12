package com.ringo.mapper.company;

import com.ringo.dto.company.LabelDto;
import com.ringo.mapper.common.SingleDtoEntityMapper;
import com.ringo.model.company.Label;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelMapper extends SingleDtoEntityMapper<LabelDto, Label> {
}
