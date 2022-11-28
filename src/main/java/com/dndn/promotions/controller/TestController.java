package com.dndn.promotions.controller;

import com.dndn.promotions.model.TestEntity;
import com.dndn.promotions.model.UserVO;
import com.dndn.promotions.repository.PromotionRepository;
import com.dndn.promotions.repository.TestRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TestRepository testRepository;
    private final PromotionRepository promotionRepository;

    @GetMapping(value = "/test")
    public ResponseEntity<UserVO> test() {
        UserVO user = promotionRepository.getUser(null);
        return ResponseEntity.ok(user);
    }

    @GetMapping(value = "/test1")
    public ResponseEntity<TestEntity> test1() {
        return ResponseEntity.ok(TestEntity.builder().no(2).name("haha").build());
    }

}
