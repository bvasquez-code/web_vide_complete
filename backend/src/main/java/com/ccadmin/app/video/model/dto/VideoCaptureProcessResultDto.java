package com.ccadmin.app.video.model.dto;

import com.ccadmin.app.video.model.entity.VideoCaptureEntity;
import java.util.ArrayList;
import java.util.List;

public class VideoCaptureProcessResultDto {
    public String VideoCod;
    public String Duration;
    public Integer CaptureCount = 0;
    public List<VideoCaptureEntity> Captures = new ArrayList<>();
}
