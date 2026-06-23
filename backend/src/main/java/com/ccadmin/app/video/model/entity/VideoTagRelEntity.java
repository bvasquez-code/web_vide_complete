package com.ccadmin.app.video.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import com.ccadmin.app.video.model.entity.id.VideoTagRelID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(VideoTagRelID.class)
@Table(name = "video_tag_rel")
public class VideoTagRelEntity extends AuditTableEntity {
    @Id
    public String VideoCod;
    @Id
    public String TagCod;
}
