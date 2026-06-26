package com.ccadmin.app.video.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import com.ccadmin.app.video.model.entity.id.VideoCategoryRelID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(VideoCategoryRelID.class)
@Table(name = "video_category_rel")
public class VideoCategoryRelEntity extends AuditTableEntity {
    @Id
    public String VideoCod;
    @Id
    public String CategoryCod;
    public String IsPrimary;
}
