package com.ccadmin.app.video.model.dto;

import java.math.BigDecimal;

public class VideoGlobalStatisticsDto {
    public Long VideoCount;
    public Long ViewLogCount;
    public Long UniqueViewers;
    public BigDecimal TotalWatchSeconds;
    public BigDecimal AvgWatchSeconds;
    public Long CompletedCount;
    public BigDecimal CompletionRate;
    public Long ReactionCount;
    public Long LikeCount;
    public Long DislikeCount;
    public Long RatingCount;
    public BigDecimal AverageRating;
    public Long WatchLaterCount;
}
