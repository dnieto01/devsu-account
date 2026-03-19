package com.ds.devsuaccount.infraestructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("ping")
    public ResponseEntity<?> get() {
        return new ResponseEntity<>("pong", HttpStatus.OK);
    }
}
