package com.dndn.promotions.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping(value = "/health")
    public ResponseEntity<Boolean> healthCheck() {
        return ResponseEntity.ok(true);
    }

}
