package com.ccadmin.app.video.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VideoCardDto {
    public String VideoCod;
    public String Title;
    public String ShortDescription;
    public String ThumbnailUrl;
    public String SourceType;
    public String Duration;
    public Long FileSizeBytes;
    public String FileSizeLabel;
    public Integer ResolutionWidth;
    public Integer ResolutionHeight;
    public String ResolutionLabel;
    public Long ViewCount;
    public LocalDateTime PublishDate;
    public LocalDateTime CreationDate;
    public String PrimaryCategoryCod;
    public String PrimaryCategoryName;
    public List<VideoLabelDto> Categories = new ArrayList<>();
    public List<VideoLabelDto> Actors = new ArrayList<>();
    public List<VideoLabelDto> Tags = new ArrayList<>();
}
