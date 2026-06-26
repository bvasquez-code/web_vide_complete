package com.ccadmin.app.shared.model.entity;

import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public class AuditTableEntity {
    public String CreationUser;
    public LocalDateTime CreationDate;
    public String ModifyUser;
    public LocalDateTime ModifyDate;
    public String Status;

    public void addSessionCreate(String userCod) {
        this.CreationUser = userCod;
        this.CreationDate = LocalDateTime.now();
        this.ModifyUser = userCod;
        this.ModifyDate = LocalDateTime.now();
        if (this.Status == null || this.Status.isBlank()) {
            this.Status = "A";
        }
    }

    public void addSessionModify(String userCod) {
        this.ModifyUser = userCod;
        this.ModifyDate = LocalDateTime.now();
        if (this.Status == null || this.Status.isBlank()) {
            this.Status = "A";
        }
    }

    public void active(String userCod) {
        this.Status = "A";
        addSessionModify(userCod);
    }

    public void inactive(String userCod) {
        this.Status = "I";
        addSessionModify(userCod);
    }
}
