package com.ccadmin.app.desktop.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "desktop_app_config")
public class DesktopAppConfigEntity extends AuditTableEntity {
    @Id
    public String ConfigKey;
    public String ConfigValue;
    public String Description;
}
