package com.ccadmin.app.video.model.dto;

import java.util.List;

public class VideoStatisticsDetailDto {
    public VideoStatisticsRowDto Summary;
    public List<VideoDailyStatisticsDto> DailyViews;

    public VideoStatisticsDetailDto(VideoStatisticsRowDto summary, List<VideoDailyStatisticsDto> dailyViews) {
        this.Summary = summary;
        this.DailyViews = dailyViews;
    }
}
