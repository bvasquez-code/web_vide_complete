package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.dto.VideoWatchProgressDto;
import com.ccadmin.app.video.model.dto.VideoRegisterDto;
import com.ccadmin.app.video.model.entity.*;
import com.ccadmin.app.video.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class VideoCreateService extends SessionService {
    private static final Logger log = LogManager.getLogger(VideoCreateService.class);

    private final VideoRepository videoRepository;
    private final VideoCategoryRelRepository categoryRelRepository;
    private final VideoActorRelRepository actorRelRepository;
    private final VideoTagRelRepository tagRelRepository;
    private final VideoViewLogRepository viewLogRepository;
    private final CodGeneratorService codGeneratorService;
    private final VideoMetadataProcessService metadataProcessService;
    private final VideoPathService videoPathService;

    public VideoCreateService(VideoRepository videoRepository, VideoCategoryRelRepository categoryRelRepository, VideoActorRelRepository actorRelRepository, VideoTagRelRepository tagRelRepository, VideoViewLogRepository viewLogRepository, CodGeneratorService codGeneratorService, VideoMetadataProcessService metadataProcessService, VideoPathService videoPathService) {
        this.videoRepository = videoRepository;
        this.categoryRelRepository = categoryRelRepository;
        this.actorRelRepository = actorRelRepository;
        this.tagRelRepository = tagRelRepository;
        this.viewLogRepository = viewLogRepository;
        this.codGeneratorService = codGeneratorService;
        this.metadataProcessService = metadataProcessService;
        this.videoPathService = videoPathService;
    }

    @Transactional
    public VideoEntity save(VideoRegisterDto dto) {
        String videoCod = dto != null && dto.Video != null ? dto.Video.VideoCod : "";
        boolean newVideo = videoCod == null || videoCod.isBlank();
        log.info("Iniciando guardado de video. videoCod={}, nuevo={}", videoCod, newVideo);
        if (dto == null || dto.Video == null) {
            throw new IllegalArgumentException("Datos del video obligatorios.");
        }
        if (dto.CategoryCodList == null || dto.CategoryCodList.isEmpty()) {
            throw new IllegalArgumentException("Debe asociar al menos una categoria.");
        }
        VideoEntity video = dto.Video.validate();
        if (video.VideoCod == null || video.VideoCod.isBlank()) {
            video.VideoCod = codGeneratorService.next("VID");
            video.addSessionCreate(getUserCod());
        } else {
            video.addSessionModify(getUserCod());
            categoryRelRepository.deleteByVideoCod(video.VideoCod);
            actorRelRepository.deleteByVideoCod(video.VideoCod);
            tagRelRepository.deleteByVideoCod(video.VideoCod);
        }
        metadataProcessService.applyFileMetadataIfAvailable(video);
        VideoEntity saved = videoRepository.save(video);
        saveCategoryRels(saved.VideoCod, dto.CategoryCodList);
        saveActorRels(saved.VideoCod, dto.ActorCodList);
        saveTagRels(saved.VideoCod, dto.TagCodList);
        log.info("Video guardado correctamente. videoCod={}, nuevo={}", saved.VideoCod, newVideo);
        return saved;
    }

    @Transactional
    public VideoEntity enable(String cod) {
        log.info("Iniciando activacion de video. videoCod={}", cod);
        VideoEntity video = videoRepository.findById(cod).orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
        video.active(getUserCod());
        VideoEntity saved = videoRepository.save(video);
        log.info("Video activado correctamente. videoCod={}", saved.VideoCod);
        return saved;
    }

    @Transactional
    public VideoEntity disable(String cod) {
        log.info("Iniciando desactivacion de video. videoCod={}", cod);
        VideoEntity video = videoRepository.findById(cod).orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
        video.inactive(getUserCod());
        VideoEntity saved = videoRepository.save(video);
        log.info("Video desactivado correctamente. videoCod={}", saved.VideoCod);
        return saved;
    }

    @Transactional
    public VideoEntity renamePathFile(String videoCod, String newFileName) throws Exception {
        log.info("Iniciando renombrado de archivo de video. videoCod={}, newFileName={}", videoCod, newFileName);
        if (videoCod == null || videoCod.isBlank()) {
            throw new IllegalArgumentException("Codigo de video obligatorio.");
        }
        if (newFileName == null || newFileName.isBlank()) {
            throw new IllegalArgumentException("Nombre de archivo obligatorio.");
        }
        VideoEntity video = videoRepository.findById(videoCod)
                .orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
        if (!"PATH".equals(video.SourceType)) {
            throw new IllegalArgumentException("Solo se puede renombrar el archivo de videos con origen PATH.");
        }
        if (video.SourceValue == null || video.SourceValue.isBlank()) {
            throw new IllegalArgumentException("El video no tiene ruta de archivo registrada.");
        }

        Path source = videoPathService.resolveSourcePath(video.SourceValue);
        if (!Files.exists(source) || !Files.isRegularFile(source) || !Files.isReadable(source)) {
            throw new IllegalArgumentException("El archivo actual no existe o no tiene permiso de lectura.");
        }
        Path parent = source.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("La ruta actual del archivo no tiene carpeta padre valida.");
        }

        String safeFileName = normalizeNewFileName(newFileName, source);
        Path target = parent.resolve(safeFileName).normalize();
        if (!target.startsWith(parent.normalize())) {
            throw new IllegalArgumentException("Nombre de archivo invalido.");
        }
        if (source.equals(target)) {
            video.SourceValue = videoPathService.renamedSourceValue(video.SourceValue, target);
            video.addSessionModify(getUserCod());
            VideoEntity saved = videoRepository.save(video);
            log.info("Archivo de video sin cambios de nombre, referencia actualizada. videoCod={}, sourceValue={}", saved.VideoCod, saved.SourceValue);
            return saved;
        }
        if (Files.exists(target)) {
            throw new IllegalArgumentException("Ya existe un archivo con ese nombre.");
        }

        Files.move(source, target);
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new IllegalStateException("No se pudo validar el archivo con el nuevo nombre.");
        }
        if (Files.exists(source)) {
            throw new IllegalStateException("El archivo anterior todavia existe. No se actualizo la base de datos.");
        }

        video.SourceValue = videoPathService.renamedSourceValue(video.SourceValue, target);
        video.addSessionModify(getUserCod());
        VideoEntity saved = videoRepository.save(video);
        log.info("Archivo de video renombrado correctamente. videoCod={}, sourceValue={}", saved.VideoCod, saved.SourceValue);
        return saved;
    }

    @Transactional
    public VideoViewLogEntity registerView(String videoCod, HttpServletRequest request) {
        if (!videoRepository.existsById(videoCod)) {
            throw new IllegalArgumentException("Video no encontrado.");
        }
        videoRepository.incrementView(videoCod);
        VideoViewLogEntity log = new VideoViewLogEntity();
        log.VideoCod = videoCod;
        String userCod = getUserCod();
        log.ViewerType = resolveViewerType(userCod);
        log.ViewerUserCod = "PUBLIC".equals(log.ViewerType) ? null : userCod;
        log.PlayerContext = resolvePlayerContext(null);
        log.WatchSeconds = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        log.Completed = "N";
        log.ViewerIp = request.getRemoteAddr();
        log.UserAgent = request.getHeader("User-Agent");
        log.addSessionCreate(log.ViewerUserCod == null ? "PUBLIC" : log.ViewerUserCod);
        return viewLogRepository.save(log);
    }

    @Transactional
    public VideoViewLogEntity registerWatchProgress(String videoCod, VideoWatchProgressDto dto, HttpServletRequest request) {
        if (!videoRepository.existsById(videoCod)) {
            throw new IllegalArgumentException("Video no encontrado.");
        }
        VideoWatchProgressDto safeDto = dto == null ? new VideoWatchProgressDto() : dto;
        VideoViewLogEntity log = resolveProgressLog(videoCod, safeDto, request);
        double playedSeconds = safeNumber(safeDto.PlayedSeconds);
        if (playedSeconds > 0) {
            BigDecimal current = log.WatchSeconds == null ? BigDecimal.ZERO : log.WatchSeconds;
            log.WatchSeconds = current.add(BigDecimal.valueOf(Math.min(playedSeconds, 300))).setScale(3, RoundingMode.HALF_UP);
        }
        if (safeDto.CurrentSecond != null && Double.isFinite(safeDto.CurrentSecond) && safeDto.CurrentSecond >= 0) {
            log.LastPositionSecond = BigDecimal.valueOf(safeDto.CurrentSecond).setScale(3, RoundingMode.HALF_UP);
        }
        if (safeDto.DurationSeconds != null && Double.isFinite(safeDto.DurationSeconds) && safeDto.DurationSeconds > 0) {
            log.DurationSeconds = BigDecimal.valueOf(safeDto.DurationSeconds).setScale(3, RoundingMode.HALF_UP);
        }
        if (Boolean.TRUE.equals(safeDto.Completed)) {
            log.Completed = "Y";
        }
        log.addSessionModify(log.ViewerUserCod == null ? "PUBLIC" : log.ViewerUserCod);
        return viewLogRepository.save(log);
    }

    private VideoViewLogEntity resolveProgressLog(String videoCod, VideoWatchProgressDto dto, HttpServletRequest request) {
        if (dto.ViewLogId != null) {
            VideoViewLogEntity existing = viewLogRepository.findById(dto.ViewLogId)
                    .orElseThrow(() -> new IllegalArgumentException("Registro de vista no encontrado."));
            if (!videoCod.equals(existing.VideoCod)) {
                throw new IllegalArgumentException("El registro de vista no corresponde al video.");
            }
            return existing;
        }
        VideoViewLogEntity log = new VideoViewLogEntity();
        log.VideoCod = videoCod;
        String userCod = getUserCod();
        log.ViewerType = resolveViewerType(userCod);
        log.ViewerUserCod = "PUBLIC".equals(log.ViewerType) ? null : userCod;
        log.PlayerContext = resolvePlayerContext(dto.PlayerContext);
        log.WatchSeconds = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        log.Completed = "N";
        log.ViewerIp = request.getRemoteAddr();
        log.UserAgent = request.getHeader("User-Agent");
        log.addSessionCreate(log.ViewerUserCod == null ? "PUBLIC" : log.ViewerUserCod);
        videoRepository.incrementView(videoCod);
        return log;
    }

    private String resolveViewerType(String userCod) {
        if (userCod == null || userCod.isBlank() || "anonymousUser".equals(userCod) || "SISTEMA".equals(userCod)) {
            return "PUBLIC";
        }
        return userCod.startsWith("SUB") ? "VIEWER" : "ADMIN";
    }

    private String resolvePlayerContext(String playerContext) {
        if (playerContext == null || playerContext.isBlank()) {
            return "PUBLIC_PLAYER";
        }
        String safeContext = playerContext.trim().toUpperCase();
        return safeContext.length() > 32 ? safeContext.substring(0, 32) : safeContext;
    }

    private double safeNumber(Double value) {
        if (value == null || !Double.isFinite(value) || value < 0) {
            return 0;
        }
        return value;
    }

    private void saveCategoryRels(String videoCod, List<String> categoryCods) {
        for (int i = 0; i < categoryCods.size(); i++) {
            VideoCategoryRelEntity rel = new VideoCategoryRelEntity();
            rel.VideoCod = videoCod;
            rel.CategoryCod = categoryCods.get(i);
            rel.IsPrimary = i == 0 ? "Y" : "N";
            rel.addSessionCreate(getUserCod());
            categoryRelRepository.save(rel);
        }
    }

    private void saveActorRels(String videoCod, List<String> actorCods) {
        if (actorCods == null) return;
        for (String actorCod : actorCods) {
            VideoActorRelEntity rel = new VideoActorRelEntity();
            rel.VideoCod = videoCod;
            rel.ActorCod = actorCod;
            rel.addSessionCreate(getUserCod());
            actorRelRepository.save(rel);
        }
    }

    private void saveTagRels(String videoCod, List<String> tagCods) {
        if (tagCods == null) return;
        for (String tagCod : tagCods) {
            VideoTagRelEntity rel = new VideoTagRelEntity();
            rel.VideoCod = videoCod;
            rel.TagCod = tagCod;
            rel.addSessionCreate(getUserCod());
            tagRelRepository.save(rel);
        }
    }

    private String normalizeNewFileName(String newFileName, Path source) {
        String trimmed = newFileName.trim();
        if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains(":")) {
            throw new IllegalArgumentException("Ingrese solo el nombre del archivo, no una ruta.");
        }
        if (trimmed.matches(".*[<>\"|?*].*")) {
            throw new IllegalArgumentException("El nombre contiene caracteres invalidos.");
        }
        if (trimmed.isBlank() || ".".equals(trimmed) || "..".equals(trimmed) || trimmed.endsWith(".")) {
            throw new IllegalArgumentException("Nombre de archivo invalido.");
        }
        String extension = resolveExtension(source.getFileName().toString());
        if (!extension.isBlank() && !trimmed.toLowerCase().endsWith(extension.toLowerCase())) {
            trimmed = trimmed + extension;
        }
        return trimmed;
    }

    private String resolveExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index);
    }
}
