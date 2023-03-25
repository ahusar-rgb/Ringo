package com.ringo.dto.company;

import com.ringo.dto.common.PhotoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrgPhotoDto extends PhotoDto {
    private Long organisationId;
}
