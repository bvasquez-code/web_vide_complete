package com.ccadmin.app.video.model.dto;

import java.math.BigDecimal;

public class VideoDailyStatisticsDto {
    public String Day;
    public Long ViewLogCount;
    public BigDecimal TotalWatchSeconds;

    public VideoDailyStatisticsDto(String day, Long viewLogCount, BigDecimal totalWatchSeconds) {
        this.Day = day;
        this.ViewLogCount = viewLogCount;
        this.TotalWatchSeconds = totalWatchSeconds;
    }
}
