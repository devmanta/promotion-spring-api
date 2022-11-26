package com.dndn.promotions.service;

import com.dndn.promotions.model.UserVO;
import com.dndn.promotions.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public UserVO getUser(UserVO userVo) {
        return promotionRepository.getUser(userVo);
    }

}
