package com.dndn.promotions.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(Include.NON_NULL)
public class UserVO {

    private Integer id;
    private String contact;
    private Integer drawCnt;
    private Integer kakaoShareCnt;

}
