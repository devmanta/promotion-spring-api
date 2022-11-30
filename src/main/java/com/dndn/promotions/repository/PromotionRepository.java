package com.dndn.promotions.repository;

import com.dndn.promotions.model.DrawVO;
import com.dndn.promotions.model.UserDrawResultVO;
import com.dndn.promotions.model.UserVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromotionRepository {

    UserVO getUser(UserVO userVO);
    void insertUser(UserVO userVO);

    List<UserDrawResultVO> getDrawResult(Integer userId);

    void addUserDrawCntById(UserVO userVO);
    void deleteDrawResultByUserId(Integer userId);
    DrawVO getDrawtById(Integer id);
    void addDrawWinnerCntById(Integer drawId);

    Map<String, Integer> getDrawResultByUserId(Integer userId);
    void deductDrawWinnerCntById(Integer drawId);
}
