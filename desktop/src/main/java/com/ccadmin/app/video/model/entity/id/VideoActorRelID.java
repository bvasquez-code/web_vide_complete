package com.ccadmin.app.video.model.entity.id;

import java.io.Serializable;
import java.util.Objects;

public class VideoActorRelID implements Serializable {
    public String VideoCod;
    public String ActorCod;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoActorRelID that)) return false;
        return Objects.equals(VideoCod, that.VideoCod) && Objects.equals(ActorCod, that.ActorCod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(VideoCod, ActorCod);
    }
}
