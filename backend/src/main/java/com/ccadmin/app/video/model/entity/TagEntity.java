package com.ccadmin.app.video.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tag")
public class TagEntity extends AuditTableEntity {
    @Id
    public String TagCod;
    public String Name;

    public TagEntity validate() {
        if (Name == null || Name.isBlank()) {
            throw new IllegalArgumentException("El nombre del tag es obligatorio.");
        }
        if (Status == null || Status.isBlank()) {
            Status = "A";
        }
        return this;
    }
}
