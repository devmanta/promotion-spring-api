package com.dndn.promotions.repository;

import com.dndn.promotions.model.DrawEntity;
import com.dndn.promotions.model.UserDrawResultEntity;
import com.dndn.promotions.model.UserEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromotionRepository {

    UserEntity getUser(UserEntity userEntity);
    void insertUser(UserEntity userEntity);

    List<UserDrawResultEntity> getDrawResultList();
    UserDrawResultEntity getDrawResultWithUserDetail(Integer userId);

    void addUserDrawCntById(UserEntity userEntity);
    void deleteDrawResultByUserId(Integer userId);
    DrawEntity getDrawtById(Integer id);
    List<DrawEntity> getDrawList();

    void addDrawWinnerCntById(Integer drawId);

    Map<String, Integer> getDrawResultByUserId(Integer userId);
    void deductDrawWinnerCntById(Integer drawId);

    void insertDrawResult(Map<String, Integer> params);

    boolean isSoldOut();

    void insertUserShare(String contact);
    Map<String, Object> getUserShareByContact(String contact);
    void deleteUserShareByContact(String contact);
    void insertDrawResultHistory(Integer userId);
}
