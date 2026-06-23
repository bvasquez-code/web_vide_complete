package com.ccadmin.app.video.repository;

import com.ccadmin.app.video.model.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, String> {
    @Query(value = "select * from tag where Name = :Name limit 1", nativeQuery = true)
    Optional<TagEntity> findByName(@Param("Name") String Name);

    @Query(value = "select * from tag where Status = 'A' order by Name", nativeQuery = true)
    List<TagEntity> findActives();

    @Query(value = "select * from tag where (:query = '' or Name like concat('%', :query, '%')) and (:status = '' or Status = :status) order by Name limit :init, :limit", nativeQuery = true)
    List<TagEntity> findByFilters(@Param("query") String query, @Param("status") String status, @Param("init") Integer init, @Param("limit") Integer limit);

    @Query(value = "select count(*) from tag where (:query = '' or Name like concat('%', :query, '%')) and (:status = '' or Status = :status)", nativeQuery = true)
    Long countByFilters(@Param("query") String query, @Param("status") String status);
}
