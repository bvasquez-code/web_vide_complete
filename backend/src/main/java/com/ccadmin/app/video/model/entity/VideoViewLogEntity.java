package com.ccadmin.app.video.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "video_view_log")
public class VideoViewLogEntity extends AuditTableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long ViewLogId;
    public String VideoCod;
    public String ViewerIp;
    public String UserAgent;
}
