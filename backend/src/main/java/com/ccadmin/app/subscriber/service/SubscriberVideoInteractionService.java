package com.ccadmin.app.subscriber.service;

import com.ccadmin.app.subscriber.model.dto.VideoInteractionDto;
import com.ccadmin.app.subscriber.model.dto.ViewerVideoStateDto;
import com.ccadmin.app.subscriber.model.entity.VideoRatingEntity;
import com.ccadmin.app.subscriber.model.entity.VideoReactionEntity;
import com.ccadmin.app.subscriber.model.entity.VideoWatchLaterEntity;
import com.ccadmin.app.subscriber.repository.VideoRatingRepository;
import com.ccadmin.app.subscriber.repository.VideoReactionRepository;
import com.ccadmin.app.subscriber.repository.VideoWatchLaterRepository;
import com.ccadmin.app.video.model.dto.VideoCardDto;
import com.ccadmin.app.video.repository.VideoRepository;
import com.ccadmin.app.video.service.VideoSearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriberVideoInteractionService {
    private final SubscriberAuthService authService;
    private final VideoRepository videoRepository;
    private final VideoReactionRepository reactionRepository;
    private final VideoRatingRepository ratingRepository;
    private final VideoWatchLaterRepository watchLaterRepository;
    private final VideoSearchService videoSearchService;

    public SubscriberVideoInteractionService(SubscriberAuthService authService, VideoRepository videoRepository, VideoReactionRepository reactionRepository, VideoRatingRepository ratingRepository, VideoWatchLaterRepository watchLaterRepository, VideoSearchService videoSearchService) {
        this.authService = authService;
        this.videoRepository = videoRepository;
        this.reactionRepository = reactionRepository;
        this.ratingRepository = ratingRepository;
        this.watchLaterRepository = watchLaterRepository;
        this.videoSearchService = videoSearchService;
    }

    @Transactional
    public ViewerVideoStateDto react(VideoInteractionDto dto) {
        String userCod = authService.requireViewer();
        requireVideo(dto.VideoCod);
        String reactionType = "DISLIKE".equals(dto.ReactionType) ? "DISLIKE" : "LIKE";
        VideoReactionEntity reaction = reactionRepository.findByVideoAndSubscriber(dto.VideoCod, userCod).orElseGet(VideoReactionEntity::new);
        boolean isNew = reaction.ReactionId == null;
        reaction.VideoCod = dto.VideoCod;
        reaction.SubscriberUserCod = userCod;
        reaction.ReactionType = reactionType;
        if (Boolean.FALSE.equals(dto.Enabled)) {
            reaction.inactive(userCod);
        } else if (isNew) {
            reaction.addSessionCreate(userCod);
        } else {
            reaction.active(userCod);
        }
        reactionRepository.save(reaction);
        return state(dto.VideoCod);
    }

    @Transactional
    public ViewerVideoStateDto rate(VideoInteractionDto dto) {
        String userCod = authService.requireViewer();
        requireVideo(dto.VideoCod);
        if (dto.RatingValue == null || dto.RatingValue < 1 || dto.RatingValue > 5) {
            throw new IllegalArgumentException("La valoracion debe estar entre 1 y 5.");
        }
        VideoRatingEntity rating = ratingRepository.findByVideoAndSubscriber(dto.VideoCod, userCod).orElseGet(VideoRatingEntity::new);
        boolean isNew = rating.RatingId == null;
        rating.VideoCod = dto.VideoCod;
        rating.SubscriberUserCod = userCod;
        rating.RatingValue = dto.RatingValue;
        if (isNew) {
            rating.addSessionCreate(userCod);
        } else {
            rating.active(userCod);
        }
        ratingRepository.save(rating);
        return state(dto.VideoCod);
    }

    @Transactional
    public ViewerVideoStateDto watchLater(VideoInteractionDto dto) {
        String userCod = authService.requireViewer();
        requireVideo(dto.VideoCod);
        VideoWatchLaterEntity item = watchLaterRepository.findByVideoAndSubscriber(dto.VideoCod, userCod).orElseGet(VideoWatchLaterEntity::new);
        boolean isNew = item.WatchLaterId == null;
        item.VideoCod = dto.VideoCod;
        item.SubscriberUserCod = userCod;
        if (Boolean.FALSE.equals(dto.Enabled)) {
            item.inactive(userCod);
        } else if (isNew) {
            item.addSessionCreate(userCod);
        } else {
            item.active(userCod);
        }
        watchLaterRepository.save(item);
        return state(dto.VideoCod);
    }

    public ViewerVideoStateDto state(String videoCod) {
        String userCod = authService.requireViewer();
        ViewerVideoStateDto dto = new ViewerVideoStateDto();
        reactionRepository.findByVideoAndSubscriber(videoCod, userCod)
                .filter(item -> "A".equals(item.Status))
                .ifPresent(item -> dto.ReactionType = item.ReactionType);
        ratingRepository.findByVideoAndSubscriber(videoCod, userCod)
                .filter(item -> "A".equals(item.Status))
                .ifPresent(item -> dto.RatingValue = item.RatingValue);
        dto.WatchLater = watchLaterRepository.findByVideoAndSubscriber(videoCod, userCod)
                .map(item -> "A".equals(item.Status))
                .orElse(false);
        return dto;
    }

    public List<VideoCardDto> watchLaterVideos(Integer limit) {
        String userCod = authService.requireViewer();
        return videoRepository.findWatchLaterVideos(userCod, safeLimit(limit)).stream().map(videoSearchService::toCard).toList();
    }

    public List<VideoCardDto> historyVideos(Integer limit) {
        String userCod = authService.requireViewer();
        return videoRepository.findHistoryVideos(userCod, safeLimit(limit)).stream().map(videoSearchService::toCard).toList();
    }

    public List<VideoCardDto> likedVideos(Integer limit) {
        String userCod = authService.requireViewer();
        return videoRepository.findLikedVideos(userCod, safeLimit(limit)).stream().map(videoSearchService::toCard).toList();
    }

    private void requireVideo(String videoCod) {
        if (videoCod == null || videoCod.isBlank() || !videoRepository.existsById(videoCod)) {
            throw new IllegalArgumentException("Video no encontrado.");
        }
    }

    private Integer safeLimit(Integer limit) {
        return limit == null || limit < 1 ? 30 : Math.min(limit, 100);
    }
}
