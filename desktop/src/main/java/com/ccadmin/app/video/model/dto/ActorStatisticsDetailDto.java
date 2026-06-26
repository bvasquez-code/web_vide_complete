package com.ccadmin.app.video.model.dto;

import java.util.List;

public class ActorStatisticsDetailDto {
    public ActorStatisticsRowDto Summary;
    public List<VideoStatisticsRowDto> TopVideos;
    public List<VideoDailyStatisticsDto> DailyViews;

    public ActorStatisticsDetailDto(ActorStatisticsRowDto summary, List<VideoStatisticsRowDto> topVideos, List<VideoDailyStatisticsDto> dailyViews) {
        this.Summary = summary;
        this.TopVideos = topVideos;
        this.DailyViews = dailyViews;
    }
}
