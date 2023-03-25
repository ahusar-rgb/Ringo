package com.ringo.mapper.common;

import com.ringo.dto.common.PhotoDto;
import com.ringo.model.common.Photo;

public interface PhotoMapper<E extends PhotoDto, T extends Photo> extends EntityMapper<E, T>{
}
