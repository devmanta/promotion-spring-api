package com.dndn.promotions.repository;

import com.dndn.promotions.model.DrawVO;
import com.dndn.promotions.model.UserDrawResultVO;
import com.dndn.promotions.model.UserVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromotionRepository {

    UserVO getUser(UserVO userVO);
    void insertUser(UserVO userVO);

    List<UserDrawResultVO> getDrawResult();

    void updateUserDrawCntById(UserVO userVO);
    void deleteDrawResultByUserId(Integer userId);
    DrawVO getDrawtById(Integer id);
    void updateDrawWinnerCntById(DrawVO drawVO);
}
