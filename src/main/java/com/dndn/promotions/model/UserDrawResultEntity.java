package com.dndn.promotions.model;

import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDrawResultEntity extends UserEntity {

    @ApiModelProperty(notes = "당첨금액")
    private int amount;
    private LocalDateTime createDate;

}
