package com.dndn.promotions.test;

import com.dndn.promotions.repository.PromotionRepository;
import com.dndn.promotions.util.AesUtils;
import com.dndn.promotions.util.DrawUtils;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test {

    @Autowired
    PromotionRepository promotionRepository;
    @Autowired
    DrawUtils drawUtils;
    @Autowired
    AesUtils aesUtils;

    @org.junit.jupiter.api.Test
    @DisplayName("암호화값 출력")
    void encrypt() throws Exception {
        String s = aesUtils.encryptAES256("01049675780");
        System.out.println("s = " + s);
    }

    @org.junit.jupiter.api.Test
    void test() throws Exception {
        int a = 1;
        int b= 3;

        double r = (double) a / (double) b;
        int round = (int) Math.round(r * 10);
        System.out.println("round = " + round);


    }

}
