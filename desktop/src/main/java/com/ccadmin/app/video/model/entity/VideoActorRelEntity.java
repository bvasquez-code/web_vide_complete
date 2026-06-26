package com.ccadmin.app.video.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import com.ccadmin.app.video.model.entity.id.VideoActorRelID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(VideoActorRelID.class)
@Table(name = "video_actor_rel")
public class VideoActorRelEntity extends AuditTableEntity {
    @Id
    public String VideoCod;
    @Id
    public String ActorCod;
}
