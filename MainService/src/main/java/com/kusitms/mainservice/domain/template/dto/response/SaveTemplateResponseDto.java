package com.kusitms.mainservice.domain.template.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SaveTemplateResponseDto {
    private Long templateid;
    private Long userid;

public static SaveTemplateResponseDto of(Long templateid,Long userid){
    return SaveTemplateResponseDto.builder()
            .templateid(templateid)
            .userid(userid)
            .build();
}
}
