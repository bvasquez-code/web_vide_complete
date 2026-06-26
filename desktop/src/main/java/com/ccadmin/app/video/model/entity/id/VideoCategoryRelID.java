package com.ccadmin.app.video.model.entity.id;

import java.io.Serializable;
import java.util.Objects;

public class VideoCategoryRelID implements Serializable {
    public String VideoCod;
    public String CategoryCod;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoCategoryRelID that)) return false;
        return Objects.equals(VideoCod, that.VideoCod) && Objects.equals(CategoryCod, that.CategoryCod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(VideoCod, CategoryCod);
    }
}
