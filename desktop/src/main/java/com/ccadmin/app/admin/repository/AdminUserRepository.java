package com.ccadmin.app.admin.repository;

import com.ccadmin.app.admin.model.entity.AdminUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUserEntity, String> {
    @Query(value = "select * from admin_user where UserName = :UserName and Status = :Status limit 1", nativeQuery = true)
    Optional<AdminUserEntity> findByUserNameAndStatus(@Param("UserName") String UserName, @Param("Status") String Status);
}
