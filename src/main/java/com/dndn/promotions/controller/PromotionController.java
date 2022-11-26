package com.dndn.promotions.controller;

import com.dndn.promotions.model.UserVO;
import com.dndn.promotions.service.PromotionService;
import com.dndn.promotions.util.CryptoGenerator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping(value = {"/generator"})
    public ResponseEntity<Map<String, String>> cryptoGenerator(HttpServletRequest request) {
        HttpSession session = request.getSession();

        Map<String, String> publicKeyMap = CryptoGenerator.generatePairKey(session);
        return ResponseEntity.ok(publicKeyMap);
    }

    @PostMapping(value = "/user")
    public ResponseEntity<UserVO> insertNewUser(HttpServletRequest request, @RequestBody UserVO userVO) throws Exception {
        String decryptedContact = CryptoGenerator.decryptRSA(request.getSession(), userVO.getContact());
        userVO.setContact(decryptedContact);

        promotionService.insertUser(userVO);
        return ResponseEntity.ok(userVO);
    }

    @GetMapping(value = "/user/{contact}")
    public ResponseEntity<UserVO> getUserByContact(HttpServletRequest request, @PathVariable String contact) throws Exception {
        String decryptedContact = CryptoGenerator.decryptRSA(request.getSession(), contact);

        UserVO user = UserVO.builder().contact(decryptedContact).build();
        return ResponseEntity.ok(promotionService.getUser(user));
    }

}
