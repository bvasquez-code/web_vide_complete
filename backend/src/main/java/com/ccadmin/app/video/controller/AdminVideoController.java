package com.ccadmin.app.video.controller;

import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import com.ccadmin.app.video.model.dto.VideoRegisterDto;
import com.ccadmin.app.video.service.VideoCreateService;
import com.ccadmin.app.video.service.VideoSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin/videos")
public class AdminVideoController {
    private final VideoSearchService searchService;
    private final VideoCreateService createService;

    public AdminVideoController(VideoSearchService searchService, VideoCreateService createService) {
        this.searchService = searchService;
        this.createService = createService;
    }

    @GetMapping("findAll")
    public ResponseEntity<ResponseWsDto> findAll(@RequestParam(defaultValue = "") String Query, @RequestParam(defaultValue = "") String Status, @RequestParam(defaultValue = "") String SourceType, @RequestParam(defaultValue = "") String CategoryCod, @RequestParam(defaultValue = "") String ActorCod, @RequestParam(defaultValue = "") String TagCod, @RequestParam(defaultValue = "1") Integer Page, @RequestParam(defaultValue = "10") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(searchService.findAll(Query, Status, SourceType, CategoryCod, ActorCod, TagCod, Page, Limit)), HttpStatus.OK);
    }

    @GetMapping("findById")
    public ResponseEntity<ResponseWsDto> findById(@RequestParam String VideoCod) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(searchService.findDetail(VideoCod)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("findDataForm")
    public ResponseEntity<ResponseWsDto> findDataForm() {
        return new ResponseEntity<>(new ResponseWsDto(searchService.findDataForm()), HttpStatus.OK);
    }

    @PostMapping("save")
    public ResponseEntity<ResponseWsDto> save(@RequestBody VideoRegisterDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(createService.save(dto)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("enable")
    public ResponseEntity<ResponseWsDto> enable(@RequestBody VideoRegisterDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(createService.enable(dto.Video.VideoCod)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("disable")
    public ResponseEntity<ResponseWsDto> disable(@RequestBody VideoRegisterDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(createService.disable(dto.Video.VideoCod)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }
}
