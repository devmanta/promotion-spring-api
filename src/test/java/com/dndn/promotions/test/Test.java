package com.dndn.promotions.test;

import com.dndn.promotions.model.UserDrawResultVO;
import com.dndn.promotions.model.UserVO;
import com.dndn.promotions.repository.PromotionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test {

    @Autowired
    PromotionRepository promotionRepository;
    @org.junit.jupiter.api.Test
    public void test(){
        List<UserDrawResultVO> drawResult = promotionRepository.getDrawResult(null);
        System.out.println("drawResult = " + drawResult);

        List<UserDrawResultVO> drawResult1 = promotionRepository.getDrawResult(5);
        System.out.println("drawResult1 = " + drawResult1);

        UserVO user = promotionRepository.getUser(null);
    }

}
