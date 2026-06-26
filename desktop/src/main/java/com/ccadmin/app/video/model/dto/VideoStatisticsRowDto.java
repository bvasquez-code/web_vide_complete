package com.ccadmin.app.video.model.dto;

import java.math.BigDecimal;

public class VideoStatisticsRowDto {
    public String VideoCod;
    public String Title;
    public String ThumbnailUrl;
    public String SourceType;
    public Long ViewCount;
    public Long ViewLogCount;
    public Long UniqueViewers;
    public BigDecimal TotalWatchSeconds;
    public BigDecimal AvgWatchSeconds;
    public BigDecimal AvgCompletionPercent;
    public Long CompletedCount;
    public BigDecimal CompletionRate;
    public Long ReactionCount;
    public Long LikeCount;
    public Long DislikeCount;
    public Long RatingCount;
    public BigDecimal AverageRating;
    public Long WatchLaterCount;
}
