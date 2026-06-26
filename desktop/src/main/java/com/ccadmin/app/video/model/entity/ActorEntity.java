package com.ccadmin.app.video.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "actor")
public class ActorEntity extends AuditTableEntity {
    @Id
    public String ActorCod;
    public String Name;
    public String Description;
    public String ImageUrl;

    public ActorEntity validate() {
        if (Name == null || Name.isBlank()) {
            throw new IllegalArgumentException("El nombre del actor es obligatorio.");
        }
        if (Status == null || Status.isBlank()) {
            Status = "A";
        }
        return this;
    }
}
