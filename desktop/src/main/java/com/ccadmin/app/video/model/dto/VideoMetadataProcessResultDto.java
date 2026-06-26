package com.ccadmin.app.video.model.dto;

import java.util.ArrayList;
import java.util.List;

public class VideoMetadataProcessResultDto {
    public Integer TotalVideos = 0;
    public Integer Processed = 0;
    public Integer Skipped = 0;
    public Integer Errors = 0;
    public Double Percentage;
    public List<VideoMetadataProcessItemDto> Items = new ArrayList<>();
}
