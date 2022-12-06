package com.dndn.promotions.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserEntity {

    private Integer id;
    private String contact;
    @ApiModelProperty(notes = "응모횟수 / 총 응모횟수는 사용자당 4번 으로 제한")
    private Integer drawCnt;
    @ApiModelProperty(notes = "당첨 소진 여부")
    private Boolean soldOut;
    @ApiModelProperty(notes = "당첨인지 아닌지")
    private boolean win;

}
