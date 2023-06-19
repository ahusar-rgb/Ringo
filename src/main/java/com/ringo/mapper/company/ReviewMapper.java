package com.ringo.mapper.company;

import com.ringo.dto.company.ReviewRequestDto;
import com.ringo.dto.company.ReviewResponseDto;
import com.ringo.mapper.common.EntityMapper;
import com.ringo.model.company.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ParticipantMapper.class})
public interface ReviewMapper extends EntityMapper<ReviewRequestDto, ReviewResponseDto, Review> {

    @Override
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "organisation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Review toEntity(ReviewRequestDto entityDto);
}