package com.ccadmin.app.video.controller;

import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import com.ccadmin.app.subscriber.model.dto.VideoCaptureSuggestionDto;
import com.ccadmin.app.video.model.dto.VideoRegisterDto;
import com.ccadmin.app.video.model.dto.VideoWatchProgressDto;
import com.ccadmin.app.video.service.ThumbnailStorageService;
import com.ccadmin.app.video.service.VideoCreateService;
import com.ccadmin.app.video.service.VideoCaptureService;
import com.ccadmin.app.video.service.VideoMetadataProcessService;
import com.ccadmin.app.video.service.VideoSearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/admin/videos")
public class AdminVideoController {
    private final VideoSearchService searchService;
    private final VideoCreateService createService;
    private final ThumbnailStorageService thumbnailStorageService;
    private final VideoMetadataProcessService metadataProcessService;
    private final VideoCaptureService videoCaptureService;

    public AdminVideoController(VideoSearchService searchService, VideoCreateService createService, ThumbnailStorageService thumbnailStorageService, VideoMetadataProcessService metadataProcessService, VideoCaptureService videoCaptureService) {
        this.searchService = searchService;
        this.createService = createService;
        this.thumbnailStorageService = thumbnailStorageService;
        this.metadataProcessService = metadataProcessService;
        this.videoCaptureService = videoCaptureService;
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

    @PostMapping("uploadThumbnail")
    public ResponseEntity<ResponseWsDto> uploadThumbnail(@RequestParam String VideoCod, @RequestParam("File") MultipartFile file, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(thumbnailStorageService.saveThumbnail(VideoCod, file, request)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("uploadCapture")
    public ResponseEntity<ResponseWsDto> uploadCapture(@RequestParam String VideoCod, @RequestParam("File") MultipartFile file, HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.saveManualCapture(VideoCod, file, baseUrl)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("captureAtSecond")
    public ResponseEntity<ResponseWsDto> captureAtSecond(@RequestParam String VideoCod, @RequestParam Double CaptureSecond, HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.saveCaptureAtSecond(VideoCod, CaptureSecond, baseUrl)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("processMetadata")
    public ResponseEntity<ResponseWsDto> processMetadata(@RequestParam Double Percentage, @RequestParam(defaultValue = "") String VideoCod, @RequestParam(defaultValue = "MISSING_THUMBNAIL") String Mode, @RequestParam(defaultValue = "false") Boolean Overwrite, HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            return new ResponseEntity<>(new ResponseWsDto(metadataProcessService.process(Percentage, VideoCod, Mode, Overwrite, baseUrl)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("generateCaptures")
    public ResponseEntity<ResponseWsDto> generateCaptures(@RequestParam String VideoCod, HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.generate(VideoCod, baseUrl)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("cleanUnlinkedCaptures")
    public ResponseEntity<ResponseWsDto> cleanUnlinkedCaptures(@RequestParam(defaultValue = "false") Boolean DryRun) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.cleanUnlinkedCaptures(DryRun)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("{videoCod}/watchProgress")
    public ResponseEntity<ResponseWsDto> watchProgress(@PathVariable String videoCod, @RequestBody VideoWatchProgressDto dto, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(createService.registerWatchProgress(videoCod, dto, request)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("captureSuggestions")
    public ResponseEntity<ResponseWsDto> captureSuggestions() {
        try {
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.findPendingSuggestions()), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("captureSuggestions/{suggestionId}/approve")
    public ResponseEntity<ResponseWsDto> approveCaptureSuggestion(@PathVariable Long suggestionId, @RequestBody(required = false) VideoCaptureSuggestionDto dto) {
        try {
            String comment = dto == null ? "" : dto.ReviewComment;
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.approveSuggestion(suggestionId, comment)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("captureSuggestions/{suggestionId}/reject")
    public ResponseEntity<ResponseWsDto> rejectCaptureSuggestion(@PathVariable Long suggestionId, @RequestBody(required = false) VideoCaptureSuggestionDto dto) {
        try {
            String comment = dto == null ? "" : dto.ReviewComment;
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.rejectSuggestion(suggestionId, comment)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }
}
