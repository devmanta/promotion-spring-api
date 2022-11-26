package com.dndn.promotions.service;

import com.dndn.promotions.model.UserDrawResultVO;
import com.dndn.promotions.model.UserVO;
import com.dndn.promotions.repository.PromotionRepository;
import com.dndn.promotions.util.AesUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {

    private final AesUtils aesUtils;
    private final PromotionRepository promotionRepository;

    public UserVO getUser(UserVO userVo) throws Exception {
        this.encryptUser(userVo);
        return promotionRepository.getUser(userVo);
    }

    public void insertUser(UserVO userVO) throws Exception {
        userVO.setContact(aesUtils.encryptAES256(userVO.getContact()));
        promotionRepository.insertUser(userVO);
    }

    public List<UserDrawResultVO> getDrawResult() throws Exception {
        List<UserDrawResultVO> drawResult = promotionRepository.getDrawResult();

        for(UserDrawResultVO r : drawResult) {
            this.decryptUser(r);
        }

        return drawResult;
    }




    public void encryptUser(UserVO user) throws Exception{
        user.setContact(aesUtils.encryptAES256(user.getContact()));
    }

    public void decryptUser(UserVO user) throws Exception {
        user.setContact(aesUtils.decryptAES256(user.getContact()));
    }
}
