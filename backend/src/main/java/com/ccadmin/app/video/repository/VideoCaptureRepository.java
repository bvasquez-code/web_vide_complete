package com.ccadmin.app.video.repository;

import com.ccadmin.app.video.model.entity.VideoCaptureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface VideoCaptureRepository extends JpaRepository<VideoCaptureEntity, Long> {
    @Query(value = "select * from video_capture where VideoCod = :VideoCod and Status = 'A' order by CaptureSecond, DisplayOrder", nativeQuery = true)
    List<VideoCaptureEntity> findActiveByVideoCod(@Param("VideoCod") String VideoCod);

    @Query(value = "select coalesce(max(DisplayOrder), 0) from video_capture where VideoCod = :VideoCod", nativeQuery = true)
    Integer findMaxDisplayOrder(@Param("VideoCod") String VideoCod);

    @Modifying
    @Query(value = "delete from video_capture where VideoCod = :VideoCod", nativeQuery = true)
    void deleteByVideoCod(@Param("VideoCod") String VideoCod);
}
