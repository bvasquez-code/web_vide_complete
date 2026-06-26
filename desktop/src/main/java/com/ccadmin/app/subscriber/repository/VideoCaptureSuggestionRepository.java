package com.ccadmin.app.subscriber.repository;

import com.ccadmin.app.subscriber.model.entity.VideoCaptureSuggestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoCaptureSuggestionRepository extends JpaRepository<VideoCaptureSuggestionEntity, Long> {
    @Query(value = "select * from video_capture_suggestion where Status = 'P' order by CreationDate desc", nativeQuery = true)
    List<VideoCaptureSuggestionEntity> findPending();

    @Query(value = "select * from video_capture_suggestion where VideoCod = :VideoCod and Status = 'P' order by CreationDate desc", nativeQuery = true)
    List<VideoCaptureSuggestionEntity> findPendingByVideoCod(@Param("VideoCod") String VideoCod);
}
