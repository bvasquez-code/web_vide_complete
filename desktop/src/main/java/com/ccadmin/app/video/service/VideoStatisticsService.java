package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.model.dto.ResponsePageSearchT;
import com.ccadmin.app.video.model.dto.ActorStatisticsDetailDto;
import com.ccadmin.app.video.model.dto.ActorStatisticsRowDto;
import com.ccadmin.app.video.model.dto.VideoDailyStatisticsDto;
import com.ccadmin.app.video.model.dto.VideoGlobalStatisticsDto;
import com.ccadmin.app.video.model.dto.VideoStatisticsDetailDto;
import com.ccadmin.app.video.model.dto.VideoStatisticsRowDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.List;

@Service
public class VideoStatisticsService {
    @PersistenceContext
    private EntityManager entityManager;

    public ResponsePageSearchT<VideoStatisticsRowDto> findVideoRanking(String sort, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);
        String orderColumn = "watchTime".equalsIgnoreCase(sort) ? "TotalWatchSeconds" : "ViewLogCount";
        List<VideoStatisticsRowDto> rows = findVideoRows("", orderColumn, (safePage - 1) * safeLimit, safeLimit);
        Long totalRows = numberToLong(entityManager.createNativeQuery("select count(1) from video where Status = 'A'").getSingleResult());
        return new ResponsePageSearchT<>(rows, totalRows, safePage, safeLimit);
    }

    public VideoGlobalStatisticsDto findVideoGlobal() {
        Query query = entityManager.createNativeQuery("""
                select
                    (select count(1) from video where Status = 'A') VideoCount,
                    coalesce(l.ViewLogCount, 0) ViewLogCount,
                    coalesce(l.UniqueViewers, 0) UniqueViewers,
                    coalesce(l.TotalWatchSeconds, 0) TotalWatchSeconds,
                    coalesce(l.AvgWatchSeconds, 0) AvgWatchSeconds,
                    coalesce(l.CompletedCount, 0) CompletedCount,
                    coalesce(l.CompletionRate, 0) CompletionRate,
                    coalesce(r.ReactionCount, 0) ReactionCount,
                    coalesce(r.LikeCount, 0) LikeCount,
                    coalesce(r.DislikeCount, 0) DislikeCount,
                    coalesce(rt.RatingCount, 0) RatingCount,
                    coalesce(rt.AverageRating, 0) AverageRating,
                    coalesce(w.WatchLaterCount, 0) WatchLaterCount
                from (select 1) base
                left join (
                    select count(1) ViewLogCount,
                           count(distinct case
                               when ViewerUserCod is not null then concat('U:', ViewerUserCod)
                               when ViewerIp is not null then concat('IP:', ViewerIp)
                               else concat('L:', ViewLogId)
                           end) UniqueViewers,
                           sum(WatchSeconds) TotalWatchSeconds,
                           avg(WatchSeconds) AvgWatchSeconds,
                           sum(case when Completed = 'Y' then 1 else 0 end) CompletedCount,
                           sum(case when Completed = 'Y' then 1 else 0 end) * 100 / nullif(count(1), 0) CompletionRate
                    from video_view_log
                    where Status = 'A'
                ) l on 1 = 1
                left join (
                    select count(1) ReactionCount,
                           sum(case when ReactionType = 'LIKE' then 1 else 0 end) LikeCount,
                           sum(case when ReactionType = 'DISLIKE' then 1 else 0 end) DislikeCount
                    from video_reaction
                    where Status = 'A'
                ) r on 1 = 1
                left join (
                    select count(1) RatingCount, avg(RatingValue) AverageRating
                    from video_rating
                    where Status = 'A'
                ) rt on 1 = 1
                left join (
                    select count(1) WatchLaterCount
                    from video_watch_later
                    where Status = 'A'
                ) w on 1 = 1
                """);
        Object[] row = (Object[]) query.getSingleResult();
        VideoGlobalStatisticsDto dto = new VideoGlobalStatisticsDto();
        dto.VideoCount = numberToLong(row[0]);
        dto.ViewLogCount = numberToLong(row[1]);
        dto.UniqueViewers = numberToLong(row[2]);
        dto.TotalWatchSeconds = numberToBigDecimal(row[3]);
        dto.AvgWatchSeconds = numberToBigDecimal(row[4]);
        dto.CompletedCount = numberToLong(row[5]);
        dto.CompletionRate = numberToBigDecimal(row[6]);
        dto.ReactionCount = numberToLong(row[7]);
        dto.LikeCount = numberToLong(row[8]);
        dto.DislikeCount = numberToLong(row[9]);
        dto.RatingCount = numberToLong(row[10]);
        dto.AverageRating = numberToBigDecimal(row[11]);
        dto.WatchLaterCount = numberToLong(row[12]);
        return dto;
    }

    public VideoStatisticsDetailDto findVideoDetail(String videoCod) {
        List<VideoStatisticsRowDto> rows = findVideoRows(videoCod, "ViewLogCount", 0, 1);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Video no encontrado.");
        }
        return new VideoStatisticsDetailDto(rows.get(0), findVideoDailyViews(videoCod));
    }

    public ResponsePageSearchT<ActorStatisticsRowDto> findActorRanking(String sort, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);
        String orderColumn = "watchTime".equalsIgnoreCase(sort) ? "TotalWatchSeconds" : "ViewLogCount";
        List<ActorStatisticsRowDto> rows = findActorRows("", orderColumn, (safePage - 1) * safeLimit, safeLimit);
        Long totalRows = numberToLong(entityManager.createNativeQuery("select count(1) from actor where Status = 'A'").getSingleResult());
        return new ResponsePageSearchT<>(rows, totalRows, safePage, safeLimit);
    }

    public ActorStatisticsDetailDto findActorDetail(String actorCod) {
        List<ActorStatisticsRowDto> rows = findActorRows(actorCod, "ViewLogCount", 0, 1);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Actor no encontrado.");
        }
        return new ActorStatisticsDetailDto(rows.get(0), findVideoRowsByActor(actorCod, "ViewLogCount", 0, 10), findActorDailyViews(actorCod));
    }

    private List<VideoStatisticsRowDto> findVideoRows(String videoCod, String orderColumn, int init, int limit) {
        String sql = """
                select v.VideoCod, v.Title, v.ThumbnailUrl, v.SourceType, v.ViewCount,
                       coalesce(l.ViewLogCount, 0) ViewLogCount,
                       coalesce(l.UniqueViewers, 0) UniqueViewers,
                       coalesce(l.TotalWatchSeconds, 0) TotalWatchSeconds,
                       coalesce(l.AvgWatchSeconds, 0) AvgWatchSeconds,
                       coalesce(l.AvgCompletionPercent, 0) AvgCompletionPercent,
                       coalesce(l.CompletedCount, 0) CompletedCount,
                       coalesce(l.CompletionRate, 0) CompletionRate,
                       coalesce(r.ReactionCount, 0) ReactionCount,
                       coalesce(r.LikeCount, 0) LikeCount,
                       coalesce(r.DislikeCount, 0) DislikeCount,
                       coalesce(rt.RatingCount, 0) RatingCount,
                       coalesce(rt.AverageRating, 0) AverageRating,
                       coalesce(w.WatchLaterCount, 0) WatchLaterCount
                from video v
                left join (
                    select VideoCod,
                           count(1) ViewLogCount,
                           count(distinct case
                               when ViewerUserCod is not null then concat('U:', ViewerUserCod)
                               when ViewerIp is not null then concat('IP:', ViewerIp)
                               else concat('L:', ViewLogId)
                           end) UniqueViewers,
                           sum(WatchSeconds) TotalWatchSeconds,
                           avg(WatchSeconds) AvgWatchSeconds,
                           avg(case when DurationSeconds > 0 then least(100, WatchSeconds * 100 / DurationSeconds) else null end) AvgCompletionPercent,
                           sum(case when Completed = 'Y' then 1 else 0 end) CompletedCount,
                           sum(case when Completed = 'Y' then 1 else 0 end) * 100 / count(1) CompletionRate
                    from video_view_log
                    where Status = 'A'
                    group by VideoCod
                ) l on l.VideoCod = v.VideoCod
                left join (
                    select VideoCod,
                           count(1) ReactionCount,
                           sum(case when ReactionType = 'LIKE' then 1 else 0 end) LikeCount,
                           sum(case when ReactionType = 'DISLIKE' then 1 else 0 end) DislikeCount
                    from video_reaction
                    where Status = 'A'
                    group by VideoCod
                ) r on r.VideoCod = v.VideoCod
                left join (
                    select VideoCod, count(1) RatingCount, avg(RatingValue) AverageRating
                    from video_rating
                    where Status = 'A'
                    group by VideoCod
                ) rt on rt.VideoCod = v.VideoCod
                left join (
                    select VideoCod, count(1) WatchLaterCount
                    from video_watch_later
                    where Status = 'A'
                    group by VideoCod
                ) w on w.VideoCod = v.VideoCod
                where v.Status = 'A' and (:videoCod = '' or v.VideoCod = :videoCod)
                order by %s desc, v.Title
                limit :init, :limit
                """.formatted(orderColumn);
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("videoCod", videoCod == null ? "" : videoCod);
        query.setParameter("init", init);
        query.setParameter("limit", limit);
        return ((List<Object[]>) query.getResultList()).stream().map(this::toVideoRow).toList();
    }

    private List<VideoStatisticsRowDto> findVideoRowsByActor(String actorCod, String orderColumn, int init, int limit) {
        String sql = """
                select x.* from (
                    select v.VideoCod, v.Title, v.ThumbnailUrl, v.SourceType, v.ViewCount,
                           coalesce(l.ViewLogCount, 0) ViewLogCount,
                           coalesce(l.UniqueViewers, 0) UniqueViewers,
                           coalesce(l.TotalWatchSeconds, 0) TotalWatchSeconds,
                           coalesce(l.AvgWatchSeconds, 0) AvgWatchSeconds,
                           coalesce(l.AvgCompletionPercent, 0) AvgCompletionPercent,
                           coalesce(l.CompletedCount, 0) CompletedCount,
                           coalesce(l.CompletionRate, 0) CompletionRate,
                           coalesce(r.ReactionCount, 0) ReactionCount,
                           coalesce(r.LikeCount, 0) LikeCount,
                           coalesce(r.DislikeCount, 0) DislikeCount,
                           coalesce(rt.RatingCount, 0) RatingCount,
                           coalesce(rt.AverageRating, 0) AverageRating,
                           coalesce(w.WatchLaterCount, 0) WatchLaterCount
                    from video v
                    inner join video_actor_rel ar on ar.VideoCod = v.VideoCod and ar.Status = 'A' and ar.ActorCod = :actorCod
                    left join (
                        select VideoCod, count(1) ViewLogCount,
                               count(distinct case when ViewerUserCod is not null then concat('U:', ViewerUserCod) when ViewerIp is not null then concat('IP:', ViewerIp) else concat('L:', ViewLogId) end) UniqueViewers,
                               sum(WatchSeconds) TotalWatchSeconds, avg(WatchSeconds) AvgWatchSeconds,
                               avg(case when DurationSeconds > 0 then least(100, WatchSeconds * 100 / DurationSeconds) else null end) AvgCompletionPercent,
                               sum(case when Completed = 'Y' then 1 else 0 end) CompletedCount,
                               sum(case when Completed = 'Y' then 1 else 0 end) * 100 / count(1) CompletionRate
                        from video_view_log where Status = 'A' group by VideoCod
                    ) l on l.VideoCod = v.VideoCod
                    left join (select VideoCod, count(1) ReactionCount, sum(case when ReactionType = 'LIKE' then 1 else 0 end) LikeCount, sum(case when ReactionType = 'DISLIKE' then 1 else 0 end) DislikeCount from video_reaction where Status = 'A' group by VideoCod) r on r.VideoCod = v.VideoCod
                    left join (select VideoCod, count(1) RatingCount, avg(RatingValue) AverageRating from video_rating where Status = 'A' group by VideoCod) rt on rt.VideoCod = v.VideoCod
                    left join (select VideoCod, count(1) WatchLaterCount from video_watch_later where Status = 'A' group by VideoCod) w on w.VideoCod = v.VideoCod
                    where v.Status = 'A'
                ) x
                order by %s desc, Title
                limit :init, :limit
                """.formatted(orderColumn);
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("actorCod", actorCod);
        query.setParameter("init", init);
        query.setParameter("limit", limit);
        return ((List<Object[]>) query.getResultList()).stream().map(this::toVideoRow).toList();
    }

    private List<ActorStatisticsRowDto> findActorRows(String actorCod, String orderColumn, int init, int limit) {
        String sql = """
                select a.ActorCod, a.Name, a.ImageUrl,
                       coalesce(vc.VideoCount, 0) VideoCount,
                       coalesce(l.ViewLogCount, 0) ViewLogCount,
                       coalesce(l.UniqueViewers, 0) UniqueViewers,
                       coalesce(l.TotalWatchSeconds, 0) TotalWatchSeconds,
                       coalesce(l.AvgWatchSeconds, 0) AvgWatchSeconds,
                       coalesce(l.AvgCompletionPercent, 0) AvgCompletionPercent,
                       coalesce(l.CompletedCount, 0) CompletedCount,
                       coalesce(l.CompletionRate, 0) CompletionRate,
                       coalesce(r.ReactionCount, 0) ReactionCount,
                       coalesce(rt.RatingCount, 0) RatingCount,
                       coalesce(rt.AverageRating, 0) AverageRating,
                       coalesce(w.WatchLaterCount, 0) WatchLaterCount
                from actor a
                left join (
                    select ar.ActorCod, count(distinct ar.VideoCod) VideoCount
                    from video_actor_rel ar
                    inner join video v on v.VideoCod = ar.VideoCod and v.Status = 'A'
                    where ar.Status = 'A'
                    group by ar.ActorCod
                ) vc on vc.ActorCod = a.ActorCod
                left join (
                    select ar.ActorCod,
                           count(l.ViewLogId) ViewLogCount,
                           count(distinct case
                               when l.ViewerUserCod is not null then concat('U:', l.ViewerUserCod)
                               when l.ViewerIp is not null then concat('IP:', l.ViewerIp)
                               else concat('L:', l.ViewLogId)
                           end) UniqueViewers,
                           sum(l.WatchSeconds) TotalWatchSeconds,
                           avg(l.WatchSeconds) AvgWatchSeconds,
                           avg(case when l.DurationSeconds > 0 then least(100, l.WatchSeconds * 100 / l.DurationSeconds) else null end) AvgCompletionPercent,
                           sum(case when l.Completed = 'Y' then 1 else 0 end) CompletedCount,
                           sum(case when l.Completed = 'Y' then 1 else 0 end) * 100 / count(l.ViewLogId) CompletionRate
                    from video_actor_rel ar
                    inner join video v on v.VideoCod = ar.VideoCod and v.Status = 'A'
                    inner join video_view_log l on l.VideoCod = ar.VideoCod and l.Status = 'A'
                    where ar.Status = 'A'
                    group by ar.ActorCod
                ) l on l.ActorCod = a.ActorCod
                left join (
                    select ar.ActorCod, count(r.ReactionId) ReactionCount
                    from video_actor_rel ar
                    inner join video_reaction r on r.VideoCod = ar.VideoCod and r.Status = 'A'
                    where ar.Status = 'A'
                    group by ar.ActorCod
                ) r on r.ActorCod = a.ActorCod
                left join (
                    select ar.ActorCod, count(rt.RatingId) RatingCount, avg(rt.RatingValue) AverageRating
                    from video_actor_rel ar
                    inner join video_rating rt on rt.VideoCod = ar.VideoCod and rt.Status = 'A'
                    where ar.Status = 'A'
                    group by ar.ActorCod
                ) rt on rt.ActorCod = a.ActorCod
                left join (
                    select ar.ActorCod, count(w.WatchLaterId) WatchLaterCount
                    from video_actor_rel ar
                    inner join video_watch_later w on w.VideoCod = ar.VideoCod and w.Status = 'A'
                    where ar.Status = 'A'
                    group by ar.ActorCod
                ) w on w.ActorCod = a.ActorCod
                where a.Status = 'A' and (:actorCod = '' or a.ActorCod = :actorCod)
                order by %s desc, a.Name
                limit :init, :limit
                """.formatted(orderColumn);
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("actorCod", actorCod == null ? "" : actorCod);
        query.setParameter("init", init);
        query.setParameter("limit", limit);
        return ((List<Object[]>) query.getResultList()).stream().map(this::toActorRow).toList();
    }

    private List<VideoDailyStatisticsDto> findVideoDailyViews(String videoCod) {
        Query query = entityManager.createNativeQuery("""
                select date(CreationDate) Day, count(1) ViewLogCount, coalesce(sum(WatchSeconds), 0) TotalWatchSeconds
                from video_view_log
                where Status = 'A' and VideoCod = :videoCod
                group by date(CreationDate)
                order by Day desc
                limit 30
                """);
        query.setParameter("videoCod", videoCod);
        return ((List<Object[]>) query.getResultList()).stream().map(this::toDailyRow).toList();
    }

    private List<VideoDailyStatisticsDto> findActorDailyViews(String actorCod) {
        Query query = entityManager.createNativeQuery("""
                select date(l.CreationDate) Day, count(1) ViewLogCount, coalesce(sum(l.WatchSeconds), 0) TotalWatchSeconds
                from video_actor_rel ar
                inner join video_view_log l on l.VideoCod = ar.VideoCod and l.Status = 'A'
                inner join video v on v.VideoCod = ar.VideoCod and v.Status = 'A'
                where ar.Status = 'A' and ar.ActorCod = :actorCod
                group by date(l.CreationDate)
                order by Day desc
                limit 30
                """);
        query.setParameter("actorCod", actorCod);
        return ((List<Object[]>) query.getResultList()).stream().map(this::toDailyRow).toList();
    }

    private VideoStatisticsRowDto toVideoRow(Object[] row) {
        VideoStatisticsRowDto dto = new VideoStatisticsRowDto();
        dto.VideoCod = stringValue(row[0]);
        dto.Title = stringValue(row[1]);
        dto.ThumbnailUrl = stringValue(row[2]);
        dto.SourceType = stringValue(row[3]);
        dto.ViewCount = numberToLong(row[4]);
        dto.ViewLogCount = numberToLong(row[5]);
        dto.UniqueViewers = numberToLong(row[6]);
        dto.TotalWatchSeconds = numberToBigDecimal(row[7]);
        dto.AvgWatchSeconds = numberToBigDecimal(row[8]);
        dto.AvgCompletionPercent = numberToBigDecimal(row[9]);
        dto.CompletedCount = numberToLong(row[10]);
        dto.CompletionRate = numberToBigDecimal(row[11]);
        dto.ReactionCount = numberToLong(row[12]);
        dto.LikeCount = numberToLong(row[13]);
        dto.DislikeCount = numberToLong(row[14]);
        dto.RatingCount = numberToLong(row[15]);
        dto.AverageRating = numberToBigDecimal(row[16]);
        dto.WatchLaterCount = numberToLong(row[17]);
        return dto;
    }

    private ActorStatisticsRowDto toActorRow(Object[] row) {
        ActorStatisticsRowDto dto = new ActorStatisticsRowDto();
        dto.ActorCod = stringValue(row[0]);
        dto.Name = stringValue(row[1]);
        dto.ImageUrl = stringValue(row[2]);
        dto.VideoCount = numberToLong(row[3]);
        dto.ViewLogCount = numberToLong(row[4]);
        dto.UniqueViewers = numberToLong(row[5]);
        dto.TotalWatchSeconds = numberToBigDecimal(row[6]);
        dto.AvgWatchSeconds = numberToBigDecimal(row[7]);
        dto.AvgCompletionPercent = numberToBigDecimal(row[8]);
        dto.CompletedCount = numberToLong(row[9]);
        dto.CompletionRate = numberToBigDecimal(row[10]);
        dto.ReactionCount = numberToLong(row[11]);
        dto.RatingCount = numberToLong(row[12]);
        dto.AverageRating = numberToBigDecimal(row[13]);
        dto.WatchLaterCount = numberToLong(row[14]);
        return dto;
    }

    private VideoDailyStatisticsDto toDailyRow(Object[] row) {
        String day = row[0] instanceof Date date ? date.toString() : stringValue(row[0]);
        return new VideoDailyStatisticsDto(day, numberToLong(row[1]), numberToBigDecimal(row[2]));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Long numberToLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof BigInteger bigInteger) return bigInteger.longValue();
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private BigDecimal numberToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bigDecimal) return bigDecimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return new BigDecimal(String.valueOf(value));
    }
}
