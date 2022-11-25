package com.dndn.promotions.repository;

import com.dndn.promotions.model.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TestRepository {

    @Select("select * from devmanta.test")
    TestEntity testSelect();

}
