package com.dndn.promotions.repository;

import com.dndn.promotions.model.TestEntity;
import com.dndn.promotions.model.UserDrawResultVO;
import com.dndn.promotions.model.UserVO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromotionRepository {

    UserVO getUser(UserVO userVO);
    void insertUser(UserVO userVO);

    List<UserDrawResultVO> getDrawResult();

}
