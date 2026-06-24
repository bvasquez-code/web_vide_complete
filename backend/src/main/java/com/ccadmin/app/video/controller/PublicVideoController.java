package com.ccadmin.app.video.controller;

import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import com.ccadmin.app.video.model.dto.VideoWatchProgressDto;
import com.ccadmin.app.video.model.entity.VideoEntity;
import com.ccadmin.app.video.service.ThumbnailStorageService;
import com.ccadmin.app.video.service.VideoCaptureService;
import com.ccadmin.app.video.service.VideoCatalogService;
import com.ccadmin.app.video.service.VideoCreateService;
import com.ccadmin.app.video.service.VideoSearchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/public")
public class PublicVideoController {
    private final VideoCatalogService catalogService;
    private final VideoSearchService searchService;
    private final VideoCreateService createService;
    private final ThumbnailStorageService thumbnailStorageService;
    private final VideoCaptureService videoCaptureService;

    public PublicVideoController(VideoCatalogService catalogService, VideoSearchService searchService, VideoCreateService createService, ThumbnailStorageService thumbnailStorageService, VideoCaptureService videoCaptureService) {
        this.catalogService = catalogService;
        this.searchService = searchService;
        this.createService = createService;
        this.thumbnailStorageService = thumbnailStorageService;
        this.videoCaptureService = videoCaptureService;
    }

    @GetMapping("categories")
    public ResponseEntity<ResponseWsDto> categories() {
        return new ResponseEntity<>(new ResponseWsDto(catalogService.findActiveCategories()), HttpStatus.OK);
    }

    @GetMapping("categories/top")
    public ResponseEntity<ResponseWsDto> topCategories(@RequestParam(defaultValue = "8") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(catalogService.findTopViewedCategories(Limit)), HttpStatus.OK);
    }

    @GetMapping("videos/recent")
    public ResponseEntity<ResponseWsDto> recent(@RequestParam(defaultValue = "12") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(searchService.findRecent(Limit)), HttpStatus.OK);
    }

    @GetMapping("videos/mostViewed")
    public ResponseEntity<ResponseWsDto> mostViewed(@RequestParam(defaultValue = "12") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(searchService.findMostViewed(Limit)), HttpStatus.OK);
    }

    @GetMapping("videos/search")
    public ResponseEntity<ResponseWsDto> search(@RequestParam(defaultValue = "") String Query, @RequestParam(defaultValue = "recent") String Sort, @RequestParam(defaultValue = "1") Integer Page, @RequestParam(defaultValue = "30") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(searchService.searchPublic(Query, Sort, Page, Limit)), HttpStatus.OK);
    }

    @GetMapping("categories/{categoryCod}/videos")
    public ResponseEntity<ResponseWsDto> byCategory(@PathVariable String categoryCod, @RequestParam(defaultValue = "recent") String Sort, @RequestParam(defaultValue = "1") Integer Page, @RequestParam(defaultValue = "24") Integer Limit) {
        ResponseWsDto response = new ResponseWsDto(searchService.findByCategory(categoryCod, Sort, Page, Limit));
        response.AddResponseAdditional("Category", searchService.findCategoryById(categoryCod));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("actors/{actorCod}/videos")
    public ResponseEntity<ResponseWsDto> byActor(@PathVariable String actorCod, @RequestParam(defaultValue = "recent") String Sort, @RequestParam(defaultValue = "1") Integer Page, @RequestParam(defaultValue = "24") Integer Limit) {
        ResponseWsDto response = new ResponseWsDto(searchService.findByActor(actorCod, Sort, Page, Limit));
        response.AddResponseAdditional("Actor", searchService.findActorById(actorCod));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("videos/{videoCod}")
    public ResponseEntity<ResponseWsDto> detail(@PathVariable String videoCod) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(searchService.findDetail(videoCod)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("videos/{videoCod}/ensureCaptures")
    public ResponseEntity<ResponseWsDto> ensureCaptures(@PathVariable String videoCod, HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            return new ResponseEntity<>(new ResponseWsDto(videoCaptureService.requestAutomaticCaptures(videoCod, baseUrl)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("videos/{videoCod}/related")
    public ResponseEntity<ResponseWsDto> related(@PathVariable String videoCod, @RequestParam(defaultValue = "8") Integer Limit) {
        return new ResponseEntity<>(new ResponseWsDto(searchService.findRelated(videoCod, Limit)), HttpStatus.OK);
    }

    @GetMapping("videos/{videoCod}/stream")
    public void stream(@PathVariable String videoCod, @RequestHeader HttpHeaders headers, HttpServletResponse response) {
        try {
            VideoEntity video = searchService.findEntity(videoCod);
            if (!"PATH".equals(video.SourceType)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El video no es de tipo PATH.");
                return;
            }
            Path path = Path.of(video.SourceValue).normalize();
            if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado o sin permiso de lectura.");
                return;
            }

            long contentLength = Files.size(path);
            Optional<RangeValue> range = parseRange(headers.getFirst(HttpHeaders.RANGE), contentLength);
            RangeValue value = range.orElse(new RangeValue(0, contentLength - 1, contentLength));

            response.reset();
            response.setBufferSize(64 * 1024);
            response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Length, Content-Range, Accept-Ranges");
            response.setContentType(resolveContentType(path));

            if (range.isPresent()) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + value.Start + "-" + value.End + "/" + contentLength);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
            }

            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(value.Length));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"");
            copyRange(path, response.getOutputStream(), value);
        } catch (Exception ex) {
            try {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                }
            } catch (Exception ignored) {
            }
        }
    }

    @GetMapping("thumbnails/{fileName}")
    public void thumbnail(@PathVariable String fileName, HttpServletResponse response) {
        try {
            Path path = thumbnailStorageService.findThumbnail(fileName);
            if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Miniatura no encontrada.");
                return;
            }
            response.setContentType(resolveContentType(path));
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=86400");
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(path)));
            try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
                inputStream.transferTo(response.getOutputStream());
            }
        } catch (Exception ex) {
            try {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                }
            } catch (Exception ignored) {
            }
        }
    }

    @GetMapping("captures/{folderName}/{fileName}")
    public void capture(@PathVariable String folderName, @PathVariable String fileName, HttpServletResponse response) {
        try {
            Path path = videoCaptureService.findCapture(folderName + "/" + fileName);
            if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Captura no encontrada.");
                return;
            }
            response.setContentType(resolveContentType(path));
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=86400");
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(path)));
            try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
                inputStream.transferTo(response.getOutputStream());
            }
        } catch (Exception ex) {
            try {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Optional<RangeValue> parseRange(String rangeHeader, long contentLength) {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            return Optional.empty();
        }
        String[] parts = rangeHeader.substring(6).split("-", 2);
        long start = parts[0].isBlank() ? 0 : Long.parseLong(parts[0]);
        long end = parts.length > 1 && !parts[1].isBlank() ? Long.parseLong(parts[1]) : contentLength - 1;
        end = Math.min(end, contentLength - 1);
        if (start < 0 || start > end) {
            return Optional.empty();
        }
        return Optional.of(new RangeValue(start, end, end - start + 1));
    }

    private record RangeValue(long Start, long End, long Length) {}

    private String resolveContentType(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (fileName.endsWith(".webm")) {
            return "video/webm";
        }
        if (fileName.endsWith(".ogg") || fileName.endsWith(".ogv")) {
            return "video/ogg";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    private void copyRange(Path path, OutputStream outputStream, RangeValue range) throws Exception {
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            long skipped = inputStream.skip(range.Start);
            while (skipped < range.Start) {
                long current = inputStream.skip(range.Start - skipped);
                if (current <= 0) {
                    break;
                }
                skipped += current;
            }
            byte[] buffer = new byte[64 * 1024];
            long remaining = range.Length;
            while (remaining > 0) {
                int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1) {
                    break;
                }
                outputStream.write(buffer, 0, read);
                remaining -= read;
            }
            outputStream.flush();
        }
    }

    @PostMapping("videos/{videoCod}/view")
    public ResponseEntity<ResponseWsDto> view(@PathVariable String videoCod, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(createService.registerView(videoCod, request)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("videos/{videoCod}/watchProgress")
    public ResponseEntity<ResponseWsDto> watchProgress(@PathVariable String videoCod, @RequestBody VideoWatchProgressDto dto, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(new ResponseWsDto(createService.registerWatchProgress(videoCod, dto, request)), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseWsDto(ex), HttpStatus.BAD_REQUEST);
        }
    }
}
