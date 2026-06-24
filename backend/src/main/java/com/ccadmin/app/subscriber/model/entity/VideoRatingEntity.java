package com.ccadmin.app.subscriber.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "video_rating")
public class VideoRatingEntity extends AuditTableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long RatingId;
    public String VideoCod;
    public String SubscriberUserCod;
    public Integer RatingValue;
}
