package com.dndn.promotions.controller;

import com.dndn.promotions.model.TestEntity;
import com.dndn.promotions.model.UserVO;
import com.dndn.promotions.repository.PromotionRepository;
import com.dndn.promotions.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping(value = "/user")
    public ResponseEntity<UserVO> getUser(@RequestBody UserVO userVO) {
        promotionService.insertUser(userVO);
        return ResponseEntity.ok(userVO);
    }

    @GetMapping(value = "/user/{contact}")
    public ResponseEntity<UserVO> getUser(@PathVariable String contact) {
        UserVO user = UserVO.builder().contact(contact).build();
        return ResponseEntity.ok(promotionService.getUser(user));
    }

}
