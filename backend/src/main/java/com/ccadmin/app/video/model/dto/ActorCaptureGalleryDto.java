package com.ccadmin.app.video.model.dto;

import com.ccadmin.app.video.model.entity.VideoCaptureEntity;
import java.util.List;

public class ActorCaptureGalleryDto {
    public VideoCardDto Video;
    public List<VideoCaptureEntity> Captures;

    public ActorCaptureGalleryDto(VideoCardDto video, List<VideoCaptureEntity> captures) {
        this.Video = video;
        this.Captures = captures;
    }
}
