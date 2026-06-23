package com.ccadmin.app.video.model.entity;

import com.ccadmin.app.shared.model.entity.AuditTableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "video")
public class VideoEntity extends AuditTableEntity {
    @Id
    public String VideoCod;
    public String Title;
    public String ShortDescription;
    public String LongDescription;
    public String ThumbnailUrl;
    public String SourceType;
    public String SourceValue;
    public String Duration;
    public Long ViewCount;
    public LocalDateTime PublishDate;

    public VideoEntity validate() {
        if (Title == null || Title.isBlank()) {
            throw new IllegalArgumentException("El titulo del video es obligatorio.");
        }
        if (SourceType == null || SourceType.isBlank()) {
            throw new IllegalArgumentException("El tipo de origen del video es obligatorio.");
        }
        if (!Set.of("EMBED", "URL", "PATH").contains(SourceType)) {
            throw new IllegalArgumentException("Tipo de origen no reconocido.");
        }
        if (SourceValue == null || SourceValue.isBlank()) {
            throw new IllegalArgumentException("La referencia del video es obligatoria.");
        }
        if ("URL".equals(SourceType) && !(SourceValue.startsWith("http://") || SourceValue.startsWith("https://"))) {
            throw new IllegalArgumentException("La referencia URL debe iniciar con http:// o https://.");
        }
        if (ViewCount == null) {
            ViewCount = 0L;
        }
        if (Status == null || Status.isBlank()) {
            Status = "A";
        }
        return this;
    }
}
