package com.ccadmin.app.subscriber.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "video_capture_suggestion")
public class VideoCaptureSuggestionEntity extends AuditTableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long SuggestionId;
    public String VideoCod;
    public String SubscriberUserCod;
    public String ImageUrl;
    public BigDecimal CaptureSecond;
    public String Comment;
    public String ReviewComment;
    public Long ApprovedCaptureId;
}
