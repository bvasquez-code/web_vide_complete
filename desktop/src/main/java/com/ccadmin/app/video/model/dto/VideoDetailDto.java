package com.ccadmin.app.video.model.dto;

import com.ccadmin.app.video.model.entity.ActorEntity;
import com.ccadmin.app.video.model.entity.TagEntity;
import com.ccadmin.app.video.model.entity.VideoCaptureEntity;
import com.ccadmin.app.video.model.entity.VideoCategoryEntity;
import com.ccadmin.app.video.model.entity.VideoEntity;
import java.util.List;

public class VideoDetailDto {
    public VideoEntity Video;
    public List<VideoCategoryEntity> Categories;
    public List<ActorEntity> Actors;
    public List<TagEntity> Tags;
    public List<VideoCaptureEntity> Captures;

    public VideoDetailDto(VideoEntity video, List<VideoCategoryEntity> categories, List<ActorEntity> actors, List<TagEntity> tags, List<VideoCaptureEntity> captures) {
        this.Video = video;
        this.Categories = categories;
        this.Actors = actors;
        this.Tags = tags;
        this.Captures = captures;
    }
}
