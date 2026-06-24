package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.dto.VideoRegisterDto;
import com.ccadmin.app.video.model.entity.*;
import com.ccadmin.app.video.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VideoCreateService extends SessionService {
    private final VideoRepository videoRepository;
    private final VideoCategoryRelRepository categoryRelRepository;
    private final VideoActorRelRepository actorRelRepository;
    private final VideoTagRelRepository tagRelRepository;
    private final VideoViewLogRepository viewLogRepository;
    private final CodGeneratorService codGeneratorService;

    public VideoCreateService(VideoRepository videoRepository, VideoCategoryRelRepository categoryRelRepository, VideoActorRelRepository actorRelRepository, VideoTagRelRepository tagRelRepository, VideoViewLogRepository viewLogRepository, CodGeneratorService codGeneratorService) {
        this.videoRepository = videoRepository;
        this.categoryRelRepository = categoryRelRepository;
        this.actorRelRepository = actorRelRepository;
        this.tagRelRepository = tagRelRepository;
        this.viewLogRepository = viewLogRepository;
        this.codGeneratorService = codGeneratorService;
    }

    @Transactional
    public VideoEntity save(VideoRegisterDto dto) {
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
        VideoEntity saved = videoRepository.save(video);
        saveCategoryRels(saved.VideoCod, dto.CategoryCodList);
        saveActorRels(saved.VideoCod, dto.ActorCodList);
        saveTagRels(saved.VideoCod, dto.TagCodList);
        return saved;
    }

    @Transactional
    public VideoEntity enable(String cod) {
        VideoEntity video = videoRepository.findById(cod).orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
        video.active(getUserCod());
        return videoRepository.save(video);
    }

    @Transactional
    public VideoEntity disable(String cod) {
        VideoEntity video = videoRepository.findById(cod).orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
        video.inactive(getUserCod());
        return videoRepository.save(video);
    }

    @Transactional
    public void registerView(String videoCod, HttpServletRequest request) {
        if (!videoRepository.existsById(videoCod)) {
            throw new IllegalArgumentException("Video no encontrado.");
        }
        videoRepository.incrementView(videoCod);
        VideoViewLogEntity log = new VideoViewLogEntity();
        log.VideoCod = videoCod;
        String userCod = getUserCod();
        log.ViewerUserCod = userCod != null && userCod.startsWith("SUB") ? userCod : null;
        log.ViewerIp = request.getRemoteAddr();
        log.UserAgent = request.getHeader("User-Agent");
        log.addSessionCreate(log.ViewerUserCod == null ? "PUBLIC" : log.ViewerUserCod);
        viewLogRepository.save(log);
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
}
