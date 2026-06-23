package com.ccadmin.app.video.repository;

import com.ccadmin.app.video.model.entity.VideoTagRelEntity;
import com.ccadmin.app.video.model.entity.id.VideoTagRelID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface VideoTagRelRepository extends JpaRepository<VideoTagRelEntity, VideoTagRelID> {
    @Query(value = "select * from video_tag_rel where VideoCod = :VideoCod and Status = :Status", nativeQuery = true)
    List<VideoTagRelEntity> findByVideoCodAndStatus(@Param("VideoCod") String VideoCod, @Param("Status") String Status);

    @Modifying
    @Query(value = "delete from video_tag_rel where VideoCod = :VideoCod", nativeQuery = true)
    void deleteByVideoCod(@Param("VideoCod") String VideoCod);
}
