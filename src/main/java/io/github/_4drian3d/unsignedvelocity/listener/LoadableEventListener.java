package io.github._4drian3d.unsignedvelocity.listener;

import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;

public interface LoadableEventListener {
    void register(UnSignedVelocity plugin);

    boolean canBeLoaded();
}
