package com.ccadmin.app.video.repository;

import com.ccadmin.app.video.model.entity.VideoCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VideoCategoryRepository extends JpaRepository<VideoCategoryEntity, String> {
    @Query(value = "select * from video_category where Name = :Name limit 1", nativeQuery = true)
    Optional<VideoCategoryEntity> findByName(@Param("Name") String Name);

    @Query(value = "select * from video_category where Status = 'A' order by DisplayOrder, Name", nativeQuery = true)
    List<VideoCategoryEntity> findActives();

    @Query(value = """
            select c.* from video_category c
            left join video_category_rel r on r.CategoryCod = c.CategoryCod and r.Status = 'A'
            left join video v on v.VideoCod = r.VideoCod and v.Status = 'A'
            where c.Status = 'A'
            group by c.CategoryCod, c.Name, c.Description, c.ImageUrl, c.DisplayOrder, c.CreationUser, c.CreationDate, c.ModifyUser, c.ModifyDate, c.Status
            order by coalesce(sum(v.ViewCount), 0) desc, c.DisplayOrder, c.Name
            limit :limit
            """, nativeQuery = true)
    List<VideoCategoryEntity> findTopViewed(@Param("limit") Integer limit);

    @Query(value = "select * from video_category where (:query = '' or Name like concat('%', :query, '%')) and (:status = '' or Status = :status) order by DisplayOrder, Name limit :init, :limit", nativeQuery = true)
    List<VideoCategoryEntity> findByFilters(@Param("query") String query, @Param("status") String status, @Param("init") Integer init, @Param("limit") Integer limit);

    @Query(value = "select count(*) from video_category where (:query = '' or Name like concat('%', :query, '%')) and (:status = '' or Status = :status)", nativeQuery = true)
    Long countByFilters(@Param("query") String query, @Param("status") String status);
}
