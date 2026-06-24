package com.ccadmin.app.video.model.dto;

import java.math.BigDecimal;

public class ActorStatisticsRowDto {
    public String ActorCod;
    public String Name;
    public String ImageUrl;
    public Long VideoCount;
    public Long ViewLogCount;
    public Long UniqueViewers;
    public BigDecimal TotalWatchSeconds;
    public BigDecimal AvgWatchSeconds;
    public BigDecimal AvgCompletionPercent;
    public Long CompletedCount;
    public BigDecimal CompletionRate;
    public Long ReactionCount;
    public Long RatingCount;
    public BigDecimal AverageRating;
    public Long WatchLaterCount;
}
