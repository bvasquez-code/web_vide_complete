package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.model.dto.ResponsePageSearchT;
import com.ccadmin.app.video.model.dto.VideoCardDto;
import com.ccadmin.app.video.model.dto.VideoDetailDto;
import com.ccadmin.app.video.model.dto.VideoLabelDto;
import com.ccadmin.app.video.model.entity.*;
import com.ccadmin.app.video.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VideoSearchService {
    private final VideoRepository videoRepository;
    private final VideoCategoryRepository categoryRepository;
    private final ActorRepository actorRepository;
    private final TagRepository tagRepository;
    private final VideoCategoryRelRepository categoryRelRepository;
    private final VideoActorRelRepository actorRelRepository;
    private final VideoTagRelRepository tagRelRepository;
    private final VideoCaptureRepository captureRepository;

    public VideoSearchService(VideoRepository videoRepository, VideoCategoryRepository categoryRepository, ActorRepository actorRepository, TagRepository tagRepository, VideoCategoryRelRepository categoryRelRepository, VideoActorRelRepository actorRelRepository, VideoTagRelRepository tagRelRepository, VideoCaptureRepository captureRepository) {
        this.videoRepository = videoRepository;
        this.categoryRepository = categoryRepository;
        this.actorRepository = actorRepository;
        this.tagRepository = tagRepository;
        this.categoryRelRepository = categoryRelRepository;
        this.actorRelRepository = actorRelRepository;
        this.tagRelRepository = tagRelRepository;
        this.captureRepository = captureRepository;
    }

    public List<VideoCardDto> findRecent(Integer limit) { return videoRepository.findRecent(safeLimit(limit)).stream().map(this::toCard).toList(); }
    public List<VideoCardDto> findMostViewed(Integer limit) { return videoRepository.findMostViewed(safeLimit(limit)).stream().map(this::toCard).toList(); }
    public List<VideoCardDto> findRelated(String videoCod, Integer limit) { return videoRepository.findRelated(videoCod, safeLimit(limit)).stream().map(this::toCard).toList(); }

    public ResponsePageSearchT<VideoCardDto> searchPublic(String query, String sort, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 30 : Math.min(limit, 30);
        String q = query == null ? "" : query.trim();
        String safeSort = "views".equals(sort) ? "views" : "recent";
        return new ResponsePageSearchT<>(
                videoRepository.searchPublic(q, safeSort, (safePage - 1) * safeLimit, safeLimit).stream().map(this::toCard).toList(),
                videoRepository.countSearchPublic(q),
                safePage,
                safeLimit
        );
    }

    public ResponsePageSearchT<VideoCardDto> findByCategory(String categoryCod, String sort, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 24 : Math.min(limit, 50);
        String safeSort = "views".equals(sort) ? "views" : "recent";
        return new ResponsePageSearchT<>(
                videoRepository.findByCategory(categoryCod, safeSort, (safePage - 1) * safeLimit, safeLimit).stream().map(this::toCard).toList(),
                videoRepository.countByCategory(categoryCod),
                safePage,
                safeLimit
        );
    }

    public ResponsePageSearchT<VideoCardDto> findByActor(String actorCod, String sort, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 24 : Math.min(limit, 50);
        String safeSort = "views".equals(sort) ? "views" : "recent";
        return new ResponsePageSearchT<>(
                videoRepository.findByActor(actorCod, safeSort, (safePage - 1) * safeLimit, safeLimit).stream().map(this::toCard).toList(),
                videoRepository.countByActor(actorCod),
                safePage,
                safeLimit
        );
    }

    public ResponsePageSearchT<VideoEntity> findAll(String query, String status, String sourceType, String categoryCod, String actorCod, String tagCod, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 10 : limit;
        String q = query == null ? "" : query;
        String s = status == null ? "" : status;
        String st = sourceType == null ? "" : sourceType;
        String c = categoryCod == null ? "" : categoryCod;
        String a = actorCod == null ? "" : actorCod;
        String t = tagCod == null ? "" : tagCod;
        return new ResponsePageSearchT<>(videoRepository.findByFilters(q, s, st, c, a, t, (safePage - 1) * safeLimit, safeLimit), videoRepository.countByFilters(q, s, st, c, a, t), safePage, safeLimit);
    }

    public VideoDetailDto findDetail(String videoCod) {
        VideoEntity video = videoRepository.findById(videoCod).orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
        List<String> categoryCods = categoryRelRepository.findByVideoCodAndStatus(videoCod, "A").stream().map(r -> r.CategoryCod).toList();
        List<String> actorCods = actorRelRepository.findByVideoCodAndStatus(videoCod, "A").stream().map(r -> r.ActorCod).toList();
        List<String> tagCods = tagRelRepository.findByVideoCodAndStatus(videoCod, "A").stream().map(r -> r.TagCod).toList();
        return new VideoDetailDto(video, categoryRepository.findAllById(categoryCods), actorRepository.findAllById(actorCods), tagRepository.findAllById(tagCods), captureRepository.findActiveByVideoCod(videoCod));
    }

    public VideoEntity findEntity(String videoCod) {
        return videoRepository.findById(videoCod).orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
    }

    public VideoCategoryEntity findCategoryById(String categoryCod) {
        return categoryRepository.findById(categoryCod).orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada."));
    }

    public ActorEntity findActorById(String actorCod) {
        return actorRepository.findById(actorCod).orElseThrow(() -> new IllegalArgumentException("Actor no encontrado."));
    }

    public Object findDataForm() {
        return new FormData(categoryRepository.findActives(), actorRepository.findActives(), tagRepository.findActives(), List.of("EMBED", "URL", "PATH"));
    }

    private Integer safeLimit(Integer limit) { return limit == null || limit < 1 ? 12 : Math.min(limit, 50); }

    private VideoCardDto toCard(VideoEntity video) {
        VideoCardDto dto = new VideoCardDto();
        dto.VideoCod = video.VideoCod;
        dto.Title = video.Title;
        dto.ShortDescription = video.ShortDescription;
        dto.ThumbnailUrl = video.ThumbnailUrl;
        dto.SourceType = video.SourceType;
        dto.ViewCount = video.ViewCount;
        dto.PublishDate = video.PublishDate;
        dto.CreationDate = video.CreationDate;
        List<String> categoryCods = categoryRelRepository.findByVideoCodAndStatus(video.VideoCod, "A").stream().map(rel -> rel.CategoryCod).toList();
        List<String> actorCods = actorRelRepository.findByVideoCodAndStatus(video.VideoCod, "A").stream().map(rel -> rel.ActorCod).toList();
        List<String> tagCods = tagRelRepository.findByVideoCodAndStatus(video.VideoCod, "A").stream().map(rel -> rel.TagCod).toList();
        dto.Categories = categoryRepository.findAllById(categoryCods).stream().map(item -> new VideoLabelDto(item.CategoryCod, item.Name)).toList();
        dto.Actors = actorRepository.findAllById(actorCods).stream().map(item -> new VideoLabelDto(item.ActorCod, item.Name)).toList();
        dto.Tags = tagRepository.findAllById(tagCods).stream().limit(3).map(item -> new VideoLabelDto(item.TagCod, item.Name)).toList();
        if (!dto.Categories.isEmpty()) {
            dto.PrimaryCategoryCod = dto.Categories.get(0).Cod;
            dto.PrimaryCategoryName = dto.Categories.get(0).Name;
        }
        return dto;
    }

    public record FormData(List<VideoCategoryEntity> Categories, List<ActorEntity> Actors, List<TagEntity> Tags, List<String> SourceTypes) {}
}
