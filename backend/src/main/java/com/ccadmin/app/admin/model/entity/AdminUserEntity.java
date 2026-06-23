package com.ccadmin.app.admin.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_user")
public class AdminUserEntity extends AuditTableEntity {
    @Id
    public String AdminUserCod;
    public String UserName;
    public String PasswordHash;
    public String Names;
    public LocalDateTime LastLoginDate;
}
