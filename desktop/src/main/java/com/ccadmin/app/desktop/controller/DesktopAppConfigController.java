package com.ccadmin.app.desktop.controller;

import com.ccadmin.app.desktop.service.DesktopAppConfigService;
import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class DesktopAppConfigController {
    private final DesktopAppConfigService service;

    public DesktopAppConfigController(DesktopAppConfigService service) {
        this.service = service;
    }

    public ResponseEntity<ResponseWsDto> getUploadRoot() {
        try {
            return new ResponseEntity<>(new ResponseWsDto(service.getUploadRoot()), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<ResponseWsDto> saveUploadRoot(String uploadRoot) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(service.saveUploadRoot(uploadRoot)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }
}
