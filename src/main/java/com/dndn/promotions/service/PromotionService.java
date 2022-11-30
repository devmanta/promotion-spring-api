package com.dndn.promotions.service;

import com.dndn.promotions.model.UserDrawResultEntity;
import com.dndn.promotions.model.UserEntity;
import com.dndn.promotions.repository.PromotionRepository;
import com.dndn.promotions.util.AesUtils;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {

    private final AesUtils aesUtils;
    private final PromotionRepository promotionRepository;

    public UserEntity getUser(UserEntity userEntity) throws Exception {
        this.encryptUser(userEntity);
        return promotionRepository.getUser(userEntity);
    }

    public void insertUser(UserEntity userEntity) throws Exception {
        userEntity.setContact(aesUtils.encryptAES256(userEntity.getContact()));
        promotionRepository.insertUser(userEntity);
    }

    public List<UserDrawResultEntity> getDrawResult() throws Exception {
        List<UserDrawResultEntity> drawResult = promotionRepository.getDrawResult(null);

        for(UserDrawResultEntity r : drawResult) {
            this.decryptUser(r);
        }

        return drawResult;
    }

    public UserDrawResultEntity getDrawResultForUser(Integer userId) {
        List<UserDrawResultEntity> drawResult = promotionRepository.getDrawResult(userId);
        if(drawResult.size() == 1) {
            return drawResult.get(0);
        }

        return null;
    }

    @Transactional
    public boolean removeDrawResultAsUserSharedByKakaoTalk(UserEntity userFromRequestBody) {
        UserEntity userFromDb = promotionRepository.getUser(userFromRequestBody);
        if(userFromDb == null) {
            return false;
        } else if(userFromDb.getDrawCnt() > 3) {
            return false;
        }

        Map<String, Integer> drawResultByUserId = promotionRepository.getDrawResultByUserId(userFromDb.getId());

        promotionRepository.deductDrawWinnerCntById(drawResultByUserId.get("drawId"));
        promotionRepository.deleteDrawResultByUserId(userFromDb.getId());

        return true;
    }


    public void encryptUser(UserEntity user) throws Exception{
        user.setContact(aesUtils.encryptAES256(user.getContact()));
    }

    public void decryptUser(UserEntity user) throws Exception {
        user.setContact(aesUtils.decryptAES256(user.getContact()));
    }
}
