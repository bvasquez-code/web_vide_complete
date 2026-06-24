package com.ccadmin.app.subscriber.controller;

import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import com.ccadmin.app.subscriber.model.dto.VideoCaptureSuggestionDto;
import com.ccadmin.app.subscriber.model.dto.VideoInteractionDto;
import com.ccadmin.app.subscriber.service.SubscriberVideoInteractionService;
import com.ccadmin.app.video.service.VideoCaptureService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/public/me")
public class SubscriberVideoController {
    private final SubscriberVideoInteractionService interactionService;
    private final VideoCaptureService videoCaptureService;

    public SubscriberVideoController(SubscriberVideoInteractionService interactionService, VideoCaptureService videoCaptureService) {
        this.interactionService = interactionService;
        this.videoCaptureService = videoCaptureService;
    }

    @GetMapping("videos/{videoCod}/state")
    public ResponseEntity<ResponseWsDto> state(@PathVariable String videoCod) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(interactionService.state(videoCod)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("videos/reaction")
    public ResponseEntity<ResponseWsDto> reaction(@RequestBody VideoInteractionDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(interactionService.react(dto)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("videos/rating")
    public ResponseEntity<ResponseWsDto> rating(@RequestBody VideoInteractionDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(interactionService.rate(dto)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("videos/watchLater")
    public ResponseEntity<ResponseWsDto> watchLater(@RequestBody VideoInteractionDto dto) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(interactionService.watchLater(dto)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("videos/captureSuggestion")
    public ResponseEntity<ResponseWsDto> captureSuggestion(@RequestBody VideoCaptureSuggestionDto dto, HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.suggestCaptureAtSecond(dto.VideoCod, dto.CaptureSecond, dto.Comment, baseUrl)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("watchLater")
    public ResponseEntity<ResponseWsDto> watchLater(@RequestParam(defaultValue = "30") Integer Limit) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(interactionService.watchLaterVideos(Limit)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("history")
    public ResponseEntity<ResponseWsDto> history(@RequestParam(defaultValue = "30") Integer Limit) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(interactionService.historyVideos(Limit)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("liked")
    public ResponseEntity<ResponseWsDto> liked(@RequestParam(defaultValue = "30") Integer Limit) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(interactionService.likedVideos(Limit)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.UNAUTHORIZED);
        }
    }
}
