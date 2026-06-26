package com.ccadmin.app.subscriber.repository;

import com.ccadmin.app.subscriber.model.entity.VideoWatchLaterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VideoWatchLaterRepository extends JpaRepository<VideoWatchLaterEntity, Long> {
    @Query(value = "select * from video_watch_later where VideoCod = :VideoCod and SubscriberUserCod = :SubscriberUserCod limit 1", nativeQuery = true)
    Optional<VideoWatchLaterEntity> findByVideoAndSubscriber(@Param("VideoCod") String VideoCod, @Param("SubscriberUserCod") String SubscriberUserCod);

}
