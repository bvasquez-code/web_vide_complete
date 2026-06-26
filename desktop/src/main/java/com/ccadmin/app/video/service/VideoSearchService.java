package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.model.dto.ResponsePageSearchT;
import com.ccadmin.app.video.model.dto.ActorCaptureGalleryDto;
import com.ccadmin.app.video.model.dto.VideoCardDto;
import com.ccadmin.app.video.model.dto.VideoDetailDto;
import com.ccadmin.app.video.model.dto.VideoLabelDto;
import com.ccadmin.app.video.model.entity.*;
import com.ccadmin.app.video.repository.*;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    public VideoCardDto findRandom(String currentVideoCod) {
        String excludedVideoCod = currentVideoCod == null ? "" : currentVideoCod;
        List<VideoEntity> videos = videoRepository.findRandomRelated(excludedVideoCod, 1);
        if (videos.isEmpty()) {
            throw new IllegalArgumentException("No se encontro otro video disponible.");
        }
        return toCard(videos.get(0));
    }

    public List<VideoCardDto> findRelated(String videoCod, Integer limit) {
        int blockLimit = limit == null || limit < 1 ? 4 : Math.min(limit, 12);
        Map<String, VideoEntity> selected = new LinkedHashMap<>();
        addRelatedBlock(selected, videoCod, blockLimit, videoRepository::findRelatedByActor, videoRepository::findRelatedByActorCoworker, videoRepository::findRelatedByAllCategories, videoRepository::findRelatedByMostCategories, videoRepository::findRandomRelated);
        addRelatedBlock(selected, videoCod, blockLimit, videoRepository::findRelatedByAllCategories, videoRepository::findRelatedByMostCategories, videoRepository::findRandomRelated);
        addRelatedBlock(selected, videoCod, blockLimit, videoRepository::findRelatedByAllTags, videoRepository::findRelatedByMostTags, videoRepository::findRandomRelated);
        addRelatedBlock(selected, videoCod, blockLimit, videoRepository::findRandomRelated);
        return selected.values().stream().map(this::toCard).toList();
    }

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

    public List<ActorCaptureGalleryDto> findActorCaptureGalleries(String actorCod) {
        if (actorCod == null || actorCod.isBlank()) {
            throw new IllegalArgumentException("Actor obligatorio.");
        }
        return videoRepository.findByActorForCaptureGallery(actorCod, 200).stream()
                .map(video -> new ActorCaptureGalleryDto(toCard(video), captureRepository.findActiveByVideoCod(video.VideoCod)))
                .filter(item -> item.Captures != null && !item.Captures.isEmpty())
                .toList();
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

    @SafeVarargs
    private void addRelatedBlock(Map<String, VideoEntity> selected, String videoCod, int blockLimit, RelatedFinder... finders) {
        int initialSize = selected.size();
        for (RelatedFinder finder : finders) {
            if (selected.size() - initialSize >= blockLimit) {
                return;
            }
            int queryLimit = Math.max(blockLimit * 5, 20);
            List<VideoEntity> candidates = finder.find(videoCod, queryLimit);
            for (VideoEntity candidate : candidates) {
                if (candidate == null || candidate.VideoCod == null || candidate.VideoCod.equals(videoCod) || selected.containsKey(candidate.VideoCod)) {
                    continue;
                }
                selected.put(candidate.VideoCod, candidate);
                if (selected.size() - initialSize >= blockLimit) {
                    return;
                }
            }
        }
    }

    public VideoCardDto toCard(VideoEntity video) {
        VideoCardDto dto = new VideoCardDto();
        dto.VideoCod = video.VideoCod;
        dto.Title = video.Title;
        dto.ShortDescription = video.ShortDescription;
        dto.ThumbnailUrl = video.ThumbnailUrl;
        dto.SourceType = video.SourceType;
        dto.Duration = video.Duration;
        dto.FileSizeBytes = video.FileSizeBytes;
        dto.FileSizeLabel = formatFileSize(video.FileSizeBytes);
        dto.ResolutionWidth = video.ResolutionWidth;
        dto.ResolutionHeight = video.ResolutionHeight;
        dto.ResolutionLabel = formatResolution(video.ResolutionWidth, video.ResolutionHeight);
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

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "";
        }
        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value = value / 1024;
            unitIndex++;
        }
        return String.format(Locale.US, value >= 10 ? "%.0f %s" : "%.1f %s", value, units[unitIndex]);
    }

    private String formatResolution(Integer width, Integer height) {
        if (width == null || height == null || width <= 0 || height <= 0) {
            return "";
        }
        return width + "x" + height;
    }

    @FunctionalInterface
    private interface RelatedFinder {
        List<VideoEntity> find(String videoCod, Integer limit);
    }
}
