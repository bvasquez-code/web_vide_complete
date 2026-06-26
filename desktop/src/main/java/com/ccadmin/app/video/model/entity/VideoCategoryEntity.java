package com.ccadmin.app.video.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "video_category")
public class VideoCategoryEntity extends AuditTableEntity {
    @Id
    public String CategoryCod;
    public String Name;
    public String Description;
    public String ImageUrl;
    public Integer DisplayOrder;

    public VideoCategoryEntity validate() {
        if (Name == null || Name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoria es obligatorio.");
        }
        if (Status == null || Status.isBlank()) {
            Status = "A";
        }
        if (DisplayOrder == null) {
            DisplayOrder = 0;
        }
        return this;
    }
}
