package com.ccadmin.app.subscriber.repository;

import com.ccadmin.app.subscriber.model.entity.VideoReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VideoReactionRepository extends JpaRepository<VideoReactionEntity, Long> {
    @Query(value = "select * from video_reaction where VideoCod = :VideoCod and SubscriberUserCod = :SubscriberUserCod limit 1", nativeQuery = true)
    Optional<VideoReactionEntity> findByVideoAndSubscriber(@Param("VideoCod") String VideoCod, @Param("SubscriberUserCod") String SubscriberUserCod);

    @Query(value = "select count(1) from video_reaction where VideoCod = :VideoCod and ReactionType = :ReactionType and Status = 'A'", nativeQuery = true)
    Long countActive(@Param("VideoCod") String VideoCod, @Param("ReactionType") String ReactionType);
}
