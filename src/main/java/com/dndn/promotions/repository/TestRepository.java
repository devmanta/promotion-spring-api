package com.dndn.promotions.repository;

import com.dndn.promotions.model.TestEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface TestRepository {

    List<TestEntity> testSelect();

}
