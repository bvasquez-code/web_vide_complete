package com.ccadmin.app.video.model.dto;

import com.ccadmin.app.video.model.entity.VideoEntity;
import java.util.ArrayList;
import java.util.List;

public class VideoRegisterDto {
    public VideoEntity Video;
    public List<String> CategoryCodList = new ArrayList<>();
    public List<String> ActorCodList = new ArrayList<>();
    public List<String> TagCodList = new ArrayList<>();
}
