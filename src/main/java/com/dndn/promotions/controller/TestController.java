package com.dndn.promotions.controller;

import com.dndn.promotions.model.TestEntity;
import com.dndn.promotions.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.ResolverUtil.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TestRepository testRepository;

    @GetMapping(value = "/test")
    public ResponseEntity<TestEntity> test() {
        TestEntity testEntity = testRepository.testSelect();
        return ResponseEntity.ok(testEntity);
    }

    @GetMapping(value = "/test1")
    public ResponseEntity<TestEntity> test1() {
        return ResponseEntity.ok(TestEntity.builder().no(2).name("haha").build());
    }

}
