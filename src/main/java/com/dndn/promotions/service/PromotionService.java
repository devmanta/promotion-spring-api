package com.dndn.promotions.service;

import com.dndn.promotions.model.DrawEntity;
import com.dndn.promotions.model.UserDrawResultEntity;
import com.dndn.promotions.model.UserEntity;
import com.dndn.promotions.repository.PromotionRepository;
import com.dndn.promotions.util.AesUtils;
import com.dndn.promotions.util.DrawUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {

    private final AesUtils aesUtils;
    private final PromotionRepository promotionRepository;

    private final DrawUtils drawUtils;

    @Transactional
    public ResponseEntity<UserEntity> doUserDraw(UserEntity userEntity) {
        UserEntity userFromDb = promotionRepository.getUser(userEntity);

        boolean isSoldOut = promotionRepository.isSoldOut();
        if(userFromDb == null) {
            userFromDb = new UserEntity();
            userFromDb.setContact(userEntity.getContact());
            promotionRepository.insertUser(userFromDb);
        }

        promotionRepository.deleteUserShareByContact(userEntity.getContact()); // 카톡 공유하기 성공 레코드 지우기

        UserDrawResultEntity drawResultByUser = promotionRepository.getDrawResultWithUserDetail(userFromDb.getId());

        if(drawResultByUser != null) {
            //당첨 결과가 있으면 응모하면 안됨
            userFromDb.setSoldOut(isSoldOut);
            userFromDb.setWin(false);
            return ResponseEntity.ok(userFromDb);
        }

        UserDrawResultEntity userDrawResult = new UserDrawResultEntity();
        userDrawResult.setId(userFromDb.getId());
        userDrawResult.setDrawCnt(userFromDb.getDrawCnt());
        userDrawResult.setSoldOut(isSoldOut);

        if(!isSoldOut) {
            userDrawResult.setWin(true);
            userDrawResult = this.doDraw(userDrawResult);
        } else {
            Map<String, Integer> param = new HashMap<>();
            param.put("userId", userDrawResult.getId());
            param.put("drawId", 4);
            promotionRepository.insertDrawResult(param);

            userDrawResult.setWin(false);
        }

        if(userDrawResult == null) {
            return ResponseEntity.internalServerError().body(null);
        }

        return ResponseEntity.ok(userDrawResult);
    }

    public List<UserDrawResultEntity> getDrawResult() throws Exception {
        List<UserDrawResultEntity> drawResult = promotionRepository.getDrawResultList(null);

        for(UserDrawResultEntity r : drawResult) {
            this.decryptUser(r);
        }

        return drawResult;
    }

    @Transactional
    public boolean removeDrawResultAsUserSharedByKakaoTalk(UserEntity userFromRequestBody) {
        UserEntity userFromDb = promotionRepository.getUser(userFromRequestBody);
        if(userFromDb == null) {
            return false;
        } else if(userFromDb.getDrawCnt() >= 4) {
            return false;
        }

        Map<String, Integer> drawResultByUserId = promotionRepository.getDrawResultByUserId(userFromDb.getId());

        promotionRepository.deductDrawWinnerCntById(drawResultByUserId.get("drawId"));
        promotionRepository.deleteDrawResultByUserId(userFromDb.getId());

        promotionRepository.insertUserShare(userFromDb.getContact());
        return true;
    }

//        당첨 로직 :
//        전체 인원 중 당첨 확률로 계산해 당첨
//        660,000원 (10/1,060, 약 0.9%)
//        66,000원 (50/1,060, 약 4.7%)
//        6,600원 (1,000/1,060, 약 94.3%)
//
//        500명 응오한것 중에 495명이 3등, 4명이 2등 1명이 1등
//        660,000원 (9/560, 약 1.6%)
//        66,000 (46/560, 약 8.2%)
//        6,600 (505/560, 약 90.2%)
    @Transactional
    public UserDrawResultEntity doDraw(UserDrawResultEntity drawResultByUser) {
        List<DrawEntity> drawList = promotionRepository.getDrawList();

        int totalWinnersCnt = 0; // 총 당첨자 수 (== 모집단)
        int winnersCntTillNow = 0; // 현재까지 총 당첨자 수

        for(DrawEntity d : drawList) {
            totalWinnersCnt += d.getTotal();
            winnersCntTillNow += d.getWinnerCnt();
        }

        int denominator = totalWinnersCnt - winnersCntTillNow; // 총 당첨자 수 - 현재까지 총 당첨자수가 당첨확률의 분모가 된다.

        int[] winProbability = new int[drawList.size()];

        DrawEntity jackpotResult = null; // 당첨결과
        for(int i = 0; i < winProbability.length; i++) {
            DrawEntity draw = drawList.get(i);

            // 만약에 마지막 순번이면은 그냥 마지막(제일 꼴등) 금액 당첨되게
            if(i == winProbability.length - 1) {
                jackpotResult = draw;
                break;
            }

            // 당첨진행 고고
            int remainCnt = draw.getTotal() - draw.getWinnerCnt(); // 남은 당첨 숫자
            double r = (double) remainCnt / (double) denominator;
            winProbability[i] = (int) Math.round(r * 100);

            if(winProbability[i] == 0) {
                // 확률 구하기가 소숫점은 안되가지고.. 0이면은 그냥 1% 확률로 추첨하기..
                winProbability[i] = 1;
            }

            boolean isJackpot = drawUtils.isPercentWin(winProbability[i]);

            if(isJackpot) {
                jackpotResult = draw;
                break;
            }
        }

        if(jackpotResult != null) {
            drawResultByUser.setAmount(jackpotResult.getAmount());

            Map<String, Integer> param = new HashMap<>();
            param.put("userId", drawResultByUser.getId());
            param.put("drawId", jackpotResult.getId());
            promotionRepository.insertDrawResult(param);

            promotionRepository.addUserDrawCntById(drawResultByUser);
            promotionRepository.addDrawWinnerCntById(jackpotResult.getId());
            return drawResultByUser;
        }

        return null;
    }

//    public void deleteKakaoShareRecord(String contact) {
//        promotionRepository.deleteUserShareByContact(contact);
//    }

    public boolean isKakaoShareSucced(String contact) {
        Map<String, Object> userShareByContact = promotionRepository.getUserShareByContact(contact);
        return userShareByContact != null;
    }

    public void encryptUser(UserEntity user) throws Exception{
        user.setContact(aesUtils.encryptAES256(user.getContact()));
    }

    public void decryptUser(UserEntity user) throws Exception {
        user.setContact(aesUtils.decryptAES256(user.getContact()));
    }
}
