package com.ccadmin.app.video.repository;

import com.ccadmin.app.video.model.entity.VideoViewLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoViewLogRepository extends JpaRepository<VideoViewLogEntity, Long> {
}
