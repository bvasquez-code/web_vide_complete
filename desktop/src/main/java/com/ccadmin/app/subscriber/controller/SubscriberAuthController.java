package com.ccadmin.app.subscriber.controller;

import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import com.ccadmin.app.subscriber.model.dto.SubscriberLoginDto;
import com.ccadmin.app.subscriber.model.dto.SubscriberRegisterDto;
import com.ccadmin.app.subscriber.service.SubscriberAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/public/auth")
public class SubscriberAuthController {
    private final SubscriberAuthService authService;

    public SubscriberAuthController(SubscriberAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("register")
    public ResponseEntity<ResponseWsDto> register(@RequestBody SubscriberRegisterDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(authService.register(dto)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("login")
    public ResponseEntity<ResponseWsDto> login(@RequestBody SubscriberLoginDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(authService.login(dto)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("me")
    public ResponseEntity<ResponseWsDto> me() {
        try {
            return new ResponseEntity<>(new ResponseWsDto(authService.me()), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.UNAUTHORIZED);
        }
    }
}
