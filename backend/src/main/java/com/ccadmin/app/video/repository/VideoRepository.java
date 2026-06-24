package com.ccadmin.app.video.repository;

import com.ccadmin.app.video.model.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface VideoRepository extends JpaRepository<VideoEntity, String> {
    @Query(value = "select * from video where Status = 'A' order by coalesce(PublishDate, CreationDate) desc limit :limit", nativeQuery = true)
    List<VideoEntity> findRecent(@Param("limit") Integer limit);

    @Query(value = "select * from video where Status = 'A' order by ViewCount desc, coalesce(PublishDate, CreationDate) desc limit :limit", nativeQuery = true)
    List<VideoEntity> findMostViewed(@Param("limit") Integer limit);

    @Query(value = """
            select distinct v.* from video v
            inner join video_category_rel r on r.VideoCod = v.VideoCod and r.Status = 'A'
            where v.Status = 'A' and r.CategoryCod = :categoryCod
            order by case when :sort = 'views' then v.ViewCount else 0 end desc, coalesce(v.PublishDate, v.CreationDate) desc
            limit :init, :limit
            """, nativeQuery = true)
    List<VideoEntity> findByCategory(@Param("categoryCod") String categoryCod, @Param("sort") String sort, @Param("init") Integer init, @Param("limit") Integer limit);

    @Query(value = """
            select count(distinct v.VideoCod) from video v
            inner join video_category_rel r on r.VideoCod = v.VideoCod and r.Status = 'A'
            where v.Status = 'A' and r.CategoryCod = :categoryCod
            """, nativeQuery = true)
    Long countByCategory(@Param("categoryCod") String categoryCod);

    @Query(value = """
            select distinct v.* from video v
            inner join video_actor_rel r on r.VideoCod = v.VideoCod and r.Status = 'A'
            where v.Status = 'A' and r.ActorCod = :actorCod
            order by case when :sort = 'views' then v.ViewCount else 0 end desc, coalesce(v.PublishDate, v.CreationDate) desc
            limit :init, :limit
            """, nativeQuery = true)
    List<VideoEntity> findByActor(@Param("actorCod") String actorCod, @Param("sort") String sort, @Param("init") Integer init, @Param("limit") Integer limit);

    @Query(value = """
            select count(distinct v.VideoCod) from video v
            inner join video_actor_rel r on r.VideoCod = v.VideoCod and r.Status = 'A'
            where v.Status = 'A' and r.ActorCod = :actorCod
            """, nativeQuery = true)
    Long countByActor(@Param("actorCod") String actorCod);

    @Query(value = """
            select distinct v.* from video v
            inner join video_category_rel r on r.VideoCod = v.VideoCod and r.Status = 'A'
            where v.Status = 'A'
              and v.VideoCod <> :videoCod
              and r.CategoryCod in (select CategoryCod from video_category_rel where VideoCod = :videoCod and Status = 'A')
            order by coalesce(v.PublishDate, v.CreationDate) desc
            limit :limit
            """, nativeQuery = true)
    List<VideoEntity> findRelated(@Param("videoCod") String videoCod, @Param("limit") Integer limit);

    @Query(value = """
            select distinct v.* from video v
            left join video_actor_rel ar on ar.VideoCod = v.VideoCod and ar.Status = 'A'
            left join actor a on a.ActorCod = ar.ActorCod and a.Status = 'A'
            left join video_tag_rel tr on tr.VideoCod = v.VideoCod and tr.Status = 'A'
            left join tag t on t.TagCod = tr.TagCod and t.Status = 'A'
            where v.Status = 'A'
              and (:query = '' or v.Title like concat('%', :query, '%') or a.Name like concat('%', :query, '%') or t.Name like concat('%', :query, '%'))
            order by case when :sort = 'views' then v.ViewCount else 0 end desc, coalesce(v.PublishDate, v.CreationDate) desc
            limit :init, :limit
            """, nativeQuery = true)
    List<VideoEntity> searchPublic(@Param("query") String query, @Param("sort") String sort, @Param("init") Integer init, @Param("limit") Integer limit);

    @Query(value = """
            select count(distinct v.VideoCod) from video v
            left join video_actor_rel ar on ar.VideoCod = v.VideoCod and ar.Status = 'A'
            left join actor a on a.ActorCod = ar.ActorCod and a.Status = 'A'
            left join video_tag_rel tr on tr.VideoCod = v.VideoCod and tr.Status = 'A'
            left join tag t on t.TagCod = tr.TagCod and t.Status = 'A'
            where v.Status = 'A'
              and (:query = '' or v.Title like concat('%', :query, '%') or a.Name like concat('%', :query, '%') or t.Name like concat('%', :query, '%'))
            """, nativeQuery = true)
    Long countSearchPublic(@Param("query") String query);

    @Query(value = """
            select distinct v.* from video v
            left join video_category_rel cr on cr.VideoCod = v.VideoCod and cr.Status = 'A'
            left join video_actor_rel ar on ar.VideoCod = v.VideoCod and ar.Status = 'A'
            left join video_tag_rel tr on tr.VideoCod = v.VideoCod and tr.Status = 'A'
            where (:query = '' or v.Title like concat('%', :query, '%'))
              and (:status = '' or v.Status = :status)
              and (:sourceType = '' or v.SourceType = :sourceType)
              and (:categoryCod = '' or cr.CategoryCod = :categoryCod)
              and (:actorCod = '' or ar.ActorCod = :actorCod)
              and (:tagCod = '' or tr.TagCod = :tagCod)
            order by v.CreationDate desc
            limit :init, :limit
            """, nativeQuery = true)
    List<VideoEntity> findByFilters(@Param("query") String query, @Param("status") String status, @Param("sourceType") String sourceType, @Param("categoryCod") String categoryCod, @Param("actorCod") String actorCod, @Param("tagCod") String tagCod, @Param("init") Integer init, @Param("limit") Integer limit);

    @Query(value = """
            select count(distinct v.VideoCod) from video v
            left join video_category_rel cr on cr.VideoCod = v.VideoCod and cr.Status = 'A'
            left join video_actor_rel ar on ar.VideoCod = v.VideoCod and ar.Status = 'A'
            left join video_tag_rel tr on tr.VideoCod = v.VideoCod and tr.Status = 'A'
            where (:query = '' or v.Title like concat('%', :query, '%'))
              and (:status = '' or v.Status = :status)
              and (:sourceType = '' or v.SourceType = :sourceType)
              and (:categoryCod = '' or cr.CategoryCod = :categoryCod)
              and (:actorCod = '' or ar.ActorCod = :actorCod)
              and (:tagCod = '' or tr.TagCod = :tagCod)
            """, nativeQuery = true)
    Long countByFilters(@Param("query") String query, @Param("status") String status, @Param("sourceType") String sourceType, @Param("categoryCod") String categoryCod, @Param("actorCod") String actorCod, @Param("tagCod") String tagCod);

    @Modifying
    @Query(value = "update video set ViewCount = ViewCount + 1 where VideoCod = :videoCod", nativeQuery = true)
    void incrementView(@Param("videoCod") String videoCod);
}
