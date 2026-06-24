package com.ccadmin.app.subscriber.repository;

import com.ccadmin.app.subscriber.model.entity.SubscriberUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriberUserRepository extends JpaRepository<SubscriberUserEntity, String> {
    @Query(value = "select * from subscriber_user where (UserName = :UserName or Email = :UserName) and Status = :Status limit 1", nativeQuery = true)
    Optional<SubscriberUserEntity> findByLoginAndStatus(@Param("UserName") String UserName, @Param("Status") String Status);

    @Query(value = "select count(1) from subscriber_user where UserName = :UserName or Email = :Email", nativeQuery = true)
    Long countByUserNameOrEmail(@Param("UserName") String UserName, @Param("Email") String Email);
}
