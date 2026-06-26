package com.ccadmin.app.subscriber.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriber_user")
public class SubscriberUserEntity extends AuditTableEntity {
    @Id
    public String SubscriberUserCod;
    public String Email;
    public String UserName;
    public String PasswordHash;
    public String Names;
    public LocalDateTime LastLoginDate;
}
