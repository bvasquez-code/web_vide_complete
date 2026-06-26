package com.ccadmin.app.video.repository;

import com.ccadmin.app.video.model.entity.ActorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<ActorEntity, String> {
    @Query(value = "select * from actor where Name = :Name limit 1", nativeQuery = true)
    Optional<ActorEntity> findByName(@Param("Name") String Name);

    @Query(value = "select * from actor where Status = 'A' order by Name", nativeQuery = true)
    List<ActorEntity> findActives();

    @Query(value = "select * from actor where (:query = '' or Name like concat('%', :query, '%')) and (:status = '' or Status = :status) order by Name limit :init, :limit", nativeQuery = true)
    List<ActorEntity> findByFilters(@Param("query") String query, @Param("status") String status, @Param("init") Integer init, @Param("limit") Integer limit);

    @Query(value = "select count(*) from actor where (:query = '' or Name like concat('%', :query, '%')) and (:status = '' or Status = :status)", nativeQuery = true)
    Long countByFilters(@Param("query") String query, @Param("status") String status);
}
