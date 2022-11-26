package com.dndn.promotions.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(Include.NON_NULL) //null인건 return 안하기
public class DrawVO {

    private Integer id;
    private Integer amount;
    private int total;
    private int winnerCnt;

}
