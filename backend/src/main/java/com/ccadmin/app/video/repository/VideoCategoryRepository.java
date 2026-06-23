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

    @Query(value = "select * from video_category where (:query = '' or Name like concat('%', :query, '%')) and (:status = '' or Status = :status) order by DisplayOrder, Name limit :init, :limit", nativeQuery = true)
    List<VideoCategoryEntity> findByFilters(@Param("query") String query, @Param("status") String status, @Param("init") Integer init, @Param("limit") Integer limit);

    @Query(value = "select count(*) from video_category where (:query = '' or Name like concat('%', :query, '%')) and (:status = '' or Status = :status)", nativeQuery = true)
    Long countByFilters(@Param("query") String query, @Param("status") String status);
}
