package io.github._4drian3d.unsignedvelocity.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;

public interface LoadablePacketListener {
    default void register() {
        PacketEvents.getAPI().getEventManager().registerListener((PacketListenerCommon) this);
    }

    boolean canBeLoaded();
}
