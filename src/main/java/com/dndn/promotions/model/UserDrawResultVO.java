package com.dndn.promotions.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDrawResultVO extends UserVO {

    private int amount;

    UserDrawResultVO(Integer id, String contact, Integer drawCnt) {
        super(id, contact, drawCnt);
    }

}
