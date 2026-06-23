package com.ccadmin.app.video.model.entity.id;

import java.io.Serializable;
import java.util.Objects;

public class VideoTagRelID implements Serializable {
    public String VideoCod;
    public String TagCod;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoTagRelID that)) return false;
        return Objects.equals(VideoCod, that.VideoCod) && Objects.equals(TagCod, that.TagCod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(VideoCod, TagCod);
    }
}
