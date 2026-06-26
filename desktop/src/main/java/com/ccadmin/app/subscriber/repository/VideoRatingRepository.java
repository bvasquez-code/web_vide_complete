package com.ccadmin.app.subscriber.repository;

import com.ccadmin.app.subscriber.model.entity.VideoRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VideoRatingRepository extends JpaRepository<VideoRatingEntity, Long> {
    @Query(value = "select * from video_rating where VideoCod = :VideoCod and SubscriberUserCod = :SubscriberUserCod limit 1", nativeQuery = true)
    Optional<VideoRatingEntity> findByVideoAndSubscriber(@Param("VideoCod") String VideoCod, @Param("SubscriberUserCod") String SubscriberUserCod);
}
