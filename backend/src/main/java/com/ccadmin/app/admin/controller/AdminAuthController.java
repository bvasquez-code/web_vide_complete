package com.ccadmin.app.admin.controller;

import com.ccadmin.app.admin.model.dto.LoginDto;
import com.ccadmin.app.admin.service.AdminAuthService;
import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin/auth")
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("login")
    public ResponseEntity<ResponseWsDto> login(@RequestBody LoginDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(adminAuthService.login(dto)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("session")
    public ResponseEntity<ResponseWsDto> session() {
        return new ResponseEntity<>(new ResponseWsDto("OK"), HttpStatus.OK);
    }
}
