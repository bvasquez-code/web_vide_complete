package com.ccadmin.app.video.controller;

import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import com.ccadmin.app.video.model.entity.ActorEntity;
import com.ccadmin.app.video.model.entity.TagEntity;
import com.ccadmin.app.video.model.entity.VideoCategoryEntity;
import com.ccadmin.app.video.service.VideoCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin")
public class AdminCatalogController {
    private final VideoCatalogService catalogService;

    public AdminCatalogController(VideoCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("categories/findAll")
    public ResponseEntity<ResponseWsDto> categories(@RequestParam(defaultValue = "") String Query, @RequestParam(defaultValue = "") String Status, @RequestParam(defaultValue = "1") Integer Page, @RequestParam(defaultValue = "10") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(catalogService.findCategories(Query, Status, Page, Limit)), HttpStatus.OK);
    }

    @PostMapping("categories/save")
    public ResponseEntity<ResponseWsDto> saveCategory(@RequestBody VideoCategoryEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.saveCategory(entity)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }

    @PostMapping("categories/enable")
    public ResponseEntity<ResponseWsDto> enableCategory(@RequestBody VideoCategoryEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.enableCategory(entity.CategoryCod)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }

    @PostMapping("categories/disable")
    public ResponseEntity<ResponseWsDto> disableCategory(@RequestBody VideoCategoryEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.disableCategory(entity.CategoryCod)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }

    @GetMapping("actors/findAll")
    public ResponseEntity<ResponseWsDto> actors(@RequestParam(defaultValue = "") String Query, @RequestParam(defaultValue = "") String Status, @RequestParam(defaultValue = "1") Integer Page, @RequestParam(defaultValue = "10") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(catalogService.findActors(Query, Status, Page, Limit)), HttpStatus.OK);
    }

    @PostMapping("actors/save")
    public ResponseEntity<ResponseWsDto> saveActor(@RequestBody ActorEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.saveActor(entity)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }

    @PostMapping("actors/enable")
    public ResponseEntity<ResponseWsDto> enableActor(@RequestBody ActorEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.enableActor(entity.ActorCod)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }

    @PostMapping("actors/disable")
    public ResponseEntity<ResponseWsDto> disableActor(@RequestBody ActorEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.disableActor(entity.ActorCod)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }

    @GetMapping("tags/findAll")
    public ResponseEntity<ResponseWsDto> tags(@RequestParam(defaultValue = "") String Query, @RequestParam(defaultValue = "") String Status, @RequestParam(defaultValue = "1") Integer Page, @RequestParam(defaultValue = "10") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(catalogService.findTags(Query, Status, Page, Limit)), HttpStatus.OK);
    }

    @PostMapping("tags/save")
    public ResponseEntity<ResponseWsDto> saveTag(@RequestBody TagEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.saveTag(entity)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }

    @PostMapping("tags/enable")
    public ResponseEntity<ResponseWsDto> enableTag(@RequestBody TagEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.enableTag(entity.TagCod)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }

    @PostMapping("tags/disable")
    public ResponseEntity<ResponseWsDto> disableTag(@RequestBody TagEntity entity) {
        try { return new ResponseEntity<>(new ResponseWsDto(catalogService.disableTag(entity.TagCod)), HttpStatus.OK); }
        catch (Exception ex) { return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST); }
    }
}
