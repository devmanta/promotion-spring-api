package com.dndn.promotions.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    protected ResponseEntity<Map<String, Object>> defaultException(Exception e) {
        log.error("ExcpetionHandler", e);
        HashMap<String, Object> res = new HashMap<>();
        res.put("errorMessage", e.getMessage());
        return ResponseEntity.ok(res);
    }

}
